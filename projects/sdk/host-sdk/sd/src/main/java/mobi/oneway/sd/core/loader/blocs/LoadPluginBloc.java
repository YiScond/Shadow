package mobi.oneway.sd.core.loader.blocs;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;

import mobi.oneway.sd.core.common.InstalledApk;
import mobi.oneway.sd.core.load_parameters.LoadParameters;
import mobi.oneway.sd.core.loader.classloaders.PluginClassLoader;
import mobi.oneway.sd.core.loader.exceptions.LoadPluginException;
import mobi.oneway.sd.core.loader.infos.ManifestInfo;
import mobi.oneway.sd.core.loader.infos.PluginInfo;
import mobi.oneway.sd.core.loader.infos.PluginParts;
import mobi.oneway.sd.core.loader.managers.ComponentManager;
import mobi.oneway.sd.core.loader.managers.PluginPackageManagerImpl;
import mobi.oneway.sd.core.runtime.PluginPackageManager;
import mobi.oneway.sd.core.runtime.PluginPartInfo;
import mobi.oneway.sd.core.runtime.PluginPartInfoManager;
import mobi.oneway.sd.core.runtime.ShadowAppComponentFactory;
import mobi.oneway.sd.core.runtime.ShadowApplication;
import mobi.oneway.sd.core.runtime.ShadowContext;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class LoadPluginBloc {

    public static <T> Future<T> loadPlugin(ExecutorService executorService,
                                           final Set<PackageInfo> pluginPackageInfoSet,
                                           final PackageInfo[] allPluginPackageInfo,
                                           final ComponentManager componentManager,
                                           final ReentrantLock lock,
                                           final Map<String, PluginParts> pluginPartsMap,
                                           final Context hostAppContext,
                                           final InstalledApk installedApk,
                                           final LoadParameters loadParameters) throws LoadPluginException {
        if (installedApk.apkFilePath == null) {
            throw new LoadPluginException("apkFilePath==null");
        } else {

            final Future<PluginClassLoader> buildClassLoader = executorService.submit(new Callable<PluginClassLoader>() {
                @Override
                public PluginClassLoader call() throws Exception {

                    PluginClassLoader classLoader = null;

                    try {
                        lock.lock();
                        classLoader = LoadApkBloc.loadPlugin(installedApk, loadParameters, pluginPartsMap);
                    } finally {
                        lock.unlock();
                    }
                    return classLoader;
                }
            });

            final Future<PackageInfo> getPackageInfo = executorService.submit(new Callable<PackageInfo>() {
                @Override
                public PackageInfo call() throws Exception {

                    String archiveFilePath = installedApk.apkFilePath;
                    PackageManager packageManager = hostAppContext.getPackageManager();

                    PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(
                            archiveFilePath,
                            PackageManager.GET_ACTIVITIES
                                    | PackageManager.GET_META_DATA
                                    | PackageManager.GET_SERVICES
                                    | PackageManager.GET_PROVIDERS
                                    | PackageManager.GET_RECEIVERS
                                    | PackageManager.GET_SIGNATURES
                    );

                    if (packageArchiveInfo == null) {
                        throw new NullPointerException("getPackageArchiveInfo return null.archiveFilePath==" + archiveFilePath);
                    }

                    ShadowContext tempContext = new ShadowContext(hostAppContext, 0);
                    tempContext.setBusinessName(loadParameters.businessName);

                    File dataDir;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        dataDir = tempContext.getDataDir();
                    } else {
                        dataDir = new File(tempContext.getFilesDir(), "dataDir");
                    }
                    dataDir.mkdirs();

                    packageArchiveInfo.applicationInfo.nativeLibraryDir = installedApk.libraryPath;
                    packageArchiveInfo.applicationInfo.dataDir = dataDir.getAbsolutePath();
                    packageArchiveInfo.applicationInfo.processName = hostAppContext.getApplicationInfo().processName;
                    packageArchiveInfo.applicationInfo.uid = hostAppContext.getApplicationInfo().uid;

                    lock.lock();
                    try {
                        pluginPackageInfoSet.add(packageArchiveInfo);
                    } catch (Exception e) {

                    } finally {
                        lock.unlock();
                    }
                    return packageArchiveInfo;
                }
            });

            final Future<ManifestInfo> buildManifestInfo = executorService.submit(new Callable<ManifestInfo>() {
                @Override
                public ManifestInfo call() throws Exception {
                    return ParseManifestBloc.parse(hostAppContext, installedApk);
                }
            });

            final Future<PluginInfo> buildPluginInfo = executorService.submit(new Callable<PluginInfo>() {
                @Override
                public PluginInfo call() throws Exception {
                    return ParsePluginApkBloc.parse(getPackageInfo.get(),
                            buildManifestInfo.get(),
                            loadParameters,
                            hostAppContext);
                }
            });

            final Future<PluginPackageManager> buildPackageManager = executorService.submit(new Callable<PluginPackageManager>() {
                @Override
                public PluginPackageManager call() throws Exception {
                    PackageInfo packageInfo = getPackageInfo.get();
                    PackageManager hostPackageManager = hostAppContext.getPackageManager();
                    return new PluginPackageManagerImpl(hostPackageManager, packageInfo, allPluginPackageInfo);
                }
            });

            final Future<Resources> buildResources = executorService.submit(new Callable<Resources>() {
                @Override
                public Resources call() throws Exception {
                    PackageInfo packageInfo = getPackageInfo.get();
                    return CreateResourceBloc.create(packageInfo, installedApk.apkFilePath, hostAppContext);
                }
            });

            final Future<ShadowAppComponentFactory> buildAppComponentFactory = executorService.submit(new Callable<ShadowAppComponentFactory>() {
                @Override
                public ShadowAppComponentFactory call() throws Exception {
                    ClassLoader pluginClassLoader = buildClassLoader.get();
                    PluginInfo pluginInfo = buildPluginInfo.get();
                    if (pluginInfo.getAppComponentFactory() != null) {
                        Class clazz = pluginClassLoader.loadClass(pluginInfo.getAppComponentFactory());
                        return (ShadowAppComponentFactory) clazz.newInstance();
                    } else return new ShadowAppComponentFactory();
                }
            });

            final Future<ShadowApplication> buildApplication = executorService.submit(new Callable<ShadowApplication>() {
                @Override
                public ShadowApplication call() throws Exception {
                    PluginClassLoader pluginClassLoader = buildClassLoader.get();
                    Resources resources = buildResources.get();
                    PluginInfo pluginInfo = buildPluginInfo.get();
                    PackageInfo packageInfo = getPackageInfo.get();
                    ShadowAppComponentFactory appComponentFactory = buildAppComponentFactory.get();

                    return CreateApplicationBloc.createShadowApplication(
                            pluginClassLoader,
                            pluginInfo,
                            resources,
                            hostAppContext,
                            componentManager,
                            packageInfo.applicationInfo,
                            appComponentFactory
                    );
                }
            });

            Future buildRunningPlugin = executorService.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    if (!new File(installedApk.apkFilePath).exists()) {
                        throw new LoadPluginException("插件文件不存在.pluginFile==" + installedApk.apkFilePath);
                    }
                    PluginPackageManager pluginPackageManager = buildPackageManager.get();
                    PluginClassLoader pluginClassLoader = buildClassLoader.get();
                    Resources resources = buildResources.get();
                    PluginInfo pluginInfo = buildPluginInfo.get();
                    ShadowApplication shadowApplication = buildApplication.get();
                    ShadowAppComponentFactory appComponentFactory = buildAppComponentFactory.get();

                    lock.lock();
                    try {
                        componentManager.addPluginApkInfo(pluginInfo);
                        pluginPartsMap.put(pluginInfo.getPartKey(), new PluginParts(
                                appComponentFactory,
                                shadowApplication,
                                pluginClassLoader,
                                resources,
                                pluginInfo.getBusinessName(),
                                pluginPackageManager
                        ));

                        PluginPartInfoManager.addPluginInfo(pluginClassLoader, new PluginPartInfo(shadowApplication, resources,
                                pluginClassLoader, pluginPackageManager));
                    } finally {
                        lock.unlock();
                    }
                    return null;
                }
            });

            return buildRunningPlugin;
        }

    }

}
