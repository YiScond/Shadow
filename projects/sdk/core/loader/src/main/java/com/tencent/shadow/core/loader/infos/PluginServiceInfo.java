package com.tencent.shadow.core.loader.infos;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class PluginServiceInfo extends PluginComponentInfo implements Parcelable {
    public static final PluginServiceInfo.CREATOR CREATOR = new PluginServiceInfo.CREATOR();

    public void writeToParcel( Parcel dest, int flags) {
        dest.writeString(this.getClassName());
    }

    public int describeContents() {
        return 0;
    }

    public PluginServiceInfo( String className) {
        super(className);
    }

    public PluginServiceInfo( Parcel parcel) {
        this(parcel.readString());
    }


    public static final class CREATOR implements Creator {
        
        public PluginServiceInfo createFromParcel( Parcel parcel) {
            return new PluginServiceInfo(parcel);
        }


        public PluginServiceInfo[] newArray(int size) {
            return new PluginServiceInfo[size];
        }


        private CREATOR() {
        }

    }
}
