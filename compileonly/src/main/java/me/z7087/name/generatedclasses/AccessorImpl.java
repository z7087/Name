package me.z7087.name.generatedclasses;

import java.security.ProtectionDomain;

public class AccessorImpl implements Accessor {
    private static AbstractMethodError shouldNotReachHere() {
        return new AbstractMethodError(AccessorImpl.class.getName());
    }

    public ClassLoader getClassLoader(Class<?> clazz) {
        throw shouldNotReachHere();
    }

    public ClassLoader getProtectionDomain(Class<?> clazz) {
        throw shouldNotReachHere();
    }

    public ClassLoader getProtectionDomainOrNull(Class<?> clazz) {
        throw shouldNotReachHere();
    }

    public <T> T getLookupDefault() {
        throw shouldNotReachHere();
    }
    public <T> T getLookupDefault(Class<?> clazz) {
        throw shouldNotReachHere();
    }

    public <T> T getLookupTrustedJ14() {
        throw shouldNotReachHere();
    }
    public <T> T getLookupTrustedJ14(Class<?> clazz) {
        throw shouldNotReachHere();
    }

    public <T> T getLookupTrustedJ15() {
        throw shouldNotReachHere();
    }
    public <T> T getLookupTrustedJ15(Class<?> clazz) {
        throw shouldNotReachHere();
    }

    public <T> T getImplLookup() {
        throw shouldNotReachHere();
    }

    public Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) throws ClassFormatError {
        throw shouldNotReachHere();
    }

    public Class<?> defineClassUnsafeJ8(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) {
        throw shouldNotReachHere();
    }

    public Class<?> defineClassUnsafeJ9(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) {
        throw shouldNotReachHere();
    }

    public Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        throw shouldNotReachHere();
    }

    public Class<?> defineHiddenClass(Class<?> hostClass, byte[] data) {
        throw shouldNotReachHere();
    }
}
