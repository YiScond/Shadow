package mobi.oneway.sd.helper;

import android.content.res.Resources;

import mobi.oneway.sd.core.runtime.ResourcesWrapper;

public class PluginResourceWrapper extends ResourcesWrapper {

    private String pluginPackageName;

    public PluginResourceWrapper(Resources base, String pluginPackageName) {
        super(base);
        this.pluginPackageName = pluginPackageName;
    }

    /**
     * 从宿主包名加载不到 再到插件包名进行加载
     *
     * @param name
     * @param defType
     * @param defPackage 默认是宿主包名
     * @return
     */
    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        int id = super.getIdentifier(name, defType, defPackage);
        if (id == 0) {
            id = super.getIdentifier(name, defType, pluginPackageName);
        }
        return id;
    }
}
