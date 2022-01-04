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

package mobi.oneway.sd.core.manager.installplugin;

import java.io.File;

/**
 * 目录各模块的目录关系管理
 */
public class AppCacheFolderManager {

    public static File getAppDir(File root, String appName) {
        return new File(root, appName);
    }


    public static File getODexDir(File root, String key) {
        return new File(root, key + "_odex");
    }

    public static File getODexCopiedFile(File oDexDir, String key) {
        return new File(oDexDir, key + "_oDexed");
    }

    public static File getLibDir(File root, String key) {
        return new File(root, key + "_lib");
    }

    public static File getLibCopiedFile(File soDir, String key) {
        //更新写入成功标识文件,新的so识别机制防止上一版插件存在导致没复制到正确位数的so文件
        return new File(soDir, key + "_copied_2");
    }

}
