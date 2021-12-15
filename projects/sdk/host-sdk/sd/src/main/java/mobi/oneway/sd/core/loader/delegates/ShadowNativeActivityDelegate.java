package mobi.oneway.sd.core.loader.delegates;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.InputQueue;
import android.view.SurfaceHolder;

import mobi.oneway.sd.core.runtime.ShadowNativeActivity;
import mobi.oneway.sd.core.runtime.container.HostNativeActivityDelegate;

public class ShadowNativeActivityDelegate extends ShadowActivityDelegate
        implements HostNativeActivityDelegate {


    public ShadowNativeActivityDelegate(DI mDI) {
        super(mDI);
    }

    private ShadowNativeActivity getShadowNativeActivity() {
        return (ShadowNativeActivity) super.pluginActivity;
    }

    @Override
    public PackageManager getPackageManager() {
        return new PackageManagerWrapper(mHostActivityDelegator.superGetPackageManager()) {
            @Override
            public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {
                return mPluginActivityInfo.getActivityInfo();
            }
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        getShadowNativeActivity().surfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        getShadowNativeActivity().surfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        getShadowNativeActivity().surfaceRedrawNeeded(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        getShadowNativeActivity().surfaceDestroyed(holder);
    }

    @Override
    public void onInputQueueCreated(InputQueue queue) {
        getShadowNativeActivity().onInputQueueCreated(queue);
    }

    @Override
    public void onInputQueueDestroyed(InputQueue queue) {
        getShadowNativeActivity().onInputQueueDestroyed(queue);
    }

    @Override
    public void onGlobalLayout() {
        getShadowNativeActivity().onGlobalLayout();
    }
}
