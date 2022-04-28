package mobi.oneway.sd.core.loader.blocs;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.webkit.WebView;

import java.util.concurrent.CountDownLatch;

public class CreateResourceBloc {

    public static Resources create(PackageInfo packageArchiveInfo, String archiveFilePath, final Context hostAppContext) {
        //先用宿主context初始化一个WebView，以便WebView的逻辑去修改sharedLibraryFiles，将webview.apk添加进去
        final CountDownLatch latch = new CountDownLatch(1);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                creatWebview(hostAppContext);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PackageManager packageManager = hostAppContext.getPackageManager();
        packageArchiveInfo.applicationInfo.publicSourceDir = archiveFilePath;
        packageArchiveInfo.applicationInfo.sourceDir = archiveFilePath;
        packageArchiveInfo.applicationInfo.sharedLibraryFiles = hostAppContext.getApplicationInfo().sharedLibraryFiles;
        try {
            return packageManager.getResourcesForApplication(packageArchiveInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void creatWebview(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    int myPid = Process.myPid();
                    String str = context.getPackageName() + myPid;
                    for (ActivityManager.RunningAppProcessInfo next : ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
                        if (next.pid == myPid) {
                            str = next.processName;
                        }
                    }
                    WebView.setDataDirectorySuffix(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            new WebView(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
