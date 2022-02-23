package mobi.oneway.sd.helper;

import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;

import mobi.oneway.sd.core.loader.managers.ComponentManager;
import mobi.oneway.sd.core.manager.ShadowPluginManager;
import mobi.oneway.sd.core.runtime.ShadowContext;

/**
 * 包装Context 支持调用插件的能力
 */
public class PluginContextWrapper extends ShadowContext {

    private Context hostContext;
    private ShadowPluginManager shadowPluginManager;
    private ComponentManager componentManager;
    private Resources pluginResourceWrapper;


    public PluginContextWrapper(Context base) {
        super(base, 0);
        hostContext = base;
        shadowPluginManager = ShadowPluginManager.getInstance(base);
        componentManager = shadowPluginManager.getComponentManager();
        ShadowUtil.injectShadow(this);
    }


    @Override
    public void startActivity(Intent intent) {
        if (componentManager.isPluginComponent(intent)) {
            shadowPluginManager.startPluginActivity(intent);
        } else {
            hostContext.startActivity(intent);
        }
    }


    @Override
    public void unbindService(ServiceConnection conn) {
        if (!componentManager.unbindService(null, conn).first) {
            hostContext.unbindService(conn);
        }
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        if (componentManager.isPluginComponent(service)) {
            return shadowPluginManager.bindPluginService(service, conn, flags);
        } else {
            return hostContext.bindService(service, conn, flags);
        }

    }

    @Override
    public boolean stopService(Intent name) {
        if (componentManager.isPluginComponent(name)) {
            return shadowPluginManager.stopPluginService(name);
        } else {
            return hostContext.stopService(name);
        }
    }

    @Override
    public ComponentName startService(Intent service) {
        if (componentManager.isPluginComponent(service)) {
            return shadowPluginManager.startPluginService(service);
        } else {
            return hostContext.startService(service);
        }
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }

    @Override
    public Context getBaseContext() {
        return hostContext;
    }

    @Override
    public String getPackageName() {
        return hostContext.getPackageName();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return hostContext.getApplicationInfo();
    }

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            return super.getSystemService(name);
        }
        return hostContext.getSystemService(name);
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        hostContext.registerComponentCallbacks(callback);
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        hostContext.unregisterComponentCallbacks(callback);
    }

    @Override
    public Resources getResources() {
        if (pluginResourceWrapper == null) {
            Resources parentResources = super.getResources();
            String pluginPackageName = ThirdShadowApplication.getShadowApplication().getPackageName();
            pluginResourceWrapper = new PluginResourceWrapper(parentResources, pluginPackageName);
        }

        return pluginResourceWrapper;

    }
}
