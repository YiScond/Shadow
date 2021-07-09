package mobi.oneway.sd.dynamic.loader.impl;

import android.content.ComponentName;
import android.content.Context;

import mobi.oneway.sd.core.loader.infos.ContainerProviderInfo;
import mobi.oneway.sd.core.loader.managers.ComponentManager;
import mobi.oneway.sd.core.runtime.proxy.activity.PluginDefaultProxyActivity;
import mobi.oneway.sd.core.runtime.proxy.activity.PluginSingleInstance1ProxyActivity;
import mobi.oneway.sd.core.runtime.proxy.activity.PluginSingleTask1ProxyActivity;

import java.util.ArrayList;
import java.util.List;

public class CustomComponentManager extends ComponentManager {

    /**
     * sample-runtime 模块中定义的壳子Activity，需要在宿主AndroidManifest.xml注册
     */
    private static final String DEFAULT_ACTIVITY = PluginDefaultProxyActivity.class.getName();
    private static final String SINGLE_INSTANCE_ACTIVITY = PluginSingleInstance1ProxyActivity.class.getName();
    private static final String SINGLE_TASK_ACTIVITY = PluginSingleTask1ProxyActivity.class.getName();

    private Context context;

    public CustomComponentManager(Context context) {
        this.context = context;
    }


    /**
     * 配置插件Activity 到 壳子Activity的对应关系
     *
     * @param pluginActivity 插件Activity
     * @return 壳子Activity
     */
    @Override
    public ComponentName onBindContainerActivity(ComponentName pluginActivity) {
        switch (pluginActivity.getClassName()) {
            /**
             * 这里配置对应的对应关系
             */
        }
        return new ComponentName(context, DEFAULT_ACTIVITY);
    }

    /**
     * 配置对应宿主中预注册的壳子contentProvider的信息
     */
    @Override
    public ContainerProviderInfo onBindContainerContentProvider(ComponentName pluginContentProvider) {
        return new ContainerProviderInfo(
                mobi.oneway.sd.core.runtime.container.PluginContainerContentProvider.class.getName(),
                context.getPackageName() + "ow.contentprovider.authority.dynamic");
    }

    @Override
    public List<BroadcastInfo> getBroadcastInfoList(String partKey) {
        List<BroadcastInfo> broadcastInfos = new ArrayList<>();

        //如果有静态广播需要像下面代码这样注册
//        if (partKey.equals(Constant.PART_KEY_PLUGIN_MAIN_APP)) {
//            broadcastInfos.add(
//                    new ComponentManager.BroadcastInfo(
//                            "mobi.oneway.sd.demo.usecases.receiver.MyReceiver",
//                            new String[]{"com.tencent.test.action"}
//                    )
//            );
//        }
        return broadcastInfos;
    }

}
