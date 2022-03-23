package mobi.oneway.sd.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.oneway.sd.core.runtime.ShadowContext;
import mobi.oneway.sd.helper.util.ReflectUtil;

public class ShadowUtil {

    private static final String SUFFIX_APK = ".apk";

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

    /**
     * 获取插件的路径
     * 通过报错调用栈间接得出
     *
     * @return
     */
    public static String getPluginApkPath() {
        String pluginApkPath = null;

        //产生一个so加载的报错,报错信息含有插件的路径 通过正则表达式将其提取出来
        try {
            System.loadLibrary("shadow_no_exist_lib");
        } catch (Throwable e) {
            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(e.getMessage());
            while (m.find()) {
                String group = m.group().replace("\"", "");
                if (group.endsWith(SUFFIX_APK)) {
                    pluginApkPath = group;
                    break;
                }
            }
        }

        return pluginApkPath;
    }

    /**
     * 通过路径获取插件名
     *
     * @param pluginPath
     * @return
     */
    public static String getPluginName(String pluginPath) {
        String pluginName = null;
        String[] splitArray = pluginPath.split("/");
        if (splitArray != null && splitArray.length > 0) {
            String lastIndexStr = splitArray[splitArray.length - 1];
            if (lastIndexStr.contains(SUFFIX_APK)) {
                pluginName = lastIndexStr;
            }
        }
        return pluginName;
    }


    /**
     * 增加shadow的pluginClassLoader白名单
     */
    public static void appendPluginClassLoaderWhiteList(String[] appendWhiteList) {
        try {
            ClassLoader pluginClassLoader = ShadowUtil.class.getClassLoader();
            Class loaderClass = pluginClassLoader.getClass();
            Field[] fields = loaderClass.getDeclaredFields();
            for (Field itemField : fields) {
                itemField.setAccessible(true);
                Class fieldType = itemField.getType();
                if (fieldType.isArray()) {
                    String[] whiteList = (String[]) itemField.get(pluginClassLoader);
                    String[] combineWhite = combineWhiteList(whiteList, appendWhiteList);
                    itemField.set(pluginClassLoader, combineWhite);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] combineWhiteList(String[] oriWhiteList, String[] appendWhiteList) {
        String[] rWhiteList = new String[oriWhiteList.length + appendWhiteList.length];
        // 合并两个数组
        System.arraycopy(oriWhiteList, 0, rWhiteList, 0, oriWhiteList.length);
        System.arraycopy(appendWhiteList, 0, rWhiteList, oriWhiteList.length, appendWhiteList.length);
        return rWhiteList;
    }
}
