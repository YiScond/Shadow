package com.tencent.shadow.core.loader.infos;

import android.content.pm.ProviderInfo;
import android.os.Parcel;
import android.os.Parcelable;


public final class PluginProviderInfo extends PluginComponentInfo implements Parcelable {

    private final String authority;

    private final ProviderInfo providerInfo;

    public static final PluginProviderInfo.CREATOR CREATOR = new PluginProviderInfo.CREATOR();

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.getClassName());
        parcel.writeString(this.authority);
        parcel.writeParcelable((Parcelable) this.providerInfo, flags);
    }

    public int describeContents() {
        return 0;
    }


    public final String getAuthority() {
        return this.authority;
    }


    public final ProviderInfo getProviderInfo() {
        return this.providerInfo;
    }

    public PluginProviderInfo(String className, String authority, ProviderInfo providerInfo) {
        super(className);
        this.authority = authority;
        this.providerInfo = providerInfo;
    }

    public PluginProviderInfo(Parcel parcel) {
        this(parcel.readString(), parcel.readString(), (ProviderInfo) parcel.readParcelable(ProviderInfo.class.getClassLoader()));
    }


    public static final class CREATOR implements Creator {

        public PluginProviderInfo createFromParcel(Parcel parcel) {
            return new PluginProviderInfo(parcel);
        }



        public PluginProviderInfo[] newArray(int size) {
            return new PluginProviderInfo[size];
        }


        private CREATOR() {
        }
    }
}
