package me.z7087.name.generatedclasses;

import java.security.ProtectionDomain;

public class AccessorImpl implements Accessor {
    private static void throwException() {
        System.out.println(AccessorImpl.class.getName());
        throw new RuntimeException("1234");
    }

    public ClassLoader getClassLoader(Class<?> clazz) {
        throwException();
        return null;
    }

    public ClassLoader getProtectionDomain(Class<?> clazz) {
        throwException();
        return null;
    }

    // same as getProtectionDomain, but may return null
    public ClassLoader getProtectionDomainOrNull(Class<?> clazz) {
        throwException();
        return null;
    }

    // lookup exists since jdk7
    // lookup with FULL_POWER_MODES if jdk9+ or ALL_MODES if jdk8-
    // throws exception if on jdk13- and className.startsWith("java.lang.invoke.")
    public <T> T getLookupDefault() {
        throwException();
        return null;
    }
    public <T> T getLookupDefault(Class<?> clazz) {
        throwException();
        return null;
    }

    // jdk7 ~ jdk14
    public <T> T getLookupTrustedJ14() {
        throwException();
        return null;
    }
    public <T> T getLookupTrustedJ14(Class<?> clazz) {
        throwException();
        return null;
    }

    // jdk15 ~ jdk22
    public <T> T getLookupTrustedJ15() {
        throwException();
        return null;
    }
    public <T> T getLookupTrustedJ15(Class<?> clazz) {
        throwException();
        return null;
    }

    // jdk7 ~ jdk22
    // Trusted access
    public <T> T getImplLookup() {
        throwException();
        return null;
    }

    public Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) throws ClassFormatError {
        throwException();
        return null;
    }

    // jdk6- ~ jdk11
    public Class<?> defineClassUnsafeJ8(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) {
        throwException();
        return null;
    }

    // jdk9 ~ jdk22+
    public Class<?> defineClassUnsafeJ9(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) {
        throwException();
        return null;
    }

    // jdk7 ~ jdk16
    public Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        throwException();
        return null;
    }

    // jdk15 ~ jdk22+
    // hostClass and the class going to define must have same package
    public Class<?> defineHiddenClass(Class<?> hostClass, byte[] data) {
        throwException();
        return null;
    }
}
