package mobi.oneway.sd.core.runtime;

import android.app.Activity;
import android.app.Dialog;

import mobi.oneway.sd.core.runtime.container.HostActivityDelegator;
import mobi.oneway.sd.core.runtime.container.PluginContainerActivity;

public class ShadowDialogSupport {

    public static void dialogSetOwnerActivity(Dialog dialog, ShadowActivity activity) {

        Activity hostActivity;
        HostActivityDelegator hostActivityDelegator = activity.hostActivityDelegator;

        if (hostActivityDelegator != null) {
            hostActivity = (Activity) hostActivityDelegator.getHostActivity();
        } else {
            hostActivity = (Activity) activity.getWrapperHostActivity();
        }

        dialog.getWindow().getDecorView().setTag(activity);
        dialog.setOwnerActivity(hostActivity);
    }

    public static ShadowActivity dialogGetOwnerActivity(Dialog dialog) {
        Activity ownerActivity = dialog.getOwnerActivity();
        if (ownerActivity == null) {
            return null;
        }

        if (ownerActivity instanceof PluginContainerActivity) {
            return (ShadowActivity) PluginActivity.get((PluginContainerActivity) ownerActivity);
        } else {
            return (ShadowActivity) dialog.getWindow().getDecorView().getTag();
        }
    }

}
