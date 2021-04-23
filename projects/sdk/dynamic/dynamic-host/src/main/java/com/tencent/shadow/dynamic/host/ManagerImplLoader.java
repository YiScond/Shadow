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

package com.tencent.shadow.dynamic.host;

import android.content.Context;

import com.tencent.shadow.core.common.InstalledApk;

import java.io.File;

final class ManagerImplLoader extends ImplLoader {
    private static final String MANAGER_FACTORY_CLASS_NAME = "com.tencent.shadow.dynamic.impl.ManagerFactoryImpl";
    private static final String[] REMOTE_PLUGIN_MANAGER_INTERFACES = new String[]
            {
                    "com.tencent.shadow.core.common",
                    "com.tencent.shadow.dynamic.host"
            };
    final private Context applicationContext;

    ManagerImplLoader(Context context) {
        applicationContext = context.getApplicationContext();
    }

    PluginManagerImpl load() {
        try {
            Class implClass = getClass().getClassLoader().loadClass(MANAGER_FACTORY_CLASS_NAME);
            ManagerFactory managerFactory = (ManagerFactory) implClass.newInstance();
            return managerFactory.buildManager(applicationContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    String[] getCustomWhiteList() {
        return REMOTE_PLUGIN_MANAGER_INTERFACES;
    }
}
