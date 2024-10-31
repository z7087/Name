package me.z7087.name.generatedclasses;

import java.security.ProtectionDomain;

public interface Accessor {
    ClassLoader getClassLoader(Class<?> clazz);

    ClassLoader getProtectionDomain(Class<?> clazz);

    // same as getProtectionDomain, but may return null
    ClassLoader getProtectionDomainOrNull(Class<?> clazz);

    // lookup exists since jdk7
    // lookup with FULL_POWER_MODES if jdk9+ or ALL_MODES if jdk8-
    // throws exception if on jdk13- and className.startsWith("java.lang.invoke.")
    <T> T getLookupDefault();
    <T> T getLookupDefault(Class<?> clazz);

    // jdk7 ~ jdk14
    <T> T getLookupTrustedJ14();
    <T> T getLookupTrustedJ14(Class<?> clazz);

    // jdk15 ~ jdk22
    <T> T getLookupTrustedJ15();
    <T> T getLookupTrustedJ15(Class<?> clazz);

    // jdk7 ~ jdk22
    // Trusted access
    <T> T getImplLookup();

    Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) throws ClassFormatError;

    // jdk6- ~ jdk11
    Class<?> defineClassUnsafeJ8(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain);

    // jdk9 ~ jdk22+
    Class<?> defineClassUnsafeJ9(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain);

    // jdk7 ~ jdk16
    Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches);

    // jdk15 ~ jdk22+
    // hostClass and the class going to define must have same package
    Class<?> defineHiddenClass(Class<?> hostClass, byte[] data);
}
