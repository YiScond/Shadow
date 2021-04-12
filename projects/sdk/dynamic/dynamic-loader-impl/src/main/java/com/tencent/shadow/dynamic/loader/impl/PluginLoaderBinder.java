/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  android.content.ComponentName
 *  android.content.Intent
 *  android.os.Binder
 *  android.os.IBinder
 *  android.os.Parcel
 *  android.os.RemoteException
 *  com.tencent.shadow.dynamic.host.PluginLoaderImpl
 *  com.tencent.shadow.dynamic.host.UuidManager
 *  com.tencent.shadow.dynamic.loader.PluginLoader
 *  kotlin.Metadata
 *  kotlin.jvm.internal.Intrinsics
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.tencent.shadow.dynamic.loader.impl;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.tencent.shadow.dynamic.host.PluginLoaderImpl;
import com.tencent.shadow.dynamic.host.UuidManager;
import com.tencent.shadow.dynamic.loader.PluginLoader;

import java.util.Map;

public final class PluginLoaderBinder
        extends Binder
        implements PluginLoaderImpl {
    private final DynamicPluginLoader mDynamicPluginLoader;

    public void setUuidManager( UuidManager uuidManager) {
        this.mDynamicPluginLoader.setUuidManager(uuidManager);
    }

    public boolean onTransact(int code,  Parcel data,  Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1598968902: {
                Parcel parcel = reply;
                parcel.writeString(PluginLoader.DESCRIPTOR);
                return true;
            }
            case 1: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                String _arg0 = null;
                String string = data.readString();
                _arg0 = string;
                this.mDynamicPluginLoader.loadPlugin(_arg0);
                Parcel parcel = reply;
                parcel.writeNoException();
                return true;
            }
            case 2: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                Map<String, Boolean> _result = this.mDynamicPluginLoader.getLoadedPlugin();
                Parcel parcel = reply;
                parcel.writeNoException();
                reply.writeMap(_result);
                return true;
            }
            case 3: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                String _arg0 = null;
                String string = data.readString();
                _arg0 = string;
                this.mDynamicPluginLoader.callApplicationOnCreate(_arg0);
                Parcel parcel = reply;
                parcel.writeNoException();
                return true;
            }
            case 4: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                Intent _arg0 = null;
                _arg0 = data.readInt() != 0 ? (Intent)Intent.CREATOR.createFromParcel(data) : (Intent)null;
                Intent intent = _arg0;
                Intent _result = this.mDynamicPluginLoader.convertActivityIntent(intent);
                Parcel parcel = reply;
                parcel.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            }
            case 5: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                Intent _arg0 = null;
                _arg0 = data.readInt() != 0 ? (Intent)Intent.CREATOR.createFromParcel(data) : (Intent)null;
                Intent intent = _arg0;
                ComponentName _result = this.mDynamicPluginLoader.startPluginService(intent);
                Parcel parcel = reply;
                parcel.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            }
            case 6: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                Intent _arg0 = null;
                _arg0 = data.readInt() != 0 ? (Intent)Intent.CREATOR.createFromParcel(data) : (Intent)null;
                Intent intent = _arg0;
                boolean _result = this.mDynamicPluginLoader.stopPluginService(intent);
                Parcel parcel = reply;
                parcel.writeNoException();
                reply.writeInt(_result ? 1 : 0);
                return true;
            }
            case 7: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                Intent _arg0 = null;
                _arg0 = data.readInt() != 0 ? (Intent)Intent.CREATOR.createFromParcel(data) : (Intent)null;
                IBinder iBinder = data.readStrongBinder();
                BinderPluginServiceConnection _arg1 = new BinderPluginServiceConnection(iBinder);
                int _arg2 = 0;
                _arg2 = data.readInt();
                Intent intent = _arg0;
                boolean _result = this.mDynamicPluginLoader.bindPluginService(intent, _arg1, _arg2);
                Parcel parcel = reply;
                parcel.writeNoException();
                reply.writeInt(_result ? 1 : 0);
                return true;
            }
            case 8: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                IBinder iBinder = data.readStrongBinder();
                this.mDynamicPluginLoader.unbindService(iBinder);
                Parcel parcel = reply;
                parcel.writeNoException();
                return true;
            }
            case 9: {
                data.enforceInterface(PluginLoader.DESCRIPTOR);
                Object object = Intent.CREATOR.createFromParcel(data);
                this.mDynamicPluginLoader.startActivityInPluginProcess((Intent)object);
                Parcel parcel = reply;
                parcel.writeNoException();
                return true;
            }
        }
        return super.onTransact(code, data, reply, flags);
    }

    public PluginLoaderBinder(DynamicPluginLoader mDynamicPluginLoader) {
        this.mDynamicPluginLoader = mDynamicPluginLoader;
    }
}
