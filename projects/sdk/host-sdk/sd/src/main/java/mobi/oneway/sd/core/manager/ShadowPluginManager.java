package mobi.oneway.sd.core.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import java.io.File;
import java.util.Map;
import mobi.oneway.sd.core.loader.managers.ComponentManager;


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
     * @param pluginFile 插件文件
     * @throws Exception
     */
    public void loadPlugin(File pluginFile) throws Exception {
        loadPlugin(pluginFile, null);
    }

    /**
     * 加载插件
     *
     * @param pluginFile 插件文件
     * @param whiteList  宿主类白名单 插件可以使用的宿主类
     * @throws Exception
     */
    public void loadPlugin(File pluginFile, String[] whiteList) throws Exception {
        loadPlugin(pluginFile, whiteList, null);
    }

    /**
     * 加载插件
     *
     * @param pluginFile
     * @param whiteList  宿主类白名单 插件可以使用的宿主类
     * @param dependsOn  被依赖的其他插件
     */
    public void loadPlugin(File pluginFile, String[] whiteList, String[] dependsOn) throws Exception {

        String partKey = pluginFile.getName();
        pluginPathMap.put(partKey, pluginFile.getPath());

        extractSo(pluginFile);
        oDexPlugin(pluginFile);

        Map map = dynamicPluginLoader.getLoadedPlugin();
        if (!map.containsKey(partKey)) {
            dynamicPluginLoader.loadPlugin(partKey, whiteList, dependsOn);
        }
        Boolean isCall = (Boolean) map.get(partKey);
        if (isCall == null || !isCall) {
            dynamicPluginLoader.callApplicationOnCreate(partKey);
        }
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

    public ComponentName startPluginService(Intent intent) {
        return dynamicPluginLoader.startPluginService(intent);
    }

    public boolean stopPluginService(Intent intent) {
        return dynamicPluginLoader.stopPluginService(intent);
    }

    public boolean bindPluginService(Intent intent, ServiceConnection connection, int flags) {
        return dynamicPluginLoader.bindPluginService(intent, connection, flags);
    }

    public void unBindPluginService(ServiceConnection connection) {
        dynamicPluginLoader.unbindService(connection);
    }

    public ComponentManager getComponentManager() {
        return dynamicPluginLoader.getComponentManager();
    }
}
