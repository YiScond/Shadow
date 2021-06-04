package mobi.oneway.sd.core.loader.classloaders;

import android.os.Build;

import java.io.File;
import java.util.Arrays;

import dalvik.system.BaseDexClassLoader;

public class PluginClassLoader extends BaseDexClassLoader {


    private ClassLoader specialClassLoader;
    private ClassLoader loaderClassLoader = PluginClassLoader.class.getClassLoader();
    private String[] allHostWhiteList;

    public PluginClassLoader(String dexPath,
                             File optimizedDirectory,
                             String librarySearchPath,
                             ClassLoader parent,
                             ClassLoader specialClassLoader,
                             String[] hostWhiteList) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        this.specialClassLoader = specialClassLoader;
        initWhiteList(hostWhiteList);
    }

    private void initWhiteList(String[] hostWhiteList) {
        allHostWhiteList = new String[]{"org.apache.commons.logging"};//org.apache.commons.logging是非常特殊的的包,由系统放到App的PathClassLoader中.
        if (hostWhiteList != null) {
            int thisSize = allHostWhiteList.length;
            int arraySize = hostWhiteList.length;
            String[] newArray = Arrays.copyOf(allHostWhiteList, thisSize + arraySize);
            System.arraycopy(hostWhiteList, 0, newArray, thisSize, arraySize);
            allHostWhiteList = newArray;
        }
    }


    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        if (specialClassLoader == null) {//specialClassLoader 为null 表示该classLoader依赖了其他的插件classLoader，需要遵循双亲委派
            return super.loadClass(className, resolve);
        } else if (subStringBeforeDot(className).equals("mobi.oneway.sd.core.runtime")) {
            return loaderClassLoader.loadClass(className);
        } else if (inPackage(className, allHostWhiteList)
                || (Build.VERSION.SDK_INT < 28 && className.startsWith("org.apache.http"))) {//Android 9.0以下的系统里面带有http包，走系统的不走本地的) {
            return super.loadClass(className, resolve);
        } else {
            Class clazz = findLoadedClass(className);

            if (clazz == null) {
                ClassNotFoundException suppressed = null;
                try {
                    clazz = findClass(className);
                } catch (ClassNotFoundException e) {
                    suppressed = e;
                }
                if (clazz == null) {
                    try {
                        clazz = specialClassLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            e.addSuppressed(suppressed);
                        }
                        throw e;
                    }

                }
            }

            return clazz;
        }
    }


    private boolean inPackage(String className, String[] packageNames) {
        String packageName = subStringBeforeDot(className);

        if (packageNames == null) {
            return false;
        }

        for (String itemPackageName : packageNames) {
            if (itemPackageName.equals("")) {
            } else if (itemPackageName.equals(".*")) {
            } else if (itemPackageName.equals(".**")) {
            } else if (itemPackageName.endsWith(".*")) {
                //只允许一级子包
                String sub = subStringBeforeDot(packageName);
                if (!sub.isEmpty() && sub.equals(subStringBeforeDot(itemPackageName))) {
                    return true;
                }
            } else if (itemPackageName.endsWith(".**")) {
                //允许所有子包
                String sub = subStringBeforeDot(packageName);
                if (!sub.isEmpty() && sub.startsWith(subStringBeforeDot(itemPackageName) + ".")) {
                    return true;
                }
            } else {
                if (packageName.equals(itemPackageName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String subStringBeforeDot(String className) {
        int index = className.lastIndexOf(".");
        if (index == -1) {
            return "";
        } else {
            return className.substring(0, index);
        }
    }
}
