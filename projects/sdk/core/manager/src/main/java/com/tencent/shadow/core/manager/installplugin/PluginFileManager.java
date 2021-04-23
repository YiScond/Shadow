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

package com.tencent.shadow.core.manager.installplugin;

import android.text.TextUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class PluginFileManager {


    //partkey 中间连接符  例pluginName_pluginHash
    public static final String PARTKEY_INFIX = "_";


    //插件文件根目录
    private final File mShadowDir;

    //shadow目录名称
    private final String SHADOW_DIR_NAME = "ow_shadow";
    //插件目录名称
    private final String PLUGIN_DIR_NAME = "p";
    //插件最大缓存数量
    private final int PLUGIN_MAX_CACHE = 3;

    ;

    public PluginFileManager(File root) {
        mShadowDir = new File(root, SHADOW_DIR_NAME);
        mShadowDir.mkdirs();
    }


    public File getAppDir() {
        return mShadowDir;
    }


    /**
     * 获取插件文件
     *
     * @return
     */
    public File getPluginFile(String partKey) {
        File pluginFile = new File(getPluginDir(), partKey);
        checkPluginCacheCount(pluginFile, partKey);
        return pluginFile;
    }


    /**
     * 删除文件
     *
     * @param file
     */
    private void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (File childFile : childFiles) {
                deleteFile(childFile);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    /**
     * 获取插件目录
     *
     * @return
     */
    private File getPluginDir() {
        File pluginDir = new File(mShadowDir, PLUGIN_DIR_NAME);
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }

        return pluginDir;
    }


    /**
     * 检查插件的缓存数量是否已经超过限制
     * 如果超过限制 删除最老的插件版本
     *
     * @param curPluginFile 当前插件文件
     * @param partKey       插件名称
     */
    private void checkPluginCacheCount(File curPluginFile, final String partKey) {
        //当前插件已缓存 不需要操作
        if (curPluginFile.exists()) {
            return;
        }


        final String pluginName = getPluginName(partKey);

        //过滤出插件的缓存文件
        File[] pluginCacheFiles = getPluginDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(pluginName);
            }
        });

        //排序 获得最老的插件缓存
        if (pluginCacheFiles != null && pluginCacheFiles.length >= PLUGIN_MAX_CACHE) {
            Arrays.sort(pluginCacheFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return (int) (o1.lastModified() - o2.lastModified());
                }
            });

            //获取超出缓存限制数量的总数
            int oldestCount = pluginCacheFiles.length - (PLUGIN_MAX_CACHE - 1);
            for (int i = 0; i < oldestCount; i++) {
                deletePluginFile(pluginCacheFiles[i]);
            }

        }
    }


    /**
     * 删除插件文件
     * 包括对应的libs文件以及odex文件
     *
     * @param pluginFile
     */
    private void deletePluginFile(File pluginFile) {
        String pluginName = pluginFile.getName();
        deleteFile(AppCacheFolderManager.getLibDir(mShadowDir, pluginName));
        deleteFile(AppCacheFolderManager.getODexDir(mShadowDir, pluginName));
        pluginFile.delete();
    }

    /**
     * 根据partKey获取插件名称
     *
     * @param partKey
     * @return
     */
    private String getPluginName(String partKey) {
        String pluginName = "";
        if (!TextUtils.isEmpty(partKey)) {
            String[] s = partKey.split(PARTKEY_INFIX);
            if (s != null && s.length > 0) {
                pluginName = s[0];
            }
        }

        return pluginName;
    }

}
