package me.z7087.name.api;

public interface BaseFieldAccessorForObject <T, O> {
    O getObject(T object);
    void setObject(T object, O value);
}
