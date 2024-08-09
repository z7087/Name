package me.z7087.name.api;

// <T:Ljava/lang/Object;R:Ljava/lang/Object;>Ljava/lang/Object;
// (TT;[Ljava/lang/Object;)TR;
// <T:Ljava/lang/Object;R:Ljava/lang/Object;>Ljava/lang/Object;Lme/z7087/name/api/BaseMethodAccessor<TT;TR;>;
public interface BaseMethodAccessor <T> {
    Object invoke(T object, Object... args);

    boolean invokeForBoolean(T object);

    byte invokeForByte(T object);

    char invokeForChar(T object);

    double invokeForDouble(T object);

    float invokeForFloat(T object);

    int invokeForInt(T object);

    long invokeForLong(T object);

    short invokeForShort(T object);

    void invokeForVoid(T object);

    Object invokeForObject(T object);
}
