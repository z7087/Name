package me.z7087.name.util;

import sun.misc.Unsafe;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ReflectionUtil {
    //public static final Method M_Constructor_getConstructorAccessor = getMethod(Constructor.class, "getConstructorAccessor");
    //public static final Method M_ClassLoader_defineClass = getMethod(ClassLoader.class, "defineClass", byte[].class, int.class, int.class);
    //public static final Field F_ObjectStreamClass_cons = getField(ObjectStreamClass.class, "cons");
    public static final Unsafe theUnsafe = getFieldValue(Unsafe.class, "theUnsafe", null);

    private static volatile Class<?> GeneratedClassForExtending;

    private static Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            final Method m = clazz.getDeclaredMethod(name, paramTypes);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            final Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> T getFieldValue(Class<?> clazz, String name, Object obj) {
        try {
            final Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Deprecated
//    public static Class<?> getGeneratedClassForExtending() throws IllegalAccessException, InvocationTargetException {
//        if (GeneratedClassForExtending == null) {
//            synchronized (ReflectionUtil.class) {
//                if (GeneratedClassForExtending == null) {
//                    final ObjectStreamClass osc = ObjectStreamClass.lookup(ReflectionUtil.class);
//                    final Object constructorAccessor = M_Constructor_getConstructorAccessor.invoke(F_ObjectStreamClass_cons.get(osc), (Object[]) null);
//                    GeneratedClassForExtending = constructorAccessor.getClass();
//                }
//            }
//        }
//        return GeneratedClassForExtending;
//    }

    private ReflectionUtil() {}
}
