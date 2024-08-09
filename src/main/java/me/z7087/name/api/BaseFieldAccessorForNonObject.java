package me.z7087.name.api;

public interface BaseFieldAccessorForNonObject <T> {
    boolean getBoolean(T object);

    byte getByte(T object);

    char getChar(T object);

    double getDouble(T object);

    float getFloat(T object);

    int getInt(T object);

    long getLong(T object);

    short getShort(T object);

    void setBoolean(T object, boolean value);

    void setByte(T object, byte value);

    void setChar(T object, char value);

    void setDouble(T object, double value);

    void setFloat(T object, float value);

    void setInt(T object, int value);

    void setLong(T object, long value);

    void setShort(T object, short value);
}
