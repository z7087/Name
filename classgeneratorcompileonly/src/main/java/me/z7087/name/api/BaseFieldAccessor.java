package me.z7087.name.api;

public interface BaseFieldAccessor <T, O> {
    O get(T object);
    void set(T object, O value);
}
