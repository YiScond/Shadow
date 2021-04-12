package com.tencent.shadow.dynamic.loader.impl;

import android.content.Context;
import com.tencent.shadow.dynamic.host.LoaderFactory;
import com.tencent.shadow.dynamic.host.PluginLoaderImpl;


public class LoaderFactoryImpl implements LoaderFactory {
    
    public PluginLoaderImpl buildLoader( String p0,  Context p2) {
        return new PluginLoaderBinder(new DynamicPluginLoader(p2, p0));
    }
}
