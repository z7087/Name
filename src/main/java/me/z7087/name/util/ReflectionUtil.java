package me.z7087.name.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil {
    public static final Unsafe theUnsafe = (Unsafe) getFieldValue(Unsafe.class, "theUnsafe", null);

    private static volatile Class<?> GeneratedClassForExtending;

    private static Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            final Method m = clazz.getDeclaredMethod(name, paramTypes);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            final Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getFieldValue(Class<?> clazz, String name, Object obj) {
        try {
            final Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private ReflectionUtil() {}
}
