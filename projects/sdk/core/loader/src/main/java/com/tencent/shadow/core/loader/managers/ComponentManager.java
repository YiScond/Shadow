package com.tencent.shadow.core.loader.managers;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;

import com.tencent.shadow.core.loader.infos.ContainerProviderInfo;
import com.tencent.shadow.core.loader.infos.PluginActivityInfo;
import com.tencent.shadow.core.loader.infos.PluginComponentInfo;
import com.tencent.shadow.core.loader.infos.PluginInfo;
import com.tencent.shadow.core.loader.infos.PluginProviderInfo;
import com.tencent.shadow.core.loader.infos.PluginServiceInfo;
import com.tencent.shadow.core.runtime.ShadowContext;
import com.tencent.shadow.core.runtime.ShadowContext.PluginComponentLauncher;
import com.tencent.shadow.core.runtime.container.DelegateProviderHolder;
import com.tencent.shadow.core.runtime.container.GeneratedHostActivityDelegator;

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
    public static final ComponentManager.Companion Companion = new ComponentManager.Companion();


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


    public Pair startService(ShadowContext context, Intent service) {
        if (this.isPluginComponent(service)) {
            PluginServiceManager var10000 = this.mPluginServiceManager;
            ComponentName component = var10000.startPluginService(service);
            if (component != null) {
                return new Pair(true, component);
            }
        }

        return new Pair(false, service.getComponent());
    }


    public Pair stopService(ShadowContext context, Intent intent) {
        if (this.isPluginComponent(intent)) {
            PluginServiceManager var10000 = this.mPluginServiceManager;
            boolean stopped = var10000.stopPluginService(intent);
            return new Pair(true, stopped);
        } else {
            return new Pair(false, true);
        }
    }


    public Pair bindService(ShadowContext context, Intent intent, ServiceConnection conn, int flags) {
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


    public Pair unbindService(ShadowContext context, ServiceConnection conn) {
        PluginServiceManager var10000 = this.mPluginServiceManager;
        var10000.unbindPluginService(conn);
        return new Pair(true, null);
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
        PluginInfo var10000 = (PluginInfo) this.pluginInfoMap.get(componentName);
        return var10000 != null ? var10000.getBusinessName() : null;
    }


    public final String getComponentPartKey(ComponentName componentName) {
        PluginInfo var10000 = (PluginInfo) this.pluginInfoMap.get(componentName);
        return var10000 != null ? var10000.getPartKey() : null;
    }

    public final void setPluginServiceManager(PluginServiceManager pluginServiceManager) {
        this.mPluginServiceManager = pluginServiceManager;
    }

    public final void setPluginContentProviderManager(PluginContentProviderManager pluginContentProviderManager) {
        this.mPluginContentProviderManager = pluginContentProviderManager;
    }

    private final boolean isPluginComponent(Intent $this$isPluginComponent) {
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

    private final Intent toActivityContainerIntent(Intent $this$toActivityContainerIntent) {
        Bundle bundleForPluginLoader = new Bundle();
        Map var4 = this.pluginComponentInfoMap;
        ComponentName var5 = $this$toActivityContainerIntent.getComponent();
        boolean var6 = false;
        Object var10000 = var4.get(var5);
        PluginComponentInfo pluginComponentInfo = (PluginComponentInfo) var10000;
        bundleForPluginLoader.putParcelable("CM_ACTIVITY_INFO", (Parcelable) pluginComponentInfo);
        return this.toContainerIntent($this$toActivityContainerIntent, bundleForPluginLoader);
    }

    private final Intent toContainerIntent(Intent $this$toContainerIntent, Bundle bundleForPluginLoader) {
        ComponentName var10000 = $this$toContainerIntent.getComponent();
        ComponentName component = var10000;
        String var11 = component.getClassName();
        String className = var11;
        Object var12 = this.packageNameMap.get(className);
        String packageName = (String) var12;
        $this$toContainerIntent.setComponent(new ComponentName(packageName, className));
        var12 = this.componentMap.get(component);
        ComponentName containerComponent = (ComponentName) var12;
        var12 = this.pluginInfoMap.get(component);
        String businessName = ((PluginInfo) var12).getBusinessName();
        var12 = this.pluginInfoMap.get(component);
        String partKey = ((PluginInfo) var12).getPartKey();
        Bundle pluginExtras = $this$toContainerIntent.getExtras();
        $this$toContainerIntent.replaceExtras((Bundle) null);
        Intent containerIntent = new Intent($this$toContainerIntent);
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
                    ComponentManager.BroadcastInfo broadcastInfo = (ComponentManager.BroadcastInfo) var4.next();
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
