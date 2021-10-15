package mobi.oneway.sd.core.runtime;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;

import java.util.List;

public interface PluginPackageManager {
    ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException;

    ActivityInfo getActivityInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException;

    ServiceInfo getServiceInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException;

    PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException;

    ProviderInfo resolveContentProvider(String name, int flags);

    List<ProviderInfo> queryContentProviders(String processName, int uid, int flags);

    ResolveInfo resolveActivity(Intent intent, int flags);

    ResolveInfo resolveService(Intent intent, int flags);
}
