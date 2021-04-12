package com.tencent.shadow.core.loader.classloaders;

import android.os.Build;

public class CombineClassLoader extends ClassLoader {

    private ClassLoader[] classLoaders;

    public CombineClassLoader(ClassLoader[] classLoaders, ClassLoader parent) {
        super(parent);
        this.classLoaders = classLoaders;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        ClassNotFoundException classNotFoundException = new ClassNotFoundException(name);
        if (c == null) {
            try {
                c = super.loadClass(name, resolve);
            } catch (ClassNotFoundException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    classNotFoundException.addSuppressed(e);
                }
            }

            if (c == null) {
                if (classLoaders != null) {
                    for (ClassLoader classLoader : classLoaders) {
                        try {
                            c = classLoader.loadClass(name);
                            break;
                        } catch (ClassNotFoundException e) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                classNotFoundException.addSuppressed(e);
                            }
                        }
                    }
                }
                if (c == null) {
                    throw classNotFoundException;
                }
            }
        }
        return c;
    }
}
