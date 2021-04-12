package com.tencent.shadow.dynamic.loader.impl;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.tencent.shadow.dynamic.loader.PluginServiceConnection;

public class BinderPluginServiceConnection {
    private IBinder mRemote;

    public BinderPluginServiceConnection(IBinder mRemote) {
        this.mRemote = mRemote;
    }

    public void onServiceConnected(ComponentName name, IBinder service) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(PluginServiceConnection.DESCRIPTOR);
            if (name != null) {
                _data.writeInt(1);
                name.writeToParcel(_data, 0);
            } else {
                _data.writeInt(0);
            }
            _data.writeStrongBinder(service);
            mRemote.transact(PluginServiceConnection.TRANSACTION_onServiceConnected, _data, _reply, 0);
            _reply.readException();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    public void onServiceDisconnected(ComponentName name) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(PluginServiceConnection.DESCRIPTOR);
            if (name != null) {
                _data.writeInt(1);
                name.writeToParcel(_data, 0);
            } else {
                _data.writeInt(0);
            }
            mRemote.transact(PluginServiceConnection.TRANSACTION_onServiceDisconnected, _data, _reply, 0);
            _reply.readException();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }


    public IBinder getmRemote() {
        return mRemote;
    }
}
