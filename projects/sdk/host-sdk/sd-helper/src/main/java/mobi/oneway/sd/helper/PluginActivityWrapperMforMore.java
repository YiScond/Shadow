package mobi.oneway.sd.helper;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.SearchEvent;

import mobi.oneway.sd.helper.util.ReflectUtil;

/**
 * android 6.0 后使用
 */
public class PluginActivityWrapperMforMore extends PluginActivityWrapper{

    public PluginActivityWrapperMforMore(Object hostActivity) {
        super(hostActivity);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        try {
            return ReflectUtil.with(hostActivity)
                    .onMethod("onSearchRequested", SearchEvent.class)
                    .invokeMethod(hostActivity, searchEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
