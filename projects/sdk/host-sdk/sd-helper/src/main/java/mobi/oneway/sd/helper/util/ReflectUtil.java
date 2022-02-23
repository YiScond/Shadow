package mobi.oneway.sd.helper.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {

    private Class mClass;
    private Object o;
    private Method mMethod;
    private Constructor mConstructor;

    private ReflectUtil(Class cl, Object o) {
        if (cl != null) {
            mClass = cl;
        }
        if (o != null) {
            mClass = o.getClass();
            this.o = o;
        }
    }


    public static ReflectUtil with(Object o) {
        return new ReflectUtil(null, o);
    }

    public static ReflectUtil with(Class cl) {
        return new ReflectUtil(cl, null);
    }


    /**
     * 保存持有获得的method
     *
     * @param methodName
     * @param paramterTypes
     * @return
     */
    public ReflectUtil onMethod(String methodName, Class... paramterTypes) throws Exception {
        try {
            mMethod = getMethod(methodName, paramterTypes);
            mMethod.setAccessible(true);
            return this;
        } catch (NoSuchMethodException e) {
            throw e;
        }
    }


    /**
     * 获取method
     *
     * @param methodName
     * @param paramterTypes
     * @return
     */
    public Method getMethod(String methodName, Class... paramterTypes) throws NoSuchMethodException {
        try {
            return mClass.getMethod(methodName, paramterTypes);
        } catch (NoSuchMethodException e) {
            for (Class cl = mClass; cl != null; cl = cl.getSuperclass()) {
                try {
                    return cl.getDeclaredMethod(methodName, paramterTypes);
                } catch (Exception ex) {
                }
            }

            throw e;
        }

    }

    /**
     * 执行method方法
     *
     * @param o
     * @param paramter
     * @param <T>
     * @return
     */
    public <T> T invokeMethod(Object o, Object... paramter) throws Exception {

        try {
            if (mMethod == null) {
                throw new Exception("mMethod is null");
            }
            return (T) mMethod.invoke(o, paramter);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取Field
     *
     * @param fieldName
     * @return
     */
    public Field getField(String fieldName) throws NoSuchFieldException {
        try {
            return mClass.getField(fieldName);
        } catch (NoSuchFieldException e) {
            for (Class cl = mClass; cl != null; cl = cl.getSuperclass()) {
                try {
                    return cl.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ex) {

                }
            }

            throw e;
        }
    }

    /**
     * 获取Field的值
     *
     * @param fieldName
     * @param <T>
     * @return
     */
    public <T> T getFieldValue(String fieldName) throws Exception {
        return getFieldValue(o, fieldName);
    }

    /**
     * 获取Field的值
     *
     * @param o
     * @param fieldName
     * @param <T>
     * @return
     */
    public <T> T getFieldValue(Object o, String fieldName) throws Exception {
        try {
            Field field = getField(fieldName);
            field.setAccessible(true);
            return (T) field.get(o);
        } catch (NoSuchFieldException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        }
    }

    /**
     * 设置Field的值
     *
     * @param fieldName
     * @return
     */
    public void setFieldValue(String fieldName, Object value) throws Exception {
        setFieldValue(o, fieldName, value);
    }


    /**
     * 设置Field的值
     *
     * @param o
     * @param fieldName
     * @return
     */
    public void setFieldValue(Object o, String fieldName, Object value) throws Exception {
        try {
            Field field = getField(fieldName);
            field.setAccessible(true);
            field.set(o, value);
        } catch (NoSuchFieldException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        }
    }


    /**
     * 保存持有构造方法
     *
     * @param paramterTypes
     * @return
     */
    public ReflectUtil onConstructor(Class... paramterTypes) throws Exception {
        try {
            mConstructor = getConstructor(paramterTypes);
            mConstructor.setAccessible(true);
            return this;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取构造方法
     *
     * @param paramterTypes
     * @return
     */
    public Constructor getConstructor(Class... paramterTypes) throws NoSuchMethodException {
        try {
            return mClass.getConstructor(paramterTypes);
        } catch (NoSuchMethodException e) {
            for (Class cl = mClass; cl != null; cl = cl.getSuperclass()) {
                try {
                    return cl.getDeclaredConstructor(paramterTypes);
                } catch (NoSuchMethodException ex) {

                }
            }
            throw e;
        }
    }


    /**
     * 初始化构造方法
     *
     * @param paramter
     * @param <T>
     * @return
     */
    public <T> T newInstance(Object... paramter) throws Exception {
        try {
            if (mConstructor == null) {
                throw new Exception("mConstructor is null");
            }
            return (T) mConstructor.newInstance(paramter);
        } catch (IllegalAccessException e) {
            throw e;
        } catch (InstantiationException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw e;
        }
    }


}
