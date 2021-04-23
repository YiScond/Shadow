package com.tencent.shadow.dynamic.manager;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.dynamic.host.FailedException;
import com.tencent.shadow.dynamic.host.NotFoundException;

public interface UuidManagerImpl {
    InstalledApk getPlugin(String partKey) throws NotFoundException, FailedException;
}
