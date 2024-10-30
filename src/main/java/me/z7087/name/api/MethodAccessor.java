package me.z7087.name.api;

public interface MethodAccessor <T, O> {
    O invoke(T object, Object... args);
}
