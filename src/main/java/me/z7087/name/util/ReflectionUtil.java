package me.z7087.name.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil {
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

    private ReflectionUtil() {}
}
