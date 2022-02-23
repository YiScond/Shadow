package mobi.oneway.sd.helper;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import mobi.oneway.sd.core.loader.managers.ComponentManager;
import mobi.oneway.sd.core.manager.ShadowPluginManager;
import mobi.oneway.sd.helper.util.ReflectUtil;


/**
 * 包装Activity 支持调用插件的能力
 * 大部分方法都需要重写 通过反射hostActivity来调用支持其方法功能
 */
public class PluginActivityWrapper extends Activity {

    /**
     * 防止宿主Activity被transform成ShadowActivity,故声明为Object类
     * 调用需要通过反射来调用Activity的相关方法
     */
    protected Object hostActivity;
    private ShadowPluginManager shadowPluginManager;
    private ComponentManager componentManager;
    private Context pluginContextWrapper;
    private Context hostContext;

    public PluginActivityWrapper(Object hostActivity) {
        this.hostActivity = hostActivity;

        hostContext = getHostContext();
        shadowPluginManager = ShadowPluginManager.getInstance(hostContext);
        componentManager = shadowPluginManager.getComponentManager();
        pluginContextWrapper = new PluginContextWrapper(hostContext);
        attachBaseContext(hostContext);

        ShadowUtil.injectShadow(this);
        injectWrapperHostActivity(hostActivity);

    }


    private void injectWrapperHostActivity(Object hostActivity) {
        try {
            ReflectUtil.with(this)
                    .onMethod("setWrapperHostActivity", Object.class)
                    .invokeMethod(this, hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (componentManager.isPluginComponent(intent)) {
            shadowPluginManager.startPluginActivity(intent);
            return;
        }

        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("startActivity", Intent.class)
                    .invokeMethod(hostActivity, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        if (componentManager.unbindService(null, conn).first) {
            return;
        }

        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("unbindService", ServiceConnection.class)
                    .invokeMethod(hostActivity, conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        if (componentManager.isPluginComponent(service)) {
            return shadowPluginManager.bindPluginService(service, conn, flags);
        }

        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("bindService", Intent.class, ServiceConnection.class, int.class)
                    .invokeMethod(hostActivity, service, conn, flags);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean stopService(Intent name) {
        if (componentManager.isPluginComponent(name)) {
            return shadowPluginManager.stopPluginService(name);
        }

        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("stopService", Intent.class)
                    .invokeMethod(hostActivity, name);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ComponentName startService(Intent service) {
        if (componentManager.isPluginComponent(service)) {
            return shadowPluginManager.startPluginService(service);
        }

        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("startService", Intent.class)
                    .invokeMethod(hostActivity, service);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public WindowManager getWindowManager() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getWindowManager")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Window getWindow() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getWindow")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Intent getIntent() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getIntent")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getRequestedOrientation() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getRequestedOrientation")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("setRequestedOrientation", int.class)
                    .invokeMethod(hostActivity, requestedOrientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return getBaseContext();
    }

    @Override
    public Context getBaseContext() {
        return pluginContextWrapper;
    }

    private Context getHostContext() {
        Context context = null;
        try {
            context = ReflectUtil.with(hostActivity)
                    .onMethod("getApplicationContext")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context;
    }

    @Override
    public String getPackageName() {
        return hostContext.getPackageName();
    }

    @Override
    public boolean isFinishing() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("isFinishing")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean isDestroyed() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("isDestroyed")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean isChangingConfigurations() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("isChangingConfigurations")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getApplicationInfo")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Resources getResources() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getResources")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Resources.Theme getTheme() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getTheme")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getClassLoader")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getSystemService(String name) {

        if (LAYOUT_INFLATER_SERVICE.equals(name))
        {
            return pluginContextWrapper.getSystemService(name);
        }

        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getSystemService", String.class)
                    .invokeMethod(hostActivity, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends View> T findViewById(int id) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("findViewById", int.class)
                    .invokeMethod(hostActivity, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ActionBar getActionBar() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("getActionBar")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onWindowFocusChanged", boolean.class)
                    .invokeMethod(hostActivity, hasFocus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("dispatchGenericMotionEvent", MotionEvent.class)
                    .invokeMethod(hostActivity, ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("dispatchKeyEvent", KeyEvent.class)
                    .invokeMethod(hostActivity, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("dispatchKeyShortcutEvent", KeyEvent.class)
                    .invokeMethod(hostActivity, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("dispatchPopulateAccessibilityEvent", AccessibilityEvent.class)
                    .invokeMethod(hostActivity, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("dispatchTouchEvent", MotionEvent.class)
                    .invokeMethod(hostActivity, ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("dispatchTrackballEvent", MotionEvent.class)
                    .invokeMethod(hostActivity, ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onActionModeFinished", ActionMode.class)
                    .invokeMethod(hostActivity, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onActionModeStarted", ActionMode.class)
                    .invokeMethod(hostActivity, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttachedToWindow() {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onAttachedToWindow")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onContentChanged() {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onContentChanged")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("onCreatePanelMenu", int.class, Menu.class)
                    .invokeMethod(hostActivity, featureId, menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public View onCreatePanelView(int featureId) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("onCreatePanelView", int.class)
                    .invokeMethod(hostActivity, featureId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDetachedFromWindow() {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onDetachedFromWindow")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("onMenuItemSelected", int.class, MenuItem.class)
                    .invokeMethod(hostActivity, featureId, item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("onMenuOpened", int.class, Menu.class)
                    .invokeMethod(hostActivity, featureId, menu);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onPanelClosed", int.class, Menu.class)
                    .invokeMethod(hostActivity, featureId, menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("onPreparePanel", int.class, View.class, Menu.class)
                    .invokeMethod(hostActivity, featureId, view, menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onSearchRequested() {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("onSearchRequested")
                    .invokeMethod(hostActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onWindowAttributesChanged", WindowManager.LayoutParams.class)
                    .invokeMethod(hostActivity, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onWindowStartingActionMode", ActionMode.Callback.class)
                    .invokeMethod(hostActivity, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        try {
            ReflectUtil.with(hostActivity)
                    .onMethod("onWindowStartingActionMode", ActionMode.Callback.class, int.class)
                    .invokeMethod(hostActivity, callback, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

