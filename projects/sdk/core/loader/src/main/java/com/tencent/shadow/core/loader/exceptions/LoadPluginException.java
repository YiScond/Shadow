package com.tencent.shadow.core.loader.exceptions;

import android.annotation.TargetApi;
import android.os.Build;

public class LoadPluginException extends Exception {

    public LoadPluginException() {
    }

    public LoadPluginException(String message) {
        super(message);
    }

    public LoadPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadPluginException(Throwable cause) {
        super(cause);
    }

    @TargetApi(24)
    public LoadPluginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
