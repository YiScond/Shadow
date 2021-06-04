package mobi.oneway.sd.core.loader.managers;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;

import mobi.oneway.sd.core.runtime.PluginPackageManager;

import java.util.ArrayList;
import java.util.List;

public class PluginPackageManagerImpl
        implements PluginPackageManager {

    private PackageManager hostPackageManager;
    private PackageInfo packageInfo;
    private PackageInfo[] allPluginPackageInfo;

    public PluginPackageManagerImpl(PackageManager hostPackageManager, PackageInfo packageInfo, PackageInfo[] allPluginPackageInfo) {
        this.hostPackageManager = hostPackageManager;
        this.packageInfo = packageInfo;
        this.allPluginPackageInfo = allPluginPackageInfo;
    }


    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        if (packageInfo.applicationInfo.packageName.equals(packageName)) {
            return packageInfo.applicationInfo;
        } else {
            return hostPackageManager.getApplicationInfo(packageName, flags);
        }
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException {
        if (component.getPackageName().equals(packageInfo.applicationInfo.packageName)) {
            ActivityInfo pluginActivityInfo = null;
            flag:
            if (allPluginPackageInfo != null) {
                for (PackageInfo packageInfo : allPluginPackageInfo) {
                    if (packageInfo.activities != null) {
                        for (ActivityInfo activityInfo : packageInfo.activities) {
                            if (activityInfo.name.equals(component.getClassName())) {
                                pluginActivityInfo = activityInfo;
                                break flag;
                            }
                        }
                    }

                }
            }

            if (pluginActivityInfo != null) {
                return pluginActivityInfo;
            }
        }

        return hostPackageManager.getActivityInfo(component, flags);
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        if (packageInfo.applicationInfo.packageName.equals(packageName)) {
            return packageInfo;
        } else {
            return hostPackageManager.getPackageInfo(packageName, flags);
        }
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) {
        ProviderInfo pluginProviderInfo = null;
        flag:
        if (allPluginPackageInfo != null) {
            for (PackageInfo packageInfo : allPluginPackageInfo) {
                if (packageInfo.providers != null) {
                    for (ProviderInfo providerInfo : packageInfo.providers) {
                        if (providerInfo.authority.equals(name)) {
                            pluginProviderInfo = providerInfo;
                            break flag;
                        }
                    }
                }
            }
        }

        if (pluginProviderInfo != null) {
            return pluginProviderInfo;
        }

        return hostPackageManager.resolveContentProvider(name, flags);
    }

    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        if (processName == null) {
            List<ProviderInfo> allNormalProviders = hostPackageManager.queryContentProviders(null, 0, flags);
            List<ProviderInfo> pluginProviders = new ArrayList<>();
            if (allPluginPackageInfo != null) {
                for (PackageInfo packageInfo : allPluginPackageInfo) {
                    if (packageInfo.providers != null) {
                        for (ProviderInfo providerInfo : packageInfo.providers) {
                            pluginProviders.add(providerInfo);
                        }
                    }
                }
            }
            if (allNormalProviders != null) {
                allNormalProviders.addAll(pluginProviders);
                return allNormalProviders;
            } else {
                return pluginProviders;
            }
        } else {
            List<ProviderInfo> pluginProviders = new ArrayList<>();
            if (allPluginPackageInfo != null) {
                for (PackageInfo packageInfo : allPluginPackageInfo) {
                    if (packageInfo.applicationInfo.processName.equals(processName) &&
                            packageInfo.applicationInfo.uid == uid &&
                            packageInfo.providers != null) {
                        for (ProviderInfo providerInfo : packageInfo.providers) {
                            pluginProviders.add(providerInfo);
                        }
                    }
                }
            }
            return pluginProviders;
        }
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        ResolveInfo hostResolveInfo = hostPackageManager.resolveActivity(intent, flags);
        if (hostResolveInfo != null && hostResolveInfo.activityInfo == null) {
            ResolveInfo resolveInfo = new ResolveInfo();
            flag:
            if (allPluginPackageInfo != null) {
                for (PackageInfo packageInfo : allPluginPackageInfo) {
                    if (packageInfo.activities != null) {
                        for (ActivityInfo activityInfo : packageInfo.activities) {
                            ComponentName componentName = intent.getComponent();
                            if (componentName != null && activityInfo.name.equals(intent.getComponent().getClassName())) {
                                resolveInfo.activityInfo = activityInfo;
                                break flag;
                            }
                        }
                    }
                }
            }
            return resolveInfo;
        } else {
            return hostResolveInfo;
        }

    }
}
