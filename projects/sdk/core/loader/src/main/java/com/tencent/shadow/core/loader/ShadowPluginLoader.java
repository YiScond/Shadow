package com.tencent.shadow.core.loader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.core.load_parameters.LoadParameters;
import com.tencent.shadow.core.loader.blocs.LoadPluginBloc;
import com.tencent.shadow.core.loader.delegates.DI;
import com.tencent.shadow.core.loader.delegates.ShadowActivityDelegate;
import com.tencent.shadow.core.loader.delegates.ShadowContentProviderDelegate;
import com.tencent.shadow.core.loader.delegates.ShadowDelegate;
import com.tencent.shadow.core.loader.exceptions.LoadPluginException;
import com.tencent.shadow.core.loader.infos.PluginParts;
import com.tencent.shadow.core.loader.managers.ComponentManager;
import com.tencent.shadow.core.loader.managers.PluginContentProviderManager;
import com.tencent.shadow.core.loader.managers.PluginServiceManager;
import com.tencent.shadow.core.runtime.ShadowApplication;
import com.tencent.shadow.core.runtime.UriConverter;
import com.tencent.shadow.core.runtime.container.ContentProviderDelegateProvider;
import com.tencent.shadow.core.runtime.container.DelegateProvider;
import com.tencent.shadow.core.runtime.container.DelegateProviderHolder;
import com.tencent.shadow.core.runtime.container.HostActivityDelegate;
import com.tencent.shadow.core.runtime.container.HostActivityDelegator;
import com.tencent.shadow.core.runtime.container.HostContentProviderDelegate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ShadowPluginLoader implements DelegateProvider, DI, ContentProviderDelegateProvider {

    protected ExecutorService mExecutorService = Executors.newCachedThreadPool();

    public String delegateProviderKey = DelegateProviderHolder.DEFAULT_KEY;

    /**
     * loadPlugin方法是在子线程被调用的。而getHostActivityDelegate方法是在主线程被调用的。
     * 两个方法需要传递数据（主要是PluginParts），因此需要同步。
     */
    private ReentrantLock mLock = new ReentrantLock();

    /**
     * 多插件Map
     * key: partKey
     * value: PluginParts
     *
     * @GuardedBy("mLock")
     */
    private Map<String, PluginParts> mPluginPartsMap = new HashMap<>();


    private ComponentManager mComponentManager;

    /**
     * @GuardedBy("mLock")
     */
    public abstract ComponentManager getComponentManager();

    /**
     * @GuardedBy("mLock")
     */
    private Set<PackageInfo> mPluginPackageInfoSet = new HashSet<>();

    private PluginServiceManager mPluginServiceManager;

    private PluginContentProviderManager mPluginContentProviderManager = new PluginContentProviderManager();

    private Lock mPluginServiceManagerLock = new ReentrantLock();

    private Context mHostAppContext;

    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    private static Logger mLogger = LoggerFactory.getLogger(ShadowPluginLoader.class);


    public ShadowPluginLoader(Context hostAppContext) {
        this.mHostAppContext = hostAppContext;
        UriConverter.setUriParseDelegate(mPluginContentProviderManager);
    }


    public PluginServiceManager getPluginServiceManager() {

        mPluginServiceManagerLock.lock();
        try {
            return mPluginServiceManager;
        } finally {
            mPluginServiceManagerLock.unlock();
        }
    }

    public PluginParts getPluginParts(String partKey) {

        mLock.lock();
        try {
            return mPluginPartsMap.get(partKey);
        } finally {
            mLock.unlock();
        }
    }

    public Map<String, PluginParts> getAllPluginPart() {

        mLock.lock();
        try {
            return mPluginPartsMap;
        } finally {
            mLock.unlock();
        }
    }


    public void onCreate() {
        mComponentManager = getComponentManager();
        mComponentManager.setPluginContentProviderManager(mPluginContentProviderManager);
    }

    public void callApplicationOnCreate(final String partKey) {
        if (isUiThread()) {
            realAction(partKey);
        } else {
            final CountDownLatch waitUiLock = new CountDownLatch(1);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    realAction(partKey);
                    waitUiLock.countDown();
                }
            });
            try {
                waitUiLock.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Future loadPlugin(
            InstalledApk installedApk
    ) throws LoadPluginException {
        LoadParameters loadParameters = getLoadParameters(installedApk);
        if (mLogger.isInfoEnabled()) {
            mLogger.info("start loadPlugin");
        }
        // 在这里初始化PluginServiceManager
        mPluginServiceManagerLock.lock();
        try {
            if (mPluginServiceManager == null) {
                mPluginServiceManager = new PluginServiceManager(this, mHostAppContext);
            }
        } finally {
            mPluginServiceManagerLock.unlock();
        }

        mComponentManager.setPluginServiceManager(mPluginServiceManager);

        return LoadPluginBloc.loadPlugin(
                mExecutorService,
                mPluginPackageInfoSet,
                allPluginPackageInfo(),
                mComponentManager,
                mLock,
                mPluginPartsMap,
                mHostAppContext,
                installedApk,
                loadParameters);
    }

    private PackageInfo[] allPluginPackageInfo() {
        mLock.lock();
        try {
            return mPluginPackageInfoSet.toArray(new PackageInfo[]{});
        } finally {
            mLock.unlock();
        }
    }


    @Override
    public HostActivityDelegate getHostActivityDelegate(Class<? extends HostActivityDelegator> delegator) {
        return new ShadowActivityDelegate(this);
    }

    @Override
    public HostContentProviderDelegate getHostContentProviderDelegate() {
        return new ShadowContentProviderDelegate(mPluginContentProviderManager);
    }

    @Override
    public void inject(ShadowDelegate delegate, String partKey) {
        mLock.lock();
        try {
            PluginParts pluginParts = mPluginPartsMap.get(partKey);
            if (pluginParts == null) {
                throw new IllegalStateException("partKey==${partKey}在map中找不到。此时map：${mPluginPartsMap}");
            } else {
                delegate.inject(pluginParts.getAppComponentFactory());
                delegate.inject(pluginParts.getApplication());
                delegate.inject(pluginParts.getClassLoader());
                delegate.inject(pluginParts.getResources());
                delegate.inject(mComponentManager);
            }
        } finally {
            mLock.unlock();
        }
    }

    public LoadParameters getLoadParameters(InstalledApk installedApk) {
        return installedApk.loadParameters;
    }


    private boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    private void realAction(String partKey) {
        PluginParts pluginParts = getPluginParts(partKey);
        if (pluginParts != null) {
            ShadowApplication application = pluginParts.getApplication();
            application.attachBaseContext(mHostAppContext);
            mPluginContentProviderManager.createContentProviderAndCallOnCreate(
                    application, partKey, pluginParts);
            application.onCreate();
        }
    }


    public String getDelegateProviderKey() {
        return delegateProviderKey;
    }
}
