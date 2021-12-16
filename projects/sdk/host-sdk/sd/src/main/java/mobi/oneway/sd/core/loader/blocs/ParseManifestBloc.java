package mobi.oneway.sd.core.loader.blocs;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;

import mobi.oneway.sd.core.common.InstalledApk;
import mobi.oneway.sd.core.loader.infos.ManifestInfo;

public final class ParseManifestBloc {
    private static final String ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android";
    private static final String TAG_RECEIVER = "receiver";
    private static final String TAG_INTENT_FILTER = "intent-filter";
    private static final String TAG_ACTION = "action";
    private static final String ATTR_NAME = "name";

    private ParseManifestBloc() {
    }

    public static final ManifestInfo parse(Context context, InstalledApk installedApk) throws Exception {
        ManifestInfo manifestInfo = new ManifestInfo();
        Resources resources = newResource(context, installedApk);
        XmlResourceParser parser = resources.getAssets().openXmlResourceParser("AndroidManifest.xml");
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.START_TAG) {
                parseBroadcastReceiver(parser, manifestInfo);
            }
        }

        return manifestInfo;
    }

    private static final Resources newResource(Context context, InstalledApk installedApk) throws PackageManager.NameNotFoundException {
        PackageInfo packageArchiveInfo = context.getPackageManager().getPackageArchiveInfo(installedApk.apkFilePath, 0);
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = packageArchiveInfo.applicationInfo;
        if (applicationInfo != null) {
            applicationInfo.publicSourceDir = installedApk.apkFilePath;
            applicationInfo.sourceDir = installedApk.apkFilePath;
        }
        return packageManager.getResourcesForApplication(packageArchiveInfo.applicationInfo);
    }

    private static final void parseBroadcastReceiver(XmlPullParser parser, ManifestInfo manifestInfo) throws Exception {
        if (TAG_RECEIVER.equals(parser.getName())) {
            String value = parser.getAttributeValue(ANDROID_RESOURCES, ATTR_NAME);
            ManifestInfo.Receiver receiver = new ManifestInfo.Receiver(value);
            int outerDepth = parser.getDepth();
            int type;
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                if (TAG_INTENT_FILTER.equals(parser.getName())) {
                    ManifestInfo.ReceiverIntentInfo receiverInfo
                            = new ManifestInfo.ReceiverIntentInfo();
                    parserIntent(parser, receiverInfo);
                    receiver.getIntents().add(receiverInfo);
                }
            }
            manifestInfo.getReceivers().add(receiver);
        }
    }

    private static final void parserIntent(XmlPullParser parser, IntentFilter intentFilter) throws Exception {
        int outerDepth = parser.getDepth();
        int type;

        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }
            if (TAG_ACTION.equals(parser.getName())) {
                String value = parser.getAttributeValue(ANDROID_RESOURCES, ATTR_NAME);
                intentFilter.addAction(value);
            }
        }
    }
}