package com.tencent.shadow.dynamic.loader.impl;

import android.content.Context;

import com.tencent.shadow.core.loader.ShadowPluginLoader;

public interface CoreLoaderFactory {
    ShadowPluginLoader build(Context var1);
}