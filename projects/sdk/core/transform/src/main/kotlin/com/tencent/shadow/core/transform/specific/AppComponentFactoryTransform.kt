package com.tencent.shadow.core.transform.specific

class AppComponentFactoryTransform : SimpleRenameTransform(
        mapOf(
                "android.app.AppComponentFactory"
                        to "mobi.oneway.sd.core.runtime.ShadowAppComponentFactory",
                "androidx.app.AppComponentFactory"
                        to "mobi.oneway.sd.core.runtime.ShadowAppComponentFactory"
        )
)
