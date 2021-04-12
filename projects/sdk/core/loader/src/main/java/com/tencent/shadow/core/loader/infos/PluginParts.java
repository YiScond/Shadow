package com.tencent.shadow.core.loader.infos;

import android.content.res.Resources;
import com.tencent.shadow.core.loader.classloaders.PluginClassLoader;
import com.tencent.shadow.core.runtime.PluginPackageManager;
import com.tencent.shadow.core.runtime.ShadowAppComponentFactory;
import com.tencent.shadow.core.runtime.ShadowApplication;

public final class PluginParts {
    
    private final ShadowAppComponentFactory appComponentFactory;
    
    private final ShadowApplication application;
    
    private final PluginClassLoader classLoader;
    
    private final Resources resources;
    
    private final String businessName;
    
    private final PluginPackageManager pluginPackageManager;

    
    public final ShadowAppComponentFactory getAppComponentFactory() {
        return this.appComponentFactory;
    }

    
    public final ShadowApplication getApplication() {
        return this.application;
    }

    
    public final PluginClassLoader getClassLoader() {
        return this.classLoader;
    }

    
    public final Resources getResources() {
        return this.resources;
    }

    
    public final String getBusinessName() {
        return this.businessName;
    }

    
    public final PluginPackageManager getPluginPackageManager() {
        return this.pluginPackageManager;
    }

    public PluginParts( ShadowAppComponentFactory appComponentFactory,  ShadowApplication application,  PluginClassLoader classLoader,  Resources resources,  String businessName,  PluginPackageManager pluginPackageManager) {
        super();
        this.appComponentFactory = appComponentFactory;
        this.application = application;
        this.classLoader = classLoader;
        this.resources = resources;
        this.businessName = businessName;
        this.pluginPackageManager = pluginPackageManager;
    }
}
