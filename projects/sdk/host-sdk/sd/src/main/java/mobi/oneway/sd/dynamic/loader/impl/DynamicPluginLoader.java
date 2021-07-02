package mobi.oneway.sd.dynamic.loader.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Looper;

import mobi.oneway.sd.core.common.InstalledApk;
import mobi.oneway.sd.core.loader.ShadowPluginLoader;
import mobi.oneway.sd.core.loader.infos.PluginParts;
import mobi.oneway.sd.core.loader.managers.ComponentManager;
import mobi.oneway.sd.core.runtime.container.ContentProviderDelegateProvider;
import mobi.oneway.sd.core.runtime.container.ContentProviderDelegateProviderHolder;
import mobi.oneway.sd.core.runtime.container.DelegateProviderHolder;
import mobi.oneway.sd.dynamic.host.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public final class DynamicPluginLoader {

    private final ShadowPluginLoader mPluginLoader;
    private Context mContext;
    private PluginManager mPluginManager;
    private final Handler mUiHandler;


    public DynamicPluginLoader(Context hostContext) {
        this.mUiHandler = new Handler(Looper.getMainLooper());
        this.mPluginLoader = new CustomePluginLoader(hostContext);
        DelegateProviderHolder.setDelegateProvider((String) this.mPluginLoader.getDelegateProviderKey(), this.mPluginLoader);
        ContentProviderDelegateProviderHolder.setContentProviderDelegateProvider((ContentProviderDelegateProvider) ((ContentProviderDelegateProvider) this.mPluginLoader));
        mPluginLoader.onCreate();
        this.mContext = hostContext;
    }


    public final void setPluginManager(PluginManager p0) {
        if (p0 != null) {
            this.mPluginManager = p0;
        }
    }

    public final void loadPlugin(String partKey, String[] hostWhiteList,String[] dependsOn) {
        PluginManager pluginManager = this.mPluginManager;

        try {
            InstalledApk installedApk2 = pluginManager.getPlugin(partKey, hostWhiteList,dependsOn);
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

    public ClassLoader getPluginClassloader(String partKey) {
        PluginParts pluginParts = mPluginLoader.getPluginParts(partKey);
        if (pluginParts != null) {
            return pluginParts.getClassLoader();
        }

        return null;
    }

    public final synchronized void callApplicationOnCreate(String partKey) {
        this.mPluginLoader.callApplicationOnCreate(partKey);
    }


    public final Intent convertActivityIntent(Intent pluginActivityIntent) {
        return this.mPluginLoader.getComponentManager().convertPluginActivityIntent(pluginActivityIntent);
    }


    public ComponentManager getComponentManager() {
        return mPluginLoader.getComponentManager();
    }

    /*
     * WARNING - void declaration
     */

    public final synchronized ComponentName startPluginService(final Intent pluginServiceIntent) {


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
    public final synchronized boolean stopPluginService(final Intent pluginServiceIntent) {

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
    public final synchronized boolean bindPluginService(final Intent pluginServiceIntent, final ServiceConnection connection, final int flags) {
        // 确保在ui线程调用
        final boolean[] stop = new boolean[1];
        if (isUiThread()) {
            stop[0] = realAction(pluginServiceIntent, connection, flags);
        } else {
            final CountDownLatch waitUiLock = new CountDownLatch(1);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    stop[0] = realAction(pluginServiceIntent, connection, flags);
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

    public final synchronized void unbindService(final ServiceConnection connection) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (connection != null) {
                    mPluginLoader.getPluginServiceManager().unbindPluginService(connection);
                }
            }
        });
    }

    public final synchronized void startActivityInPluginProcess(final Intent intent) {
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


    private boolean realAction(Intent pluginServiceIntent, ServiceConnection serviceConnection, int flags) {
        return mPluginLoader.getPluginServiceManager().bindPluginService(pluginServiceIntent, serviceConnection, flags);
    }

}
