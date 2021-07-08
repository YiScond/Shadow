package mobi.oneway.sd.core.loader.managers;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;

import mobi.oneway.sd.core.loader.infos.ContainerProviderInfo;
import mobi.oneway.sd.core.loader.infos.PluginActivityInfo;
import mobi.oneway.sd.core.loader.infos.PluginComponentInfo;
import mobi.oneway.sd.core.loader.infos.PluginInfo;
import mobi.oneway.sd.core.loader.infos.PluginProviderInfo;
import mobi.oneway.sd.core.loader.infos.PluginServiceInfo;
import mobi.oneway.sd.core.runtime.ShadowContext;
import mobi.oneway.sd.core.runtime.ShadowContext.PluginComponentLauncher;
import mobi.oneway.sd.core.runtime.container.DelegateProviderHolder;
import mobi.oneway.sd.core.runtime.container.GeneratedHostActivityDelegator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ComponentManager implements PluginComponentLauncher {
    private final Map<String, String> packageNameMap = new HashMap();
    private final Map<ComponentName, ComponentName> componentMap = new HashMap();
    private final Map<ComponentName, PluginInfo> pluginInfoMap = new HashMap<>();
    private final Map<ComponentName, PluginComponentInfo> pluginComponentInfoMap = new HashMap<>();
    private Map application2broadcastInfo;
    private PluginServiceManager mPluginServiceManager;
    private PluginContentProviderManager mPluginContentProviderManager;

    public static final String CM_LOADER_BUNDLE_KEY = "CM_LOADER_BUNDLE";

    public static final String CM_EXTRAS_BUNDLE_KEY = "CM_EXTRAS_BUNDLE";

    public static final String CM_ACTIVITY_INFO_KEY = "CM_ACTIVITY_INFO";

    public static final String CM_CLASS_NAME_KEY = "CM_CLASS_NAME";

    public static final String CM_CALLING_ACTIVITY_KEY = "CM_CALLING_ACTIVITY_KEY";

    public static final String CM_PACKAGE_NAME_KEY = "CM_PACKAGE_NAME";

    public static final String CM_BUSINESS_NAME_KEY = "CM_BUSINESS_NAME";

    public static final String CM_PART_KEY = "CM_PART";
    public static final ComponentManager.Companion Companion = new Companion();


    public abstract ComponentName onBindContainerActivity(ComponentName var1);


    public abstract ContainerProviderInfo onBindContainerContentProvider(ComponentName var1);


    public abstract List getBroadcastInfoList(String var1);

    public boolean startActivity(ShadowContext shadowContext, Intent pluginIntent, Bundle option) {
        boolean var10000;
        if (this.isPluginComponent(pluginIntent)) {
            shadowContext.superStartActivity(this.toActivityContainerIntent(pluginIntent), option);
            var10000 = true;
        } else {
            var10000 = false;
        }

        return var10000;
    }

    public boolean startActivityForResult(GeneratedHostActivityDelegator delegator, Intent pluginIntent, int requestCode, Bundle option, ComponentName callingActivity) {
        boolean var10000;
        if (this.isPluginComponent(pluginIntent)) {
            Intent containerIntent = this.toActivityContainerIntent(pluginIntent);
            containerIntent.putExtra("CM_CALLING_ACTIVITY_KEY", (Parcelable) callingActivity);
            delegator.startActivityForResult(containerIntent, requestCode, option);
            var10000 = true;
        } else {
            var10000 = false;
        }

        return var10000;
    }


    public Pair<Boolean, ComponentName> startService(ShadowContext context, Intent service) {
        if (this.isPluginComponent(service)) {
            PluginServiceManager var10000 = this.mPluginServiceManager;
            ComponentName component = var10000.startPluginService(service);
            if (component != null) {
                return new Pair(true, component);
            }
        }

        return new Pair(false, service.getComponent());
    }


    public Pair<Boolean, Boolean> stopService(ShadowContext context, Intent intent) {
        if (this.isPluginComponent(intent)) {
            PluginServiceManager var10000 = this.mPluginServiceManager;
            boolean stopped = var10000.stopPluginService(intent);
            return new Pair(true, stopped);
        } else {
            return new Pair(false, true);
        }
    }


    public Pair<Boolean, Boolean> bindService(ShadowContext context, Intent intent, ServiceConnection conn, int flags) {
        Pair var5;
        if (this.isPluginComponent(intent)) {
            PluginServiceManager var10000 = this.mPluginServiceManager;
            var10000.bindPluginService(intent, conn, flags);
            var5 = new Pair(true, true);
        } else {
            var5 = new Pair(false, false);
        }

        return var5;
    }


    public Pair<Boolean, Object> unbindService(ShadowContext context, ServiceConnection conn) {
        return Pair.create(
                mPluginServiceManager.unbindPluginService(conn).first,
                null
        );
    }


    public Intent convertPluginActivityIntent(Intent pluginIntent) {
        return this.isPluginComponent(pluginIntent) ? this.toActivityContainerIntent(pluginIntent) : pluginIntent;
    }

    public final void addPluginApkInfo(final PluginInfo pluginInfo) {
        Set<PluginActivityInfo> pluginActivityInfos = pluginInfo.getMActivities();
        if (pluginActivityInfos != null) {
            Iterator<PluginActivityInfo> iterator = pluginActivityInfos.iterator();
            while (iterator.hasNext()) {
                PluginActivityInfo info = iterator.next();
                ComponentName componentName = new ComponentName(pluginInfo.getPackageName(), info.getClassName());
                common(info, pluginInfo, componentName);
                componentMap.put(componentName, onBindContainerActivity(componentName));
            }
        }

        Set<PluginServiceInfo> pluginServiceInfos = pluginInfo.getMServices();
        if (pluginServiceInfos != null) {
            Iterator<PluginServiceInfo> iterator = pluginServiceInfos.iterator();
            while (iterator.hasNext()) {
                PluginServiceInfo info = iterator.next();
                ComponentName componentName = new ComponentName(pluginInfo.getPackageName(), info.getClassName());
                common(info, pluginInfo, componentName);
            }
        }

        Set<PluginProviderInfo> pluginProviderInfos = pluginInfo.getMProviders();
        if (pluginProviderInfos != null) {
            Iterator<PluginProviderInfo> iterator = pluginProviderInfos.iterator();
            while (iterator.hasNext()) {
                PluginProviderInfo info = iterator.next();
                ComponentName componentName = new ComponentName(pluginInfo.getPackageName(), info.getClassName());
                mPluginContentProviderManager.addContentProviderInfo(pluginInfo.getPartKey(), info, onBindContainerContentProvider(componentName));
            }
        }

    }

    private void common(PluginComponentInfo pluginComponentInfo, PluginInfo pluginInfo, ComponentName componentName) {
        packageNameMap.put(pluginComponentInfo.getClassName(), pluginInfo.getPackageName());
        if (pluginInfoMap.containsKey(componentName)) {
            throw new IllegalStateException("重复添加Component：$componentName");
        } else {
            pluginInfoMap.put(componentName, pluginInfo);
        }
        pluginComponentInfoMap.put(componentName, pluginComponentInfo);
    }


    public final String getComponentBusinessName(ComponentName componentName) {
        PluginInfo pluginInfo = getPluginInfo(componentName);
        return pluginInfo != null ? pluginInfo.getBusinessName() : null;
    }


    public final String getComponentPartKey(ComponentName componentName) {
        PluginInfo pluginInfo = getPluginInfo(componentName);
        return pluginInfo != null ? pluginInfo.getPartKey() : null;
    }

    public final void setPluginServiceManager(PluginServiceManager pluginServiceManager) {
        this.mPluginServiceManager = pluginServiceManager;
    }

    public final void setPluginContentProviderManager(PluginContentProviderManager pluginContentProviderManager) {
        this.mPluginContentProviderManager = pluginContentProviderManager;
    }

    public final boolean isPluginComponent(Intent $this$isPluginComponent) {
        ComponentName var10000 = $this$isPluginComponent.getComponent();
        if (var10000 != null) {
            ComponentName component = var10000;
            String var4 = component.getClassName();
            String className = var4;
            return this.packageNameMap.containsKey(className);
        } else {
            return false;
        }
    }

    private final Intent toActivityContainerIntent(Intent toActivityContainerIntent) {
        Bundle bundleForPluginLoader = new Bundle();
        ComponentName pluginComponentName = toActivityContainerIntent.getComponent();
        PluginComponentInfo pluginComponentInfo = getPluginComponentInfo(pluginComponentName);
        bundleForPluginLoader.putParcelable("CM_ACTIVITY_INFO", pluginComponentInfo);
        return this.toContainerIntent(toActivityContainerIntent, bundleForPluginLoader);
    }

    private final Intent toContainerIntent(Intent toContainerIntent, Bundle bundleForPluginLoader) {
        ComponentName component = toContainerIntent.getComponent();
        String className = component.getClassName();
        String packageName = this.packageNameMap.get(className);
        toContainerIntent.setComponent(new ComponentName(packageName, className));
        ComponentName containerComponent = getContainerComponent(component);
        PluginInfo pluginInfo = getPluginInfo(component);
        String businessName = pluginInfo.getBusinessName();
        String partKey = pluginInfo.getPartKey();
        Bundle pluginExtras = toContainerIntent.getExtras();
        toContainerIntent.replaceExtras((Bundle) null);
        Intent containerIntent = new Intent(toContainerIntent);
        containerIntent.setComponent(containerComponent);
        bundleForPluginLoader.putString("CM_CLASS_NAME", className);
        bundleForPluginLoader.putString("CM_PACKAGE_NAME", packageName);
        containerIntent.putExtra("CM_EXTRAS_BUNDLE", pluginExtras);
        containerIntent.putExtra("CM_BUSINESS_NAME", businessName);
        containerIntent.putExtra("CM_PART", partKey);
        containerIntent.putExtra("CM_LOADER_BUNDLE", bundleForPluginLoader);
        containerIntent.putExtra("LOADER_VERSION", "local");
        containerIntent.putExtra("PROCESS_ID_KEY", DelegateProviderHolder.sCustomPid);
        return containerIntent;
    }


    public final Map getBroadcastsByPartKey(String partKey) {
        Object var10000;
        if (this.application2broadcastInfo.get(partKey) == null) {
            this.application2broadcastInfo.put(partKey, new HashMap());
            List broadcastInfoList = this.getBroadcastInfoList(partKey);
            if (broadcastInfoList != null) {
                Iterator var4 = broadcastInfoList.iterator();

                while (var4.hasNext()) {
                    BroadcastInfo broadcastInfo = (BroadcastInfo) var4.next();
                    var10000 = this.application2broadcastInfo.get(partKey);
                    ((Map) var10000).put(broadcastInfo.getClassName(), toList(broadcastInfo.getActions()));
                }
            }
        }

        var10000 = this.application2broadcastInfo.get(partKey);
        return (Map) var10000;
    }

    public ComponentManager() {
        boolean var1 = false;
        this.application2broadcastInfo = (Map) (new HashMap());
    }


    /**
     * 获取插件componentName 对应宿主壳Activity的componentName
     *
     * @param pluginComponentName
     * @return
     */
    private ComponentName getContainerComponent(ComponentName pluginComponentName) {
        ComponentName containerComponent = componentMap.get(pluginComponentName);
        //当插件包名和宿主包名不一致的时候
        if (containerComponent == null) {
            String pluginActivityClassName = pluginComponentName.getClassName();
            for (Map.Entry<ComponentName, ComponentName> entry : componentMap.entrySet()) {
                if (entry.getKey().getClassName().equals(pluginActivityClassName)) {
                    containerComponent = entry.getValue();
                    break;
                }
            }
        }

        return containerComponent;
    }

    /**
     * 获取插件componentName 对应插件的pluginInfo 插件信息
     *
     * @param pluginComponentName
     * @return
     */
    private PluginInfo getPluginInfo(ComponentName pluginComponentName) {
        PluginInfo pluginInfo = pluginInfoMap.get(pluginComponentName);
        //当插件包名和宿主包名不一致的时候
        if (pluginInfo == null) {
            String pluginActivityClassName = pluginComponentName.getClassName();
            for (Map.Entry<ComponentName, PluginInfo> entry : pluginInfoMap.entrySet()) {
                if (entry.getKey().getClassName().equals(pluginActivityClassName)) {
                    pluginInfo = entry.getValue();
                    break;
                }
            }
        }
        return pluginInfo;
    }

    /**
     * 获取插件componentName 对应插件的PluginComponentInfo 插件信息
     *
     * @param pluginComponentName
     * @return
     */
    private PluginComponentInfo getPluginComponentInfo(ComponentName pluginComponentName) {
        PluginComponentInfo pluginComponentInfo = pluginComponentInfoMap.get(pluginComponentName);
        //当插件包名和宿主包名不一致的时候
        if (pluginComponentInfo == null) {
            String pluginActivityClassName = pluginComponentName.getClassName();
            for (Map.Entry<ComponentName, PluginComponentInfo> entry : pluginComponentInfoMap.entrySet()) {
                if (entry.getKey().getClassName().equals(pluginActivityClassName)) {
                    pluginComponentInfo = entry.getValue();
                }
            }
        }
        return pluginComponentInfo;
    }

    public static final class BroadcastInfo {

        private final String className;

        private final String[] actions;


        public final String getClassName() {
            return this.className;
        }


        public final String[] getActions() {
            return this.actions;
        }

        public BroadcastInfo(String className, String[] actions) {
            super();
            this.className = className;
            this.actions = actions;
        }
    }


    public static final class Companion {
        private Companion() {
        }
    }

    private <T> List<T> toList(T[] arrays) {
        List<T> result = new ArrayList<>();
        if (arrays != null && arrays.length > 0) {
            for (T t : arrays) {
                result.add(t);
            }
        }

        return result;
    }
}
