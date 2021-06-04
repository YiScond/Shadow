package mobi.oneway.sd.core.loader.infos;

import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;


public class PluginActivityInfo extends PluginComponentInfo implements Parcelable {
    private final int themeResource;
    private final ActivityInfo activityInfo;
    public static final PluginActivityInfo.CREATOR CREATOR = new CREATOR();

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.getClassName());
        parcel.writeInt(this.themeResource);
        parcel.writeParcelable((Parcelable) this.activityInfo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public final int getThemeResource() {
        return this.themeResource;
    }


    public final ActivityInfo getActivityInfo() {
        return this.activityInfo;
    }

    public PluginActivityInfo(String className, int themeResource, ActivityInfo activityInfo) {
        super(className);
        this.themeResource = themeResource;
        this.activityInfo = activityInfo;
    }

    public PluginActivityInfo(Parcel parcel) {
        this(parcel.readString(), parcel.readInt(), (ActivityInfo) parcel.readParcelable(ActivityInfo.class.getClassLoader()));
    }



    public static final class CREATOR implements Creator {

        public PluginActivityInfo createFromParcel(Parcel parcel) {
            return new PluginActivityInfo(parcel);
        }


        public PluginActivityInfo[] newArray(int size) {
            return new PluginActivityInfo[size];
        }


        private CREATOR() {
        }


    }
}
