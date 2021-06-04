package mobi.oneway.sd.core.manager;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import java.util.Map;


public class ShadowPluginManager extends BaseDynamicPluginManager {


    private static ShadowPluginManager instance;


    private ShadowPluginManager(Context context) {
        super(context);
    }

    public static ShadowPluginManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ShadowPluginManager.class) {
                if (instance == null) {
                    instance = new ShadowPluginManager(context);
                }
            }
        }

        return instance;
    }

    /**
     * 加载插件
     *
     * @param partKey
     */
    public void loadPlugin(String partKey, String[] whiteList) throws Exception {

        extractSo(getPluginFile(partKey));
        oDexPlugin(getPluginFile(partKey));

        Map map = dynamicPluginLoader.getLoadedPlugin();
        if (!map.containsKey(partKey)) {
            dynamicPluginLoader.loadPlugin(partKey, whiteList);
        }
        Boolean isCall = (Boolean) map.get(partKey);
        if (isCall == null || !isCall) {
            dynamicPluginLoader.callApplicationOnCreate(partKey);
        }
    }

    public String createPartKey(String pluginName, String pluginHash) {
        return pluginName + "_" + pluginHash;
    }

    /**
     * 获取插件的classloader
     *
     * @param partKey
     * @return
     */
    public ClassLoader getPluginClassloader(String partKey) {
        return dynamicPluginLoader.getPluginClassloader(partKey);
    }

    /**
     * 启动插件Activity
     *
     * @param intent
     */
    public void startPluginActivity(Intent intent) {
        Intent pluginIntent = dynamicPluginLoader.convertActivityIntent(intent);
        pluginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mHostContext.startActivity(pluginIntent);
    }

    public void startPluginService(Intent intent) {
        dynamicPluginLoader.startPluginService(intent);
    }

    public void stopPluginService(Intent intent) {
        dynamicPluginLoader.stopPluginService(intent);
    }

    public void bindPluginService(Intent intent, ServiceConnection connection, int flags) {
        dynamicPluginLoader.bindPluginService(intent, connection, flags);
    }

    public void unBindPluginService(ServiceConnection connection) {
        dynamicPluginLoader.unbindService(connection);
    }

}
