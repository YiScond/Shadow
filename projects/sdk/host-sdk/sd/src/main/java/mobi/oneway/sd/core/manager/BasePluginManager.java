/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package mobi.oneway.sd.core.manager;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import mobi.oneway.sd.core.common.Logger;
import mobi.oneway.sd.core.common.LoggerFactory;
import mobi.oneway.sd.core.manager.installplugin.AppCacheFolderManager;
import mobi.oneway.sd.core.manager.installplugin.CopySoBloc;
import mobi.oneway.sd.core.manager.installplugin.InstallPluginException;
import mobi.oneway.sd.core.manager.installplugin.InstalledPlugin;
import mobi.oneway.sd.core.manager.installplugin.InstalledType;
import mobi.oneway.sd.core.manager.installplugin.ODexBloc;
import mobi.oneway.sd.core.manager.installplugin.PluginFileManager;

import java.io.File;

public abstract class BasePluginManager {
    private static final Logger mLogger = LoggerFactory.getLogger(BasePluginManager.class);
    /*
     * 宿主的context对象
     */
    public Context mHostContext;

    /**
     * 插件文件管理器
     */
    private PluginFileManager mPluginFileManager;


    /**
     * UI线程的handler
     */
    protected Handler mUiHandler = new Handler(Looper.getMainLooper());


    public BasePluginManager(Context context) {
        this.mHostContext = context.getApplicationContext();
        this.mPluginFileManager = new PluginFileManager(mHostContext.getFilesDir());
    }


    protected InstalledPlugin.Part getPluginPartByPartKey(String partKey) {
        File pluginFile = mPluginFileManager.getPluginFile(partKey);
        File pluginLibFile = AppCacheFolderManager.getLibDir(mPluginFileManager.getAppDir(), partKey);
        File pluginOdexFile = AppCacheFolderManager.getODexDir(mPluginFileManager.getAppDir(), partKey);
        if (pluginFile != null && pluginLibFile != null && pluginOdexFile != null) {
            InstalledPlugin.Part part = new InstalledPlugin.PluginPart(InstalledType.TYPE_PLUGIN, partKey, pluginFile, pluginOdexFile, pluginLibFile, null, null);
            return part;
        }
        throw new RuntimeException("没有找到Part partKey:" + partKey);
    }


    /**
     * odex优化
     */
    public final void oDexPlugin(File apkFile) throws InstallPluginException {
        try {
            String pluginName = apkFile.getName();
            File root = mPluginFileManager.getAppDir();
            File oDexDir = AppCacheFolderManager.getODexDir(root, pluginName);
            ODexBloc.oDexPlugin(apkFile, oDexDir, AppCacheFolderManager.getODexCopiedFile(oDexDir, pluginName));
        } catch (InstallPluginException e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("oDexPlugin exception:", e);
            }
            throw e;
        }
    }


    /**
     * 插件apk的so解压
     *
     * @param apkFile 插件apk文件
     */
    public final void extractSo(File apkFile) throws InstallPluginException {
        try {
            String pluginName = apkFile.getName();
            File root = mPluginFileManager.getAppDir();
            String filter = "lib/" + getAbi() + "/";
            File soDir = AppCacheFolderManager.getLibDir(root, pluginName);
            CopySoBloc.copySo(apkFile, soDir
                    , AppCacheFolderManager.getLibCopiedFile(soDir, pluginName), filter);
        } catch (InstallPluginException e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("extractSo exception:", e);
            }
            throw e;
        }
    }

    public File getPluginFile(String partKey) {
        return mPluginFileManager.getPluginFile(partKey);
    }


    private boolean deletePart(InstalledPlugin.Part part) {
        boolean suc = true;
        if (!part.pluginFile.delete()) {
            suc = false;
        }
        if (part.oDexDir != null) {
            if (!part.oDexDir.delete()) {
                suc = false;
            }
        }
        if (part.libraryDir != null) {
            if (!part.libraryDir.delete()) {
                suc = false;
            }
        }
        return suc;
    }


    /**
     * 业务插件的abi
     *
     * @return
     */
    private String getAbi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Build.SUPPORTED_ABIS[0];
        } else {
            return Build.CPU_ABI;
        }
    }
}
