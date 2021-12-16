package mobi.oneway.sd.core.loader.infos;

import java.util.HashSet;
import java.util.Set;


public final class PluginInfo {
    private final Set<PluginActivityInfo> _mActivities;
    private final Set<PluginServiceInfo> _mServices;
    private final Set<PluginProviderInfo> _mProviders;
    private final Set<PluginReceiverInfo> _mReceivers;

    private String appComponentFactory;

    private final String businessName;

    private final String partKey;

    private final String packageName;

    private final String applicationClassName;


    public final Set<PluginActivityInfo> getMActivities() {
        return this._mActivities;
    }


    public final Set<PluginServiceInfo> getMServices() {
        return this._mServices;
    }


    public final Set<PluginProviderInfo> getMProviders() {
        return this._mProviders;
    }

    public final Set<PluginReceiverInfo> getMReceivers() {
        return this._mReceivers;
    }


    public final String getAppComponentFactory() {
        return this.appComponentFactory;
    }

    public final void setAppComponentFactory(String var1) {
        this.appComponentFactory = var1;
    }

    public final void putActivityInfo(PluginActivityInfo pluginActivityInfo) {
        this._mActivities.add(pluginActivityInfo);
    }

    public final void putServiceInfo(PluginServiceInfo pluginServiceInfo) {
        this._mServices.add(pluginServiceInfo);
    }

    public final void putPluginProviderInfo(PluginProviderInfo pluginProviderInfo) {
        this._mProviders.add(pluginProviderInfo);
    }

    public final void putReceiverInfo(PluginReceiverInfo pluginReceiverInfo) {
        _mReceivers.add(pluginReceiverInfo);
    }


    public final String getBusinessName() {
        return this.businessName;
    }


    public final String getPartKey() {
        return this.partKey;
    }


    public final String getPackageName() {
        return this.packageName;
    }


    public final String getApplicationClassName() {
        return this.applicationClassName;
    }

    public PluginInfo(String businessName, String partKey, String packageName, String applicationClassName) {
        super();
        this.businessName = businessName;
        this.partKey = partKey;
        this.packageName = packageName;
        this.applicationClassName = applicationClassName;
        this._mActivities = new HashSet();
        this._mServices = new HashSet();
        this._mProviders = new HashSet();
        this._mReceivers = new HashSet();
    }
}
