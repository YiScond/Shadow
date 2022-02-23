package mobi.oneway.sd.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Build;

import mobi.oneway.sd.core.runtime.ShadowContext;
import mobi.oneway.sd.helper.util.ReflectUtil;

public class ShadowUtil {

    private static final String field_mPluginClassLoader = "mPluginClassLoader";
    private static final String field_mPluginResources = "mPluginResources";
    private static final String field_mPluginComponentLauncher = "mPluginComponentLauncher";
    private static final String field_mApplicationInfo = "mApplicationInfo";
    private static final String field_mBusinessName = "mBusinessName";
    private static final String field_mPartkey = "mPartKey";
    private static final String field_mShadowApplication = "mShadowApplication";

    private static ClassLoader shadowClassloader;
    private static String shadowBusinessName;
    private static String shadowPartkey;
    private static Resources shadowResources;
    private static ShadowContext.PluginComponentLauncher shadowComponentLauncher;
    private static ApplicationInfo shadowApplicationInfo;
    private static Object shadowApplication;

    /**
     * 获取shadow组件相关信息
     *
     * @param pluginContext
     */
    public static void initShadowInfo(Context pluginContext) {
        try {
            shadowApplication = pluginContext;
            shadowClassloader = ReflectUtil.with(pluginContext)
                    .getFieldValue(field_mPluginClassLoader);
            shadowBusinessName = ReflectUtil.with(pluginContext)
                    .getFieldValue(field_mBusinessName);
            shadowPartkey = ReflectUtil.with(pluginContext)
                    .getFieldValue(field_mPartkey);
            shadowResources = ReflectUtil.with(pluginContext)
                    .getFieldValue(field_mPluginResources);
            shadowComponentLauncher = ReflectUtil.with(pluginContext)
                    .getFieldValue(field_mPluginComponentLauncher);
            shadowApplicationInfo = ReflectUtil.with(pluginContext)
                    .getFieldValue(field_mApplicationInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Activity createActivityWrapper(Object activity) {
        PluginActivityWrapper activityWrapper;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activityWrapper = new PluginActivityWrapperMforMore(activity);
        } else {
            activityWrapper = new PluginActivityWrapper(activity);
        }
        return activityWrapper;
    }

    public static Context createContextWrapper(Context context) {
        PluginContextWrapper pluginContextWrapper = new PluginContextWrapper(context);
        return pluginContextWrapper;
    }

    /**
     * 注入shadow插件信息到包裹类
     *
     * @param o
     */
    public static void injectShadow(Object o) {
        try {
            ReflectUtil.with(o)
                    .setFieldValue(field_mPluginClassLoader, shadowClassloader);
            ReflectUtil.with(o)
                    .setFieldValue(field_mPluginResources, shadowResources);
            ReflectUtil.with(o)
                    .setFieldValue(field_mPluginComponentLauncher, shadowComponentLauncher);
            ReflectUtil.with(o)
                    .setFieldValue(field_mApplicationInfo, shadowApplicationInfo);
            ReflectUtil.with(o)
                    .setFieldValue(field_mBusinessName, shadowBusinessName);
            ReflectUtil.with(o)
                    .setFieldValue(field_mPartkey, shadowPartkey);
            ReflectUtil.with(o)
                    .setFieldValue(field_mShadowApplication, shadowApplication);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
