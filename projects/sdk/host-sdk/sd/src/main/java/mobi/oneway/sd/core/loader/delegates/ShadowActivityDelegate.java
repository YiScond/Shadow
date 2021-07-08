package mobi.oneway.sd.core.loader.delegates;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.WindowManager;

import mobi.oneway.sd.core.common.Logger;
import mobi.oneway.sd.core.common.LoggerFactory;
import mobi.oneway.sd.BuildConfig;
import mobi.oneway.sd.core.loader.infos.PluginActivityInfo;
import mobi.oneway.sd.core.runtime.GeneratedPluginActivity;
import mobi.oneway.sd.core.runtime.MixResources;
import mobi.oneway.sd.core.runtime.PluginActivity;
import mobi.oneway.sd.core.runtime.ShadowActivity;
import mobi.oneway.sd.core.runtime.ShadowActivityLifecycleCallbacks;
import mobi.oneway.sd.core.runtime.ShadowLayoutInflater;
import mobi.oneway.sd.core.runtime.container.HostActivityDelegate;
import mobi.oneway.sd.core.runtime.container.HostActivityDelegator;

import static mobi.oneway.sd.core.loader.managers.ComponentManager.*;
import static mobi.oneway.sd.core.loader.managers.ComponentManager.CM_ACTIVITY_INFO_KEY;
import static mobi.oneway.sd.core.loader.managers.ComponentManager.CM_BUSINESS_NAME_KEY;
import static mobi.oneway.sd.core.loader.managers.ComponentManager.CM_CALLING_ACTIVITY_KEY;
import static mobi.oneway.sd.core.loader.managers.ComponentManager.CM_CLASS_NAME_KEY;
import static mobi.oneway.sd.core.loader.managers.ComponentManager.CM_EXTRAS_BUNDLE_KEY;
import static mobi.oneway.sd.core.loader.managers.ComponentManager.CM_LOADER_BUNDLE_KEY;
import static mobi.oneway.sd.core.loader.managers.ComponentManager.CM_PART_KEY;

public class ShadowActivityDelegate extends GeneratedShadowActivityDelegate implements HostActivityDelegate {


    private static final String PLUGIN_OUT_STATE_KEY = "PLUGIN_OUT_STATE_KEY";
    private static final Logger mLogger = LoggerFactory.getLogger(ShadowActivityDelegate.class);

    private DI mDI;

    private HostActivityDelegator mHostActivityDelegator;
    private String mBusinessName;
    private String mPartKey;
    private Bundle mBundleForPluginLoader;
    private Bundle mRawIntentExtraBundle;
    private boolean mPluginActivityCreated;
    private boolean mDependenciesInjected;
    private boolean mRecreateCalled;
    /**
     * 判断是否调用过OnWindowAttributesChanged，如果调用过就说明需要在onCreate之前调用
     */
    private boolean mCallOnWindowAttributesChanged;
    private WindowManager.LayoutParams mBeforeOnCreateOnWindowAttributesChangedCalledParams;
    private MixResources mMixResources;


    public ShadowActivityDelegate(DI mDI) {
        this.mDI = mDI;
    }

    @Override
    public void setDelegator(HostActivityDelegator hostActivityDelegator) {
        mHostActivityDelegator = hostActivityDelegator;
    }

    @Override
    public GeneratedPluginActivity getPluginActivity() {
        return super.pluginActivity;
    }

    private Configuration mCurrentConfiguration;
    private int mPluginHandleConfigurationChange;
    private ComponentName mCallingActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Bundle pluginInitBundle = savedInstanceState != null ? savedInstanceState : mHostActivityDelegator.getIntent().getExtras();

        mCallingActivity = pluginInitBundle.getParcelable(CM_CALLING_ACTIVITY_KEY);
        mBusinessName = pluginInitBundle.getString(CM_BUSINESS_NAME_KEY, "");
        String partKey = pluginInitBundle.getString(CM_PART_KEY);
        mPartKey = partKey;
        mDI.inject(this, partKey);
        mDependenciesInjected = true;

        mMixResources = new MixResources(mHostActivityDelegator.superGetResources(), getMPluginResources());

        Bundle bundleForPluginLoader = pluginInitBundle.getBundle(CM_LOADER_BUNDLE_KEY);
        mBundleForPluginLoader = bundleForPluginLoader;
        bundleForPluginLoader.setClassLoader(ShadowActivity.class.getClassLoader());
        String pluginActivityClassName = bundleForPluginLoader.getString(CM_CLASS_NAME_KEY);
        PluginActivityInfo pluginActivityInfo = bundleForPluginLoader.getParcelable(CM_ACTIVITY_INFO_KEY);

        mCurrentConfiguration = new Configuration(getResources().getConfiguration());
        mPluginHandleConfigurationChange =
                (pluginActivityInfo.getActivityInfo().configChanges
                        | ActivityInfo.CONFIG_SCREEN_SIZE //系统本身就会单独对待这个属性，不声明也不会重启Activity。
                        | ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE//系统本身就会单独对待这个属性，不声明也不会重启Activity。
                        | 0x20000000 //见ActivityInfo.CONFIG_WINDOW_CONFIGURATION 系统处理属性
                );
        if (savedInstanceState == null) {
            mRawIntentExtraBundle = pluginInitBundle.getBundle(CM_EXTRAS_BUNDLE_KEY);
            mHostActivityDelegator.getIntent().replaceExtras(mRawIntentExtraBundle);
        }
        mHostActivityDelegator.getIntent().setExtrasClassLoader(getMPluginClassLoader());

        try {
            ShadowActivity pluginActivity = getMAppComponentFactory().instantiateActivity(
                    getMPluginClassLoader(),
                    pluginActivityClassName,
                    mHostActivityDelegator.getIntent()
            );
            initPluginActivity(pluginActivity, pluginActivityInfo);
            super.pluginActivity = pluginActivity;

            if (mLogger.isDebugEnabled()) {
                mLogger.debug("{} mPluginHandleConfigurationChange=={}", getPluginActivity().getClass().getCanonicalName(), mPluginHandleConfigurationChange);
            }

            //使PluginActivity替代ContainerActivity接收Window的Callback
            mHostActivityDelegator.getWindow().setCallback(pluginActivity);
            //设置插件AndroidManifest.xml 中注册的WindowSoftInputMode
            mHostActivityDelegator.getWindow().setSoftInputMode(pluginActivityInfo.getActivityInfo().softInputMode);

            //Activity.onCreate调用之前应该先收到onWindowAttributesChanged。
            if (mCallOnWindowAttributesChanged) {
                pluginActivity.onWindowAttributesChanged(mBeforeOnCreateOnWindowAttributesChangedCalledParams);
                mBeforeOnCreateOnWindowAttributesChangedCalledParams = null;
            }

            Bundle pluginSavedInstanceState = null;
            if (savedInstanceState != null) {
                pluginSavedInstanceState = savedInstanceState.getBundle(PLUGIN_OUT_STATE_KEY);
            }
            if (pluginSavedInstanceState != null) {
                pluginSavedInstanceState.setClassLoader(getMPluginClassLoader());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                notifyPluginActivityPreCreated(pluginActivity, pluginSavedInstanceState);
            }
            pluginActivity.onCreate(pluginSavedInstanceState);
            mPluginActivityCreated = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initPluginActivity(PluginActivity pluginActivity, PluginActivityInfo pluginActivityInfo) {
        pluginActivity.setHostActivityDelegator(mHostActivityDelegator);
        pluginActivity.setPluginResources(getMPluginResources());
        pluginActivity.setPluginClassLoader(getMPluginClassLoader());
        pluginActivity.setPluginComponentLauncher(getMComponentManager());
        pluginActivity.setPluginApplication(getMPluginApplication());
        pluginActivity.setShadowApplication(getMPluginApplication());
        pluginActivity.setApplicationInfo(getMPluginApplication().getApplicationInfo());
        pluginActivity.setBusinessName(mBusinessName);
        pluginActivity.setCallingActivity(mCallingActivity);
        pluginActivity.setPluginPartKey(mPartKey);

        //前面的所有set方法都是PluginActivity定义的方法，
        //业务的Activity子类不会覆盖这些方法。调用它们不会执行业务Activity的任何逻辑。
        //最后这个setHostContextAsBase会调用插件Activity的attachBaseContext方法，
        //有可能会执行业务Activity覆盖的逻辑。
        //所以，这个调用要放在最后。
        pluginActivity.setHostContextAsBase((Context) mHostActivityDelegator.getHostActivity());
        pluginActivity.setTheme(pluginActivityInfo.getThemeResource());
    }

    @Override
    public String getLoaderVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle pluginExtras = intent.getBundleExtra(CM_EXTRAS_BUNDLE_KEY);
        intent.replaceExtras(pluginExtras);
        getPluginActivity().onNewIntent(intent);
    }

    @Override
    public boolean onNavigateUpFromChild(Activity arg0) {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle pluginOutState = new Bundle(getMPluginClassLoader());
        getPluginActivity().onSaveInstanceState(pluginOutState);
        outState.putBundle(PLUGIN_OUT_STATE_KEY, pluginOutState);
        outState.putString(CM_PART_KEY, mPartKey);
        outState.putBundle(CM_LOADER_BUNDLE_KEY, mBundleForPluginLoader);
        if (mRecreateCalled) {
            outState.putBundle(CM_EXTRAS_BUNDLE_KEY, mHostActivityDelegator.getIntent().getExtras());
        } else {
            outState.putBundle(CM_EXTRAS_BUNDLE_KEY, mRawIntentExtraBundle);
        }
    }

    @Override
    public void onChildTitleChanged(Activity arg0, CharSequence arg1) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int diff = newConfig.diff(mCurrentConfiguration);
        if (mLogger.isDebugEnabled()) {
            mLogger.debug("{} onConfigurationChanged diff=={}", getPluginActivity().getClass().getCanonicalName(), diff);
        }
        if (diff == (diff & mPluginHandleConfigurationChange)) {
            getPluginActivity().onConfigurationChanged(newConfig);
            mCurrentConfiguration = new Configuration(newConfig);
        } else {
            mHostActivityDelegator.superOnConfigurationChanged(newConfig);
            mHostActivityDelegator.recreate();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Bundle pluginSavedInstanceState = null;
        if (savedInstanceState != null) {
            pluginSavedInstanceState = savedInstanceState.getBundle(PLUGIN_OUT_STATE_KEY);
        }
        getPluginActivity().onRestoreInstanceState(pluginSavedInstanceState);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        Bundle pluginSavedInstanceState = null;
        if (savedInstanceState != null) {
            pluginSavedInstanceState = savedInstanceState.getBundle(PLUGIN_OUT_STATE_KEY);
        }
        getPluginActivity().onPostCreate(pluginSavedInstanceState);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (mPluginActivityCreated) {
            getPluginActivity().onWindowAttributesChanged(params);
        } else {
            mBeforeOnCreateOnWindowAttributesChangedCalledParams = params;
        }
        mCallOnWindowAttributesChanged = true;
    }

    @Override
    public void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        mHostActivityDelegator.superOnApplyThemeResource(theme, resid, first);
        if (mPluginActivityCreated) {
            getPluginActivity().onApplyThemeResource(theme, resid, first);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return getMPluginClassLoader();
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        LayoutInflater inflater = (LayoutInflater) mHostActivityDelegator.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return ShadowLayoutInflater.build(inflater, getPluginActivity(), mPartKey);
    }

    @Override
    public Resources getResources() {
        if (mDependenciesInjected) {
            return mMixResources;
        } else {
            //预期只有android.view.Window.getDefaultFeatures会调用到这个分支，此时我们还无法确定插件资源
            //而getDefaultFeatures只需要访问系统资源
            return Resources.getSystem();
        }
    }

    @Override
    public void recreate() {
        mRecreateCalled = true;
        mHostActivityDelegator.superRecreate();
    }

    private void notifyPluginActivityPreCreated(ShadowActivity pluginActivity, Bundle pluginSavedInstanceState) {
        ShadowActivityLifecycleCallbacks.Holder.notifyPluginActivityPreCreated(
                pluginActivity,
                pluginSavedInstanceState
        );
    }
}
