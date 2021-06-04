package mobi.oneway.sd.core.loader.blocs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;

import mobi.oneway.sd.core.loader.classloaders.PluginClassLoader;
import mobi.oneway.sd.core.loader.exceptions.CreateApplicationException;
import mobi.oneway.sd.core.loader.infos.PluginInfo;
import mobi.oneway.sd.core.loader.managers.ComponentManager;
import mobi.oneway.sd.core.runtime.ShadowAppComponentFactory;
import mobi.oneway.sd.core.runtime.ShadowApplication;


public class CreateApplicationBloc {

    public static ShadowApplication createShadowApplication(PluginClassLoader pluginClassLoader,
                                                            PluginInfo pluginInfo,
                                                            Resources resources,
                                                            Context hostAppContext,
                                                            ComponentManager componentManager,
                                                            ApplicationInfo applicationInfo,
                                                            ShadowAppComponentFactory appComponentFactory) throws CreateApplicationException {
        try {
            String appClassName = pluginInfo.getApplicationClassName() != null
                    ? pluginInfo.getApplicationClassName() : ShadowApplication.class.getName();
            ShadowApplication shadowApplication = appComponentFactory.instantiateApplication(pluginClassLoader, appClassName);
            String partKey = pluginInfo.getPartKey();
            shadowApplication.setPluginResources(resources);
            shadowApplication.setPluginClassLoader(pluginClassLoader);
            shadowApplication.setPluginComponentLauncher(componentManager);
            shadowApplication.setBroadcasts(componentManager.getBroadcastsByPartKey(partKey));
            shadowApplication.setAppComponentFactory(appComponentFactory);
            shadowApplication.setApplicationInfo(applicationInfo);
            shadowApplication.setBusinessName(pluginInfo.getBusinessName());
            shadowApplication.setPluginPartKey(partKey);

            //和ShadowActivityDelegate.initPluginActivity一样，attachBaseContext放到最后
            shadowApplication.setHostApplicationContextAsBase(hostAppContext);
            shadowApplication.setTheme(applicationInfo.theme);
            return shadowApplication;
        } catch (Exception e) {
            throw new CreateApplicationException(e);
        }
    }
}
