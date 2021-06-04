package mobi.oneway.sd.core.loader.delegates;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import mobi.oneway.sd.core.loader.managers.PluginContentProviderManager;
import mobi.oneway.sd.core.runtime.container.HostContentProviderDelegate;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Set;

public class ShadowContentProviderDelegate extends ShadowDelegate implements HostContentProviderDelegate {


    private PluginContentProviderManager mProviderManager;

    public ShadowContentProviderDelegate(PluginContentProviderManager mProviderManager) {
        this.mProviderManager = mProviderManager;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Set allProviders = mProviderManager.getAllContentProvider();
        if (allProviders != null)
        {
            Iterator iterator = allProviders.iterator();
            while (iterator.hasNext())
            {
                ContentProvider provider = (ContentProvider) iterator.next();
                provider.onConfigurationChanged(newConfig);
            }
        }
    }

    @Override
    public void onLowMemory() {
        Set allProviders = mProviderManager.getAllContentProvider();
        if (allProviders != null)
        {
            Iterator iterator = allProviders.iterator();
            while (iterator.hasNext())
            {
                ContentProvider provider = (ContentProvider) iterator.next();
                provider.onLowMemory();
            }
        }
    }

    @Override
    public void onTrimMemory(int level) {
        Set allProviders = mProviderManager.getAllContentProvider();
        if (allProviders != null)
        {
            Iterator iterator = allProviders.iterator();
            while (iterator.hasNext())
            {
                ContentProvider provider = (ContentProvider) iterator.next();
                provider.onTrimMemory(level);
            }
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri pluginUri = mProviderManager.convert2PluginUri(uri);
        return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).query(pluginUri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        Uri pluginUri = mProviderManager.convert2PluginUri(uri);
        return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).getType(pluginUri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri pluginUri = mProviderManager.convert2PluginUri(uri);
        return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).insert(pluginUri, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Uri pluginUri = mProviderManager.convert2PluginUri(uri);
        return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).delete(pluginUri, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Uri pluginUri = mProviderManager.convert2PluginUri(uri);
        return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).update(pluginUri, values, selection, selectionArgs);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Uri pluginUri = mProviderManager.convert2PluginUri(uri);
        return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).bulkInsert(pluginUri, values);
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Uri pluginUri = mProviderManager.convert2PluginUri(extras);
        return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).call(method, arg, extras);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) {
        Uri pluginUri = mProviderManager.convert2PluginUri(uri);
        try {
            return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).openFile(pluginUri, mode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) {
        Uri pluginUri = mProviderManager.convert2PluginUri(uri);
        try {
            return mProviderManager.getPluginContentProvider(pluginUri.getAuthority()).openFile(pluginUri, mode, signal);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
