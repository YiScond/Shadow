package com.tencent.shadow.core.loader.blocs;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.load_parameters.LoadParameters;
import com.tencent.shadow.core.loader.classloaders.CombineClassLoader;
import com.tencent.shadow.core.loader.classloaders.PluginClassLoader;
import com.tencent.shadow.core.loader.exceptions.LoadApkException;
import com.tencent.shadow.core.loader.infos.PluginParts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadApkBloc {

    public static PluginClassLoader loadPlugin(InstalledApk installedApk,
                                               LoadParameters loadParameters,
                                               Map<String, PluginParts> pluginPartsMap) throws LoadApkException {
        File apk = new File(installedApk.apkFilePath);
        File odexDir = installedApk.oDexPath == null ? null : new File(installedApk.oDexPath);
        String[] dependsOn = loadParameters.dependsOn;
        //Logger类一定打包在宿主中，所在的classLoader即为加载宿主的classLoader
        ClassLoader hostClassLoader = Logger.class.getClassLoader();
        ClassLoader hostParentClassLoader = hostClassLoader.getParent();
        if (dependsOn == null || dependsOn.length == 0) {
            return new PluginClassLoader(
                    apk.getAbsolutePath(),
                    odexDir,
                    installedApk.libraryPath,
                    hostClassLoader,
                    hostParentClassLoader,
                    loadParameters.hostWhiteList
            );
        } else if (dependsOn.length == 1) {
            String partKey = dependsOn[0];
            PluginParts pluginParts = pluginPartsMap.get(partKey);
            if (pluginParts == null) {
                throw new LoadApkException("加载" + loadParameters.partKey + "时它的依赖" + partKey + "还没有加载");
            } else {
                return new PluginClassLoader(
                        apk.getAbsolutePath(),
                        odexDir,
                        installedApk.libraryPath,
                        pluginParts.getClassLoader(),
                        null,
                        loadParameters.hostWhiteList
                );
            }
        } else {


            List<ClassLoader> dependsOnClassLoaders = new ArrayList<>();

            if (dependsOn != null) {
                for (String itemDepends : dependsOn) {
                    PluginParts pluginParts = pluginPartsMap.get(itemDepends);
                    if (pluginParts == null) {
                        throw new LoadApkException("加载" + loadParameters.partKey + "时它的依赖" + itemDepends + "还没有加载");
                    } else {
                        dependsOnClassLoaders.add(pluginParts.getClassLoader());
                    }
                }
            }

            ClassLoader combineClassLoader = new CombineClassLoader(dependsOnClassLoaders.toArray(new ClassLoader[]{}), hostParentClassLoader);
            return new PluginClassLoader(
                    apk.getAbsolutePath(),
                    odexDir,
                    installedApk.libraryPath,
                    combineClassLoader,
                    null,
                    loadParameters.hostWhiteList
            );
        }
    }
}
