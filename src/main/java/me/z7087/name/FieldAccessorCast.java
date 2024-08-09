package me.z7087.name;

import me.z7087.name.api.BaseFieldAccessor;
import me.z7087.name.api.FieldAccessor;
import me.z7087.name.api.FieldAccessorNO;
import me.z7087.name.api.FieldAccessorO;

public class FieldAccessorCast {
    private FieldAccessorCast() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> FieldAccessor<T> toFieldAccessor(BaseFieldAccessor<T, ?> baseFieldAccessor) {
        return (FieldAccessor) baseFieldAccessor;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> FieldAccessorNO<T> toFieldAccessorNO(BaseFieldAccessor<T, ?> baseFieldAccessor) {
        return (FieldAccessorNO) baseFieldAccessor;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T, O> FieldAccessorO<T, O> toFieldAccessorO(BaseFieldAccessor<T, ?> baseFieldAccessor) {
        return (FieldAccessorO) baseFieldAccessor;
    }
}
