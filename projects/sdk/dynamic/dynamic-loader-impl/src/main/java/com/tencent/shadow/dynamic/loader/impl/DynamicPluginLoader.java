/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  android.content.ComponentName
 *  android.content.Context
 *  android.content.Intent
 *  android.content.ServiceConnection
 *  android.os.Handler
 *  android.os.IBinder
 *  android.os.Looper
 *  com.tencent.shadow.core.common.InstalledApk
 *  com.tencent.shadow.core.loader.ShadowPluginLoader
 *  com.tencent.shadow.core.loader.infos.PluginParts
 *  com.tencent.shadow.core.loader.managers.PluginServiceManager
 *  com.tencent.shadow.core.runtime.container.ContentProviderDelegateProvider
 *  com.tencent.shadow.core.runtime.container.ContentProviderDelegateProviderHolder
 *  com.tencent.shadow.core.runtime.container.DelegateProvider
 *  com.tencent.shadow.core.runtime.container.DelegateProviderHolder
 *  com.tencent.shadow.dynamic.host.UuidManager
 *  kotlin.Metadata
 *  kotlin.jvm.functions.Function0
 *  kotlin.jvm.internal.DefaultConstructorMarker
 *  kotlin.jvm.internal.Intrinsics
 *  kotlin.jvm.internal.Ref$BooleanRef
 *  kotlin.jvm.internal.Ref$ObjectRef
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.tencent.shadow.dynamic.loader.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.loader.ShadowPluginLoader;
import com.tencent.shadow.core.loader.infos.PluginParts;
import com.tencent.shadow.core.runtime.container.ContentProviderDelegateProvider;
import com.tencent.shadow.core.runtime.container.ContentProviderDelegateProviderHolder;
import com.tencent.shadow.core.runtime.container.DelegateProviderHolder;
import com.tencent.shadow.dynamic.host.UuidManager;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public final class DynamicPluginLoader {
    private final ShadowPluginLoader mPluginLoader;
    private final ClassLoader mDynamicLoaderClassLoader;
    private Context mContext;
    private UuidManager mUuidManager;
    private final Handler mUiHandler;
    private final HashMap<IBinder, ServiceConnection> mConnectionMap;
    private static final String CORE_LOADER_FACTORY_IMPL_NAME = "com.tencent.shadow.dynamic.loader.impl.CoreLoaderFactoryImpl";

    public final void setUuidManager( UuidManager p0) {
        if (p0 != null) {
            this.mUuidManager = p0;
        }
    }

    public final void loadPlugin( String partKey) {
        UuidManager uuidManager = this.mUuidManager;

        try {
            InstalledApk installedApk2 = uuidManager.getPlugin(partKey);
            Future future = this.mPluginLoader.loadPlugin(installedApk2);
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public final Map<String, Boolean> getLoadedPlugin() {
        Map<String, PluginParts> plugins = this.mPluginLoader.getAllPluginPart();
        HashMap loadPlugins = new HashMap();
        for (Map.Entry<String, PluginParts> part : plugins.entrySet()) {
            ((Map) loadPlugins).put(part.getKey(), ((PluginParts) part.getValue()).getApplication().isCallOnCreate);
        }
        return loadPlugins;
    }

    public final synchronized void callApplicationOnCreate( String partKey) {
        this.mPluginLoader.callApplicationOnCreate(partKey);
    }

    
    public final Intent convertActivityIntent( Intent pluginActivityIntent) {
        return this.mPluginLoader.getMComponentManager().convertPluginActivityIntent(pluginActivityIntent);
    }

    /*
     * WARNING - void declaration
     */
    
    public final synchronized ComponentName startPluginService( final Intent pluginServiceIntent) {


        // 确保在ui线程调用
        final ComponentName[] componentName = new ComponentName[1];
        if (isUiThread()) {
            componentName[0] = mPluginLoader.getPluginServiceManager().startPluginService(pluginServiceIntent);
        } else {
            final CountDownLatch waitUiLock = new CountDownLatch(1);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    componentName[0] = mPluginLoader.getPluginServiceManager().startPluginService(pluginServiceIntent);
                    waitUiLock.countDown();
                }
            });
            try {
                waitUiLock.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return componentName[0];
    }

    /*
     * WARNING - void declaration
     */
    public final synchronized boolean stopPluginService( final Intent pluginServiceIntent) {

        // 确保在ui线程调用
        final boolean[] stopped = new boolean[1];
        if (isUiThread()) {
            stopped[0] = mPluginLoader.getPluginServiceManager().stopPluginService(pluginServiceIntent);
        } else {
            final CountDownLatch waitUiLock = new CountDownLatch(1);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopped[0] = mPluginLoader.getPluginServiceManager().stopPluginService(pluginServiceIntent);
                    waitUiLock.countDown();
                }
            });
            try {
                waitUiLock.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return stopped[0];
    }

    /*
     * WARNING - void declaration
     */
    public final synchronized boolean bindPluginService( final Intent pluginServiceIntent,  final BinderPluginServiceConnection binderPsc, final int flags) {
        // 确保在ui线程调用
        final boolean[] stop = new boolean[1];
        if (isUiThread()) {
            stop[0] = realAction(pluginServiceIntent, binderPsc, flags);
        } else {
            final CountDownLatch waitUiLock = new CountDownLatch(1);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    stop[0] = realAction(pluginServiceIntent, binderPsc, flags);
                    waitUiLock.countDown();
                }
            });
            try {
                waitUiLock.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return stop[0];
    }

    public final synchronized void unbindService( final IBinder connBinder) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                ServiceConnection connection = mConnectionMap.get(connBinder);
                if (connection != null) {
                    mConnectionMap.remove(connBinder);
                    mPluginLoader.getPluginServiceManager().unbindPluginService(connection);
                }
            }
        });
    }

    public final synchronized void startActivityInPluginProcess( final Intent intent) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mContext.startActivity(intent);
            }
        });
    }

    private final boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public final <T> T getInterface( ClassLoader $this$getInterface,  Class<T> clazz,  String className) throws Exception {
        try {
            Class<?> interfaceImplementClass = $this$getInterface.loadClass(className);
            Object obj = interfaceImplementClass.newInstance();
            Object interfaceImplement = obj;
            T t = clazz.cast(interfaceImplement);
            return t;
        } catch (ClassNotFoundException e) {
            throw new Exception(e);
        } catch (InstantiationException e) {
            throw new Exception(e);
        } catch (ClassCastException e) {
            throw new Exception(e);
        } catch (IllegalAccessException e) {
            throw new Exception(e);
        }
    }


    private boolean realAction(Intent pluginServiceIntent, BinderPluginServiceConnection binderPsc, int flags) {
        if (mConnectionMap.get(binderPsc.getmRemote()) == null) {
            mConnectionMap.put(binderPsc.getmRemote(), new ServiceConnectionWrapper(binderPsc));
        }

        ServiceConnection connWrapper = mConnectionMap.get(binderPsc.getmRemote());
        return mPluginLoader.getPluginServiceManager().bindPluginService(pluginServiceIntent, connWrapper, flags);
    }

    public DynamicPluginLoader(Context hostContext) {
        ClassLoader classLoader = DynamicPluginLoader.class.getClassLoader();
        this.mDynamicLoaderClassLoader = classLoader;
        this.mUiHandler = new Handler(Looper.getMainLooper());
        this.mConnectionMap = new HashMap();
        try {
            CoreLoaderFactory coreLoaderFactory = this.getInterface(this.mDynamicLoaderClassLoader, CoreLoaderFactory.class, CORE_LOADER_FACTORY_IMPL_NAME);
            this.mPluginLoader = coreLoaderFactory.build(hostContext);
            DelegateProviderHolder.setDelegateProvider((String) this.mPluginLoader.getDelegateProviderKey(), this.mPluginLoader);
            ContentProviderDelegateProviderHolder.setContentProviderDelegateProvider((ContentProviderDelegateProvider) ((ContentProviderDelegateProvider) this.mPluginLoader));
            this.mPluginLoader.onCreate();
        } catch (Exception e) {
            throw new RuntimeException("\u5f53\u524d\u7684classLoader\u627e\u4e0d\u5230PluginLoader\u7684\u5b9e\u73b0", e);
        }
        this.mContext = hostContext;
    }

    private static final class ServiceConnectionWrapper
            implements ServiceConnection {
        private final BinderPluginServiceConnection mConnection;

        public void onServiceDisconnected( ComponentName name) {
            try {
                this.mConnection.onServiceDisconnected(name);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceConnected( ComponentName name,  IBinder service) {
            try {
                this.mConnection.onServiceConnected(name, service);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public ServiceConnectionWrapper( BinderPluginServiceConnection mConnection) {
            this.mConnection = mConnection;
        }
    }


}
