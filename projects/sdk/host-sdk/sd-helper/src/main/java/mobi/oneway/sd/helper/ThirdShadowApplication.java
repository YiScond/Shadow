package mobi.oneway.sd.helper;

import android.app.Application;

/**
 * 第三方的shadow application 其他module的application都需要继承此类
 */
public class ThirdShadowApplication extends Application {

    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        ShadowUtil.initShadowInfo(application);
    }

    public static Application getShadowApplication() {
        return application;
    }
}
