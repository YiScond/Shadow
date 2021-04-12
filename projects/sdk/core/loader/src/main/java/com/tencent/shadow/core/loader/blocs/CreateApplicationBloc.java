package com.tencent.shadow.core.loader.blocs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;

import com.tencent.shadow.core.loader.classloaders.PluginClassLoader;
import com.tencent.shadow.core.loader.exceptions.CreateApplicationException;
import com.tencent.shadow.core.loader.infos.PluginInfo;
import com.tencent.shadow.core.loader.managers.ComponentManager;
import com.tencent.shadow.core.runtime.ShadowAppComponentFactory;
import com.tencent.shadow.core.runtime.ShadowApplication;

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
