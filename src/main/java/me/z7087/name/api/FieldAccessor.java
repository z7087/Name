package me.z7087.name.api;

public interface FieldAccessor <T, O> {
    O get(T object);
    void set(T object, O value);
}
