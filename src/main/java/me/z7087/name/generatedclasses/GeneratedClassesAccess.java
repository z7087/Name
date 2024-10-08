package me.z7087.name.generatedclasses;

import me.z7087.name.AccessorClassGenerator;

public final class GeneratedClassesAccess {
    private GeneratedClassesAccess() {
        throwException();
    }

    private static void throwException() {
        throw new RuntimeException("???");
    }

    public static Accessor createAccessorImpl() {
        // Make sure AccessorClassGenerator.class is initializing
        // If it's not initialized, it will be initialized when this code is called
        // If it's initialized, AccessorClassGenerator.isDone() must be true
        if (AccessorClassGenerator.isDone())
            throwException();
        return new AccessorImpl();
    }
}
