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

import android.app.Application;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.tencent.shadow.core.common.InstalledApk;

import java.io.File;

import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_FILE_NOT_FOUND_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_RELOAD_LOADER_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_RELOAD_RUNTIME_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_RESET_UUID_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_RUNTIME_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_UUID_MANAGER_DEAD_EXCEPTION;
import static com.tencent.shadow.dynamic.host.FailedException.ERROR_CODE_UUID_MANAGER_NULL_EXCEPTION;


public class PluginProcessService extends BasePluginProcessService {

    private final PpsBinder mPpsControllerBinder = new PpsBinder(this);

    static final ActivityHolder sActivityHolder = new ActivityHolder();

    public static Application.ActivityLifecycleCallbacks getActivityHolder() {
        return sActivityHolder;
    }

    public static PpsController wrapBinder(IBinder ppsBinder) {
        return new PpsController(ppsBinder);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("onBind:" + this);
        }
        return mPpsControllerBinder;
    }

    private UuidManager mUuidManager;

    private PluginLoaderImpl mPluginLoader;

    private boolean mRuntimeLoaded = false;


    private void checkUuidManagerNotNull() throws FailedException {
        if (mUuidManager == null) {
            throw new FailedException(ERROR_CODE_UUID_MANAGER_NULL_EXCEPTION, "mUuidManager == null");
        }
    }

    void setUuidManager(UuidManager uuidManager) {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("setUuidManager uuidManager==" + uuidManager);
        }
        mUuidManager = uuidManager;
        if (mPluginLoader != null) {
            if (mLogger.isInfoEnabled()) {
                mLogger.info("更新mPluginLoader的uuidManager");
            }
            mPluginLoader.setUuidManager(uuidManager);
        }
    }

    void exit() {
        if (mLogger.isInfoEnabled()) {
            mLogger.info("exit ");
        }
        PluginProcessService.sActivityHolder.finishAll();
        System.exit(0);
        try {
            wait();
        } catch (InterruptedException ignored) {
        }
    }

    PpsStatus getPpsStatus() {
        return new PpsStatus(mRuntimeLoaded, mPluginLoader != null, mUuidManager != null);
    }

    IBinder getPluginLoader() throws FailedException {
        checkUuidManagerNotNull();
        if (mPluginLoader == null) {
            PluginLoaderImpl pluginLoader = null;
            try {
                pluginLoader = new LoaderImplLoader().load(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            pluginLoader.setUuidManager(mUuidManager);
            mPluginLoader = pluginLoader;
        }
        return mPluginLoader;
    }
}
