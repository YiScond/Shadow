package mobi.oneway.sd.dynamic.loader.impl;

import android.content.Context;

import mobi.oneway.sd.core.loader.ShadowPluginLoader;
import mobi.oneway.sd.core.loader.managers.ComponentManager;

/**
 * 这里的类名和包名需要固定
 */
public class CustomePluginLoader extends ShadowPluginLoader {

    private ComponentManager componentManager;

    public CustomePluginLoader(Context hostAppContext) {
        super(hostAppContext);
        componentManager = new CustomComponentManager(hostAppContext);
    }

    @Override
    public ComponentManager getComponentManager() {
        return componentManager;
    }
}
