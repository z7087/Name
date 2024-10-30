package me.z7087.name;

import me.z7087.name.util.JavaVersion;
import me.z7087.name.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;

// this class allows users define classes unsafely, classloader can be null.
final class UnsafeClassDefiner {
    public static Class<?> define(String className, byte[] classByteArray) {
        return define(className, classByteArray, null, null);
    }
    public static Class<?> define(String className, byte[] classByteArray,
                                  ClassLoader loader, ProtectionDomain protectionDomain) {
        if (JavaVersion.getInstance().getVersion() <= 10) {
            try {
                assert ReflectionUtil.theUnsafe != null;
                // defineClass method in sun.mise.Unsafe still exists
                return ReflectionUtil.theUnsafe.defineClass(className, classByteArray, 0, classByteArray.length, loader, protectionDomain);
            } catch (NoSuchMethodError e) {
                // or not?
            }
        }
        // we have to do some hacky stuff
        synchronized (UnsafeClassDefiner.class) {
            try {
                return UnsafeClassDefinerJ11.defineJ11(className, classByteArray, loader, protectionDomain);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Class<?> defineHidden(Class<?> hostClass, byte[] classByteArray) {
        if (JavaVersion.getInstance().getVersion() <= 16) {
            try {
                // defineAnonymousClass method in sun.mise.Unsafe still exists
                return AccessorClassGenerator.getInstance().getGeneratedAccessorInstance().defineAnonymousClass(hostClass, classByteArray, null);
            } catch (NoSuchMethodError e) {
                // or not?
            }
        }
        return AccessorClassGenerator.getInstance().getGeneratedAccessorInstance().defineHiddenClass(hostClass, classByteArray);
    }
    private static final class UnsafeClassDefinerJ11 {
        public static Class<?> defineJ11(String className, byte[] classByteArray,
                                         ClassLoader loader,
                                         ProtectionDomain protectionDomain)
                throws NoSuchFieldException, IllegalAccessException,
                NoSuchMethodException, InvocationTargetException {
            // https://zer0peach.github.io/2023/12/28/JDK17-%E7%BB%95%E8%BF%87%E5%8F%8D%E5%B0%84%E9%99%90%E5%88%B6
            assert ReflectionUtil.theUnsafe != null;

            final Object theInternalUnsafe;
            {
                @SuppressWarnings("JavaReflectionMemberAccess")
                final java.lang.reflect.Field theInternalUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theInternalUnsafe");
                theInternalUnsafeField.setAccessible(true);
                theInternalUnsafe = theInternalUnsafeField.get(null);
            }
            @SuppressWarnings("JavaReflectionMemberAccess")
            final long fieldModuleOffset = ReflectionUtil.theUnsafe.objectFieldOffset(Class.class.getDeclaredField("module"));
            final Object originModule = ReflectionUtil.theUnsafe.getObject(UnsafeClassDefinerJ11.class, fieldModuleOffset);
            ReflectionUtil.theUnsafe.putObject(UnsafeClassDefinerJ11.class, fieldModuleOffset, ReflectionUtil.theUnsafe.getObject(Class.class, fieldModuleOffset));
            try {
                java.lang.reflect.Method defineClassMethod = theInternalUnsafe.getClass().getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                //defineClassMethod.setAccessible(true);
                return (Class<?>) defineClassMethod.invoke(theInternalUnsafe, className, classByteArray, 0, classByteArray.length, loader, protectionDomain);
            } finally {
                ReflectionUtil.theUnsafe.putObject(UnsafeClassDefinerJ11.class, fieldModuleOffset, originModule);
            }
        }
    }
}
