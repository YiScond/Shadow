// PluginServiceManager.java
package mobi.oneway.sd.core.loader.managers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import mobi.oneway.sd.core.loader.ShadowPluginLoader;
import mobi.oneway.sd.core.loader.classloaders.PluginClassLoader;
import mobi.oneway.sd.core.loader.delegates.ShadowDelegate;
import mobi.oneway.sd.core.runtime.ShadowAppComponentFactory;
import mobi.oneway.sd.core.runtime.ShadowApplication;
import mobi.oneway.sd.core.runtime.ShadowService;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public final class PluginServiceManager {

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private UnsafePluginServiceManager delegate;

    public PluginServiceManager(ShadowPluginLoader mPluginLoader, Context context) {
        delegate = new UnsafePluginServiceManager(mPluginLoader, context);
    }


    /**
     * 在主线程中执行
     *
     * @param execCallback 执行结果回调
     * @param <T>
     * @return
     */
    private <T> T execInMainThread(execCallback<T> execCallback) {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            return execCallback != null ? execCallback.onResult() : null;
        } else {
            final Object[] result = new Object[1];
            CountDownLatch countDownLatch = new CountDownLatch(1);
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (execCallback != null) {
                        result[0] = execCallback.onResult();
                    }
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return (T) result[0];
        }

    }

    /**
     * 执行结果回调接口
     *
     * @param <T>
     */
    private interface execCallback<T> {
        T onResult();
    }

    public final ComponentName startPluginService(Intent intent) {
        return execInMainThread(new execCallback<ComponentName>() {
            @Override
            public ComponentName onResult() {
                return delegate.startPluginService(intent);
            }
        });
    }

    public final boolean stopPluginService(Intent intent) {
        return execInMainThread(new execCallback<Boolean>() {
            @Override
            public Boolean onResult() {
                return delegate.stopPluginService(intent);
            }
        });
    }

    public final boolean bindPluginService(Intent intent, ServiceConnection conn, int flags) {
        return execInMainThread(new execCallback<Boolean>() {
            @Override
            public Boolean onResult() {
                return delegate.bindPluginService(intent,conn,flags);
            }
        });
    }

    public final boolean unbindPluginService(ServiceConnection connection) {
        return execInMainThread(new execCallback<Boolean>() {
            @Override
            public Boolean onResult() {
                return delegate.unbindPluginService(connection);
            }
        });
    }

    private class UnsafePluginServiceManager {

        // 保存service的binder
        private final Map<ComponentName, IBinder> mServiceBinderMap;
        // service对应ServiceConnection集合
        private final Map<ComponentName, HashSet<ServiceConnection>> mServiceConnectionMap;
        // ServiceConnection与对应的Intent的集合
        private final Map<ServiceConnection, Intent> mConnectionIntentMap;
        // 所有已启动的service集合
        private final Map<ComponentName, ShadowService> mAliveServicesMap;
        // 通过startService启动起来的service集合
        private final Set<ComponentName> mServiceStartByStartServiceSet;
        // 存在mAliveServicesMap中，且stopService已经调用的service集合
        private final Set<ComponentName> mServiceStopCalledMap;
        private final ShadowPluginLoader mPluginLoader;
        private final Context mHostContext;
        private int startId;

        private final Collection getAllDelegates() {
            Collection var10000 = this.mAliveServicesMap.values();
            return var10000;
        }


        public final ComponentName startPluginService(Intent intent) {
            ComponentName var10000 = intent.getComponent();
            ComponentName componentName = var10000;
            if (!this.mAliveServicesMap.containsKey(componentName)) {
                ShadowService service = this.createServiceAndCallOnCreate(intent);
                ((Map) this.mAliveServicesMap).put(componentName, service);
                this.mServiceStartByStartServiceSet.add(componentName);
            }

            ShadowService var4 = (ShadowService) this.mAliveServicesMap.get(componentName);
            if (var4 != null) {
                var4.onStartCommand(intent, 0, getNewStartId());
            }

            return componentName;
        }

        public final boolean stopPluginService(Intent intent) {
            ComponentName var10000 = intent.getComponent();
            ComponentName componentName = var10000;
            if (this.mAliveServicesMap.containsKey(componentName)) {
                this.mServiceStopCalledMap.add(componentName);
                return this.destroyServiceIfNeed(componentName);
            } else {
                return false;
            }
        }

        public final boolean bindPluginService(Intent intent, ServiceConnection conn, int flags) {
            ComponentName var10000 = intent.getComponent();
            ComponentName componentName = var10000;
            ShadowService service;
            if (!this.mAliveServicesMap.containsKey(componentName)) {
                service = this.createServiceAndCallOnCreate(intent);
                ((Map) this.mAliveServicesMap).put(componentName, service);
            }

            Object var12 = this.mAliveServicesMap.get(componentName);
            service = (ShadowService) var12;
            if (!this.mServiceBinderMap.containsKey(componentName)) {
                ((Map) this.mServiceBinderMap).put(componentName, service.onBind(intent));
            }

            IBinder var13 = (IBinder) this.mServiceBinderMap.get(componentName);
            if (var13 != null) {
                IBinder var6 = var13;
                if (this.mServiceConnectionMap.containsKey(componentName)) {
                    var12 = this.mServiceConnectionMap.get(componentName);
                    if (!((HashSet) var12).contains(conn)) {
                        var12 = this.mServiceConnectionMap.get(componentName);
                        ((HashSet) var12).add(conn);
                        ((Map) this.mConnectionIntentMap).put(conn, intent);
                        conn.onServiceConnected(componentName, var6);
                    }
                } else {
                    HashSet connectionSet = new HashSet();
                    connectionSet.add(conn);
                    ((Map) this.mServiceConnectionMap).put(componentName, connectionSet);
                    ((Map) this.mConnectionIntentMap).put(conn, intent);
                    conn.onServiceConnected(componentName, var6);
                }
            }

            return true;
        }

        public final boolean unbindPluginService(ServiceConnection connection) {
            Map var4 = (Map) this.mServiceConnectionMap;
            boolean result = false;
            Iterator var3 = var4.entrySet().iterator();

            while (var3.hasNext()) {
                Entry var2 = (Entry) var3.next();
                ComponentName componentName = (ComponentName) var2.getKey();
                HashSet connSet = (HashSet) var2.getValue();
                if (connSet.contains(connection)) {
                    connSet.remove(connection);
                    Intent intent = (Intent) this.mConnectionIntentMap.remove(connection);
                    if (connSet.size() == 0) {
                        this.mServiceConnectionMap.remove(componentName);
                        ShadowService var10000 = (ShadowService) this.mAliveServicesMap.get(componentName);
                        if (var10000 != null) {
                            var10000.onUnbind(intent);
                        }
                    }

                    result = true;

                    this.destroyServiceIfNeed(componentName);
                    break;
                }
            }

            return result;

        }

        public final void onConfigurationChanged(Configuration newConfig) {
            Iterable $this$forEach$iv = (Iterable) this.getAllDelegates();
            Iterator var4 = $this$forEach$iv.iterator();

            while (var4.hasNext()) {
                Object element$iv = var4.next();
                ShadowService it = (ShadowService) element$iv;
                it.onConfigurationChanged(newConfig);
            }

        }

        public final void onLowMemory() {
            Iterable $this$forEach$iv = (Iterable) this.getAllDelegates();
            Iterator var3 = $this$forEach$iv.iterator();

            while (var3.hasNext()) {
                Object element$iv = var3.next();
                ShadowService it = (ShadowService) element$iv;
                it.onLowMemory();
            }

        }

        public final void onTrimMemory(int level) {
            Iterable $this$forEach$iv = (Iterable) this.getAllDelegates();
            Iterator var4 = $this$forEach$iv.iterator();

            while (var4.hasNext()) {
                Object element$iv = var4.next();
                ShadowService it = (ShadowService) element$iv;
                it.onTrimMemory(level);
            }

        }

        public final void onTaskRemoved(Intent rootIntent) {
            Iterable $this$forEach$iv = (Iterable) this.getAllDelegates();
            Iterator var4 = $this$forEach$iv.iterator();

            while (var4.hasNext()) {
                Object element$iv = var4.next();
                ShadowService it = (ShadowService) element$iv;
                it.onTaskRemoved(rootIntent);
            }

        }

        public final void onDestroy() {
            this.mServiceBinderMap.clear();
            this.mServiceConnectionMap.clear();
            this.mConnectionIntentMap.clear();
            this.mAliveServicesMap.clear();
            this.mServiceStartByStartServiceSet.clear();
            this.mServiceStopCalledMap.clear();
        }

        private final ShadowService createServiceAndCallOnCreate(Intent intent) {

            try {
                ShadowService service = this.newServiceInstance(intent);
                service.onCreate();
                return service;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

            return null;
        }

        private final ShadowService newServiceInstance(Intent intent) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
            ComponentName var10000 = intent.getComponent();
            ComponentName componentName = var10000;
            String businessName = this.mPluginLoader.getComponentManager().getComponentBusinessName(componentName);
            String partKey = this.mPluginLoader.getComponentManager().getComponentPartKey(componentName);
            String var8 = componentName.getClassName();
            String className = var8;
            TmpShadowDelegate tmpShadowDelegate = new TmpShadowDelegate();
            ShadowPluginLoader var9 = this.mPluginLoader;
            ShadowDelegate var10001 = tmpShadowDelegate;
            var9.inject(var10001, partKey);
            ShadowService service = null;

            service = tmpShadowDelegate.getAppComponentFactory().instantiateService(tmpShadowDelegate.getPluginClassLoader(), className, intent);
            service.setPluginResources(tmpShadowDelegate.getPluginResources());
            service.setPluginClassLoader(tmpShadowDelegate.getPluginClassLoader());
            service.setShadowApplication(tmpShadowDelegate.getPluginApplication());
            service.setPluginComponentLauncher(tmpShadowDelegate.getComponentManager());
            service.setApplicationInfo(tmpShadowDelegate.getPluginApplication().getApplicationInfo());
            service.setBusinessName(businessName);
            service.setPluginPartKey(partKey);
            service.setHostContextAsBase(this.mHostContext);

            return service;
        }

        private final boolean destroyServiceIfNeed(final ComponentName service) {
            if (!this.mServiceStartByStartServiceSet.contains(service)) {
                if (this.mServiceConnectionMap.get(service) == null) {
                    destoryService(service);
                    return true;
                }
            } else if (this.mServiceStopCalledMap.contains(service) && !this.mServiceConnectionMap.containsKey(service)) {
                destoryService(service);
                return true;
            }

            return false;
        }

        private void destoryService(ComponentName service) {
            ShadowService serviceDelegate = (ShadowService) mAliveServicesMap.remove(service);
            mServiceStopCalledMap.remove(service);
            mServiceBinderMap.remove(service);
            mServiceStartByStartServiceSet.remove(service);
            serviceDelegate.onDestroy();
        }


        public UnsafePluginServiceManager(ShadowPluginLoader mPluginLoader, Context mHostContext) {
            super();
            this.mPluginLoader = mPluginLoader;
            this.mHostContext = mHostContext;
            this.mServiceBinderMap = new HashMap();
            this.mServiceConnectionMap = new HashMap();
            this.mConnectionIntentMap = new HashMap();
            this.mAliveServicesMap = new HashMap();
            this.mServiceStartByStartServiceSet = new HashSet();
            this.mServiceStopCalledMap = new HashSet();
        }

        public final int getNewStartId() {
            startId = startId + 1;
            return startId;
        }
    }


    final class TmpShadowDelegate extends ShadowDelegate {

        public final ShadowApplication getPluginApplication() {
            return super.getMPluginApplication();
        }

        public final ShadowAppComponentFactory getAppComponentFactory() {
            return this.getMAppComponentFactory();
        }

        public final PluginClassLoader getPluginClassLoader() {
            return this.getMPluginClassLoader();
        }

        public final Resources getPluginResources() {
            return this.getMPluginResources();
        }

        public final ComponentManager getComponentManager() {
            return this.getMComponentManager();
        }

    }
}
