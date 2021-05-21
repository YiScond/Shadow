package com.tencent.shadow.dynamic.manager;

import android.content.Context;
import android.os.Parcel;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.core.load_parameters.LoadParameters;
import com.tencent.shadow.core.manager.BasePluginManager;
import com.tencent.shadow.core.manager.installplugin.InstalledPlugin;
import com.tencent.shadow.dynamic.host.FailedException;
import com.tencent.shadow.dynamic.host.NotFoundException;
import com.tencent.shadow.dynamic.host.UuidManager;
import com.tencent.shadow.dynamic.loader.impl.DynamicPluginLoader;



abstract public class BaseDynamicPluginManager extends BasePluginManager implements UuidManager {
    private static final Logger mLogger = LoggerFactory.getLogger(BaseDynamicPluginManager.class);

    /**
     * 插件加载器
     */
    protected DynamicPluginLoader dynamicPluginLoader;

    public BaseDynamicPluginManager(Context context) {
        super(context);
        //初始化插件加载器
        dynamicPluginLoader = new DynamicPluginLoader(context);
        dynamicPluginLoader.setUuidManager(this);
    }

    @Override
    public InstalledApk getPlugin(String partKey,String[] hostWhiteList) throws FailedException, NotFoundException {
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
