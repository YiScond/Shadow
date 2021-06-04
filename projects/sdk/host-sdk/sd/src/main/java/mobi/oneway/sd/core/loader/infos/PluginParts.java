package mobi.oneway.sd.core.loader.infos;

import android.content.res.Resources;

import mobi.oneway.sd.core.loader.classloaders.PluginClassLoader;
import mobi.oneway.sd.core.runtime.PluginPackageManager;
import mobi.oneway.sd.core.runtime.ShadowAppComponentFactory;
import mobi.oneway.sd.core.runtime.ShadowApplication;

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
