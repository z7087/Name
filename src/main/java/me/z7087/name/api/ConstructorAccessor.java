package me.z7087.name.api;

public interface ConstructorAccessor <O> {
    O newInstance(Object... args);
}
