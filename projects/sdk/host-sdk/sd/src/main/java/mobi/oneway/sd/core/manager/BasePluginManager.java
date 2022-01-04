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
import android.text.TextUtils;

import mobi.oneway.sd.core.common.AndroidLoggerFactory;
import mobi.oneway.sd.core.common.Logger;
import mobi.oneway.sd.core.common.LoggerFactory;
import mobi.oneway.sd.core.manager.installplugin.AppCacheFolderManager;
import mobi.oneway.sd.core.manager.installplugin.CopySoBloc;
import mobi.oneway.sd.core.manager.installplugin.InstallPluginException;
import mobi.oneway.sd.core.manager.installplugin.InstalledPlugin;
import mobi.oneway.sd.core.manager.installplugin.InstalledType;
import mobi.oneway.sd.core.manager.installplugin.ODexBloc;
import mobi.oneway.sd.core.manager.installplugin.SafeZipFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class BasePluginManager {

    private Logger mLogger;
    protected Map<String, String> pluginPathMap;
    /*
     * 宿主的context对象
     */
    public Context mHostContext;


    public BasePluginManager(Context context) {
        LoggerFactory.setILoggerFactory(new AndroidLoggerFactory());
        this.mHostContext = context.getApplicationContext();
        pluginPathMap = new HashMap<>();
        mLogger = LoggerFactory.getLogger(BasePluginManager.class);
    }


    protected InstalledPlugin.Part getPluginPartByPartKey(String partKey) {
        String pluginPath = pluginPathMap.get(partKey);
        if (!TextUtils.isEmpty(pluginPath)) {
            File pluginFile = new File(pluginPath);
            File pluginParentFile = pluginFile.getParentFile();
            File pluginLibFile = AppCacheFolderManager.getLibDir(pluginParentFile, partKey);
            File pluginOdexFile = AppCacheFolderManager.getODexDir(pluginParentFile, partKey);
            if (pluginFile != null && pluginLibFile != null && pluginOdexFile != null) {
                InstalledPlugin.Part part = new InstalledPlugin.PluginPart(InstalledType.TYPE_PLUGIN, partKey, pluginFile, pluginOdexFile, pluginLibFile, null, null);
                return part;
            }
        }
        throw new RuntimeException("没有找到Part partKey:" + partKey);
    }


    /**
     * odex优化
     */
    public final void oDexPlugin(File apkFile) throws InstallPluginException {
        try {
            String pluginName = apkFile.getName();
            File root = apkFile.getParentFile();
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
            File root = apkFile.getParentFile();
            String pluginPreferredAbi = getPluginPreferredAbi(getPluginSupportedAbis(), apkFile);
            if (pluginPreferredAbi.isEmpty()) {
                if (mLogger.isInfoEnabled()) {
                    mLogger.info("插件没有so");
                }
                return;
            }
            String filter = "lib/" + pluginPreferredAbi + "/";
            File soDir = AppCacheFolderManager.getLibDir(root, pluginName);
            if (mLogger.isInfoEnabled()) {
                mLogger.info("extractSo  apkFile=={} soDir=={} filter=={}",
                        apkFile.getAbsolutePath(), soDir.getAbsolutePath(), filter);
            }
            CopySoBloc.copySo(apkFile, soDir
                    , AppCacheFolderManager.getLibCopiedFile(soDir, pluginName), filter);
        } catch (InstallPluginException e) {
            if (mLogger.isErrorEnabled()) {
                mLogger.error("extractSo exception:", e);
            }
            throw e;
        }
    }

    /**
     * 当前插件希望采用的ABI。
     * 子类可以override重新决定。
     *
     * @param pluginSupportedAbis 从getPluginSupportedAbis方法得到的可选ABI列表
     * @param apkFile             插件apk文件
     * @return 最终决定的ABI。插件没有so时返回空字符串。
     * @throws InstallPluginException 读取apk文件失败时抛出
     */
    protected String getPluginPreferredAbi(String[] pluginSupportedAbis, File apkFile)
            throws InstallPluginException {
        ZipFile zipFile;
        try {
            zipFile = new SafeZipFile(apkFile);
        } catch (IOException e) {
            throw new InstallPluginException("读取apk失败，apkFile==" + apkFile, e);
        }

        //找出插件apk中lib目录下都有哪些子目录
        Set<String> subDirsInLib = new LinkedHashSet<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith("lib/")) {
                String[] split = name.split("/");
                if (split.length == 3) {// like "lib/arm64-v8a/libabc.so"
                    subDirsInLib.add(split[1]);
                }
            }
        }

        for (String supportedAbi : pluginSupportedAbis) {
            if (subDirsInLib.contains(supportedAbi)) {
                return supportedAbi;
            }
        }
        return "";
    }


    /**
     * 获取可用的ABI列表。
     * 和Build.SUPPORTED_ABIS的区别是，这是宿主已经决定了当前进程用32位so还是64位so了，
     * 所以可用的ABI只能是其中一部分。
     */
    private String[] getPluginSupportedAbis() {
        String nativeLibraryDir = mHostContext.getApplicationInfo().nativeLibraryDir;
        int nextIndexOfLastSlash = nativeLibraryDir.lastIndexOf('/') + 1;
        String instructionSet = nativeLibraryDir.substring(nextIndexOfLastSlash);
        if (!isKnownInstructionSet(instructionSet)) {
            throw new IllegalStateException("不认识的instructionSet==" + instructionSet);
        }
        boolean is64Bit = is64BitInstructionSet(instructionSet);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return is64Bit ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS;
        } else {
            String cpuAbi = Build.CPU_ABI;
            String cpuAbi2 = Build.CPU_ABI2;
            ArrayList<String> list = new ArrayList<>(2);
            if (cpuAbi != null && !cpuAbi.isEmpty()) {
                list.add(cpuAbi);
            }
            if (cpuAbi2 != null && !cpuAbi2.isEmpty()) {
                list.add(cpuAbi2);
            }
            return list.toArray(new String[0]);
        }
    }

    /**
     * 根据VMRuntime.ABI_TO_INSTRUCTION_SET_MAP
     */
    private static boolean isKnownInstructionSet(String instructionSet) {
        return "arm".equals(instructionSet) ||
                "mips".equals(instructionSet) ||
                "mips64".equals(instructionSet) ||
                "x86".equals(instructionSet) ||
                "x86_64".equals(instructionSet) ||
                "arm64".equals(instructionSet);
    }

    /**
     * Returns whether the given {@code instructionSet} is 64 bits.
     *
     * @param instructionSet a string representing an instruction set.
     * @return true if given {@code instructionSet} is 64 bits, false otherwise.
     * <p>
     * copy from VMRuntime.java
     */
    private static boolean is64BitInstructionSet(String instructionSet) {
        return "arm64".equals(instructionSet) ||
                "x86_64".equals(instructionSet) ||
                "mips64".equals(instructionSet);
    }
}
