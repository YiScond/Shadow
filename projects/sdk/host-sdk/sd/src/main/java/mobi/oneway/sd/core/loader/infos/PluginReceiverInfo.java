package mobi.oneway.sd.core.loader.infos;

import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 插件广播数据
 *
 * @author xuedizi2009@163.com
 */
public final class PluginReceiverInfo
        extends PluginComponentInfo
        implements Parcelable {

    public static final CREATOR CREATOR = new CREATOR();

    private final ActivityInfo activityInfo;

    private final List<String> actions;

    public PluginReceiverInfo(String className, ActivityInfo activityInfo, List<String> actions) {
        super(className);
        this.activityInfo = activityInfo;
        this.actions = actions;
    }

    public final List<String> getActions() {
        return this.actions;
    }

    public PluginReceiverInfo(Parcel parcel) {
        this(parcel.readString(), parcel.readParcelable(ActivityInfo.class.getClassLoader()), parcel.createStringArrayList());
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.getClassName());
        parcel.writeParcelable(this.activityInfo, flags);
        parcel.writeStringList(this.actions);
    }

    public int describeContents() {
        return 0;
    }

    public static final class CREATOR
            implements Creator {
        private CREATOR() {
        }

        public PluginReceiverInfo createFromParcel(Parcel parcel) {
            return new PluginReceiverInfo(parcel);
        }

        public PluginReceiverInfo[] newArray(int size) {
            return new PluginReceiverInfo[size];
        }

    }
}

