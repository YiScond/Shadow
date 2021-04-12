package com.tencent.shadow.core.loader.delegates;

import android.content.res.Resources;

import com.tencent.shadow.core.loader.classloaders.PluginClassLoader;
import com.tencent.shadow.core.loader.managers.ComponentManager;
import com.tencent.shadow.core.runtime.ShadowAppComponentFactory;
import com.tencent.shadow.core.runtime.ShadowApplication;

public abstract class ShadowDelegate {

    public void  inject(ShadowApplication shadowApplication) {
        _pluginApplication = shadowApplication;
    }

    public void inject(ShadowAppComponentFactory appComponentFactory) {
        _appComponentFactory = appComponentFactory;
    }

    public void inject(PluginClassLoader pluginClassLoader) {
        _pluginClassLoader = pluginClassLoader;
    }

    public void inject(Resources resources) {
        _pluginResources = resources;
    }

    public void inject(ComponentManager componentManager) {
        _componentManager = componentManager;
    }

    private  ShadowAppComponentFactory _appComponentFactory;
    private ShadowApplication _pluginApplication;
    private PluginClassLoader _pluginClassLoader;
    private Resources  _pluginResources;
    private ComponentManager  _componentManager;


    public ShadowAppComponentFactory getMAppComponentFactory() {
        return _appComponentFactory;
    }

    public ShadowApplication getMPluginApplication() {
        return _pluginApplication;
    }

    public PluginClassLoader getMPluginClassLoader() {
        return _pluginClassLoader;
    }

    public Resources getMPluginResources() {
        return _pluginResources;
    }

    public ComponentManager getMComponentManager() {
        return _componentManager;
    }
}
