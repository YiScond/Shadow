package com.tencent.shadow.core.loader.blocs;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;

import com.tencent.shadow.core.load_parameters.LoadParameters;
import com.tencent.shadow.core.loader.exceptions.ParsePluginApkException;
import com.tencent.shadow.core.loader.infos.PluginActivityInfo;
import com.tencent.shadow.core.loader.infos.PluginInfo;
import com.tencent.shadow.core.loader.infos.PluginProviderInfo;
import com.tencent.shadow.core.loader.infos.PluginServiceInfo;

public class ParsePluginApkBloc {
    public static PluginInfo parse(PackageInfo packageArchiveInfo, LoadParameters loadParameters, Context hostAppContext){
        /*if (!packageArchiveInfo.applicationInfo.packageName.equals(hostAppContext.getPackageName())) {
            *//*
            要求插件和宿主包名一致有两方面原因：
            1.正常的构建过程中，aapt会将包名写入到arsc文件中。插件正常安装运行时，如果以
            android.content.Context.getPackageName为参数传给
            android.content.res.Resources.getIdentifier方法，可以正常获取到资源。但是在插件环境运行时，
            Context.getPackageName会得到宿主的packageName，则getIdentifier方法不能正常获取到资源。为此，
            一个可选的办法是继承Resources，覆盖getIdentifier方法。但是Resources的构造器已经被标记为
            @Deprecated了，未来可能会不可用，因此不首选这个方法。

            2.Android系统，更多情况下是OEM修改的Android系统，会在我们的context上调用getPackageName或者
            getOpPackageName等方法，然后将这个packageName跨进程传递做它用。系统的其他代码会以这个packageName
            去PackageManager中查询权限等信息。如果插件使用自己的包名，就需要在Context的getPackageName等实现中
            new Throwable()，然后判断调用来源以决定返回自己的包名还是插件的包名。但是如果保持采用宿主的包名，则没有
            这个烦恼。

            我们也可以始终认为Shadow App是宿主的扩展代码，使用是宿主的一部分，那么采用宿主的包名就是理所应当的了。
             *//*
            throw new ParsePluginApkException("插件和宿主包名不一致。宿主:" + hostAppContext.getPackageName() + " 插件:" + packageArchiveInfo.applicationInfo.packageName);
        }*/

        /*
        partKey的作用是用来区分一个Component是来自于哪个插件apk的
         */
        String partKey = loadParameters.partKey;

        PluginInfo pluginInfo = new PluginInfo(
                loadParameters.businessName,
                partKey,
                packageArchiveInfo.applicationInfo.packageName,
                packageArchiveInfo.applicationInfo.className
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pluginInfo.setAppComponentFactory(packageArchiveInfo.applicationInfo.appComponentFactory);
        }

        if (packageArchiveInfo.activities != null) {
            for (ActivityInfo activityInfo : packageArchiveInfo.activities) {
                pluginInfo.putActivityInfo(new PluginActivityInfo(activityInfo.name, activityInfo.getThemeResource(), activityInfo));
            }
        }

        if (packageArchiveInfo.services != null) {
            for (ServiceInfo serviceInfo : packageArchiveInfo.services) {
                pluginInfo.putServiceInfo(new PluginServiceInfo(serviceInfo.name));
            }
        }

        if (packageArchiveInfo.providers != null) {
            for (ProviderInfo providerInfo : packageArchiveInfo.providers) {
                pluginInfo.putPluginProviderInfo(new PluginProviderInfo(providerInfo.name, providerInfo.authority, providerInfo));
            }
        }

        return pluginInfo;
    }
}
