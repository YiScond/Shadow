package mobi.oneway.sd.core.manager;

import android.content.Context;

import mobi.oneway.sd.core.common.InstalledApk;
import mobi.oneway.sd.core.common.Logger;
import mobi.oneway.sd.core.common.LoggerFactory;
import mobi.oneway.sd.core.load_parameters.LoadParameters;
import mobi.oneway.sd.core.manager.installplugin.InstalledPlugin;
import mobi.oneway.sd.dynamic.host.FailedException;
import mobi.oneway.sd.dynamic.host.NotFoundException;
import mobi.oneway.sd.dynamic.host.PluginManager;
import mobi.oneway.sd.dynamic.loader.impl.DynamicPluginLoader;


abstract public class BaseDynamicPluginManager extends BasePluginManager implements PluginManager {
    private static final Logger mLogger = LoggerFactory.getLogger(BaseDynamicPluginManager.class);

    /**
     * 插件加载器
     */
    protected DynamicPluginLoader dynamicPluginLoader;

    public BaseDynamicPluginManager(Context context) {
        super(context);
        //初始化插件加载器
        dynamicPluginLoader = new DynamicPluginLoader(context);
        dynamicPluginLoader.setPluginManager(this);
    }

    @Override
    public InstalledApk getPlugin(String partKey, String[] hostWhiteList) throws FailedException, NotFoundException {
        try {
            InstalledPlugin.Part part;
            try {
                part = getPluginPartByPartKey(partKey);
            } catch (RuntimeException e) {
                throw new NotFoundException("partKey==" + partKey + "的Plugin找不到");
            }
            String businessName = part instanceof InstalledPlugin.PluginPart ? ((InstalledPlugin.PluginPart) part).businessName : null;
            String[] dependsOn = part instanceof InstalledPlugin.PluginPart ? ((InstalledPlugin.PluginPart) part).dependsOn : null;
            LoadParameters loadParameters
                    = new LoadParameters(businessName, partKey, dependsOn, hostWhiteList);


            return new InstalledApk(
                    part.pluginFile.getAbsolutePath(),
                    part.oDexDir == null ? null : part.oDexDir.getAbsolutePath(),
                    part.libraryDir == null ? null : part.libraryDir.getAbsolutePath(),
                    loadParameters
            );
        } catch (RuntimeException e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("getPlugin exception:", e);
            }
            throw new FailedException(e);
        }
    }

}
