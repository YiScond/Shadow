package com.tencent.shadow.core.loader.delegates;

public interface DI {
    void inject(ShadowDelegate delegate, String partKey);
}
