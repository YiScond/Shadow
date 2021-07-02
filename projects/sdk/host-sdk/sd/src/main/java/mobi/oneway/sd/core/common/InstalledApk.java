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

package mobi.oneway.sd.core.common;


import mobi.oneway.sd.core.load_parameters.LoadParameters;

/**
 * 安装完成的apk
 */
public class InstalledApk {

    public final String apkFilePath;

    public final String oDexPath;

    public final String libraryPath;

    public LoadParameters loadParameters;


    public InstalledApk(String apkFilePath, String oDexPath, String libraryPath, LoadParameters loadParameters) {
        this.apkFilePath = apkFilePath;
        this.oDexPath = oDexPath;
        this.libraryPath = libraryPath;
        this.loadParameters = loadParameters;
    }
}
