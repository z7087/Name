package me.z7087.name;

import java.util.Arrays;

public final class ConstructorDesc {
    private final String actualOwnerClass;
    private final String actualOwnerType;
    private final String constructorOwnerClass;
    private final String constructorOwnerType;
    private final String[] paramTypes;
    private final String mergedParamTypes;
    private volatile int hash;

    private ConstructorDesc(String actualOwnerClass, String constructorOwnerClass, String[] paramTypes) {
        this.actualOwnerClass = actualOwnerClass;
        this.actualOwnerType = AbstractClassGenerator.getClassDescName(actualOwnerClass);
        this.constructorOwnerClass = constructorOwnerClass;
        this.constructorOwnerType = AbstractClassGenerator.getClassDescName(constructorOwnerClass);
        this.paramTypes = paramTypes;
        {
            final StringBuilder sb = new StringBuilder();
            for (String paramType : paramTypes) {
                sb.append(paramType);
            }
            this.mergedParamTypes = sb.toString();
        }
    }

    public String getActualOwnerClass() {
        return actualOwnerClass;
    }

    public String getActualOwnerType() {
        return actualOwnerType;
    }

    public String getConstructorOwnerClass() {
        return constructorOwnerClass;
    }

    public String getConstructorOwnerType() {
        return constructorOwnerType;
    }

    public String[] getParamTypes() {
        return paramTypes.clone();
    }

    String[] getParamTypesRaw() {
        return paramTypes;
    }

    public String getMergedParamTypes() {
        return mergedParamTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ConstructorDesc) {
            ConstructorDesc that = (ConstructorDesc) o;
            return actualOwnerClass.equals(that.actualOwnerClass)
                    && constructorOwnerClass.equals(that.constructorOwnerClass)
                    && mergedParamTypes.equals(that.mergedParamTypes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            int hc = actualOwnerClass.hashCode()
                    ^ constructorOwnerClass.hashCode()
                    ^ mergedParamTypes.hashCode();
            if (hc == 0)
                hc = -1;
            hash = hc;
        }
        return hash;
    }

    public static ConstructorDesc of(Class<?> actualOwnerClass, Class<?> constructorOwnerClass, Class<?>... paramTypes) {
        final String[] paramTypesStr;
        if (paramTypes != null) {
            final int length = paramTypes.length;
            paramTypesStr = new String[length];
            for (int i = 0; i < length; ++i) {
                paramTypesStr[i] = AbstractClassGenerator.getClassDescName(paramTypes[i]);
            }
        } else {
            paramTypesStr = null;
        }
        return ConstructorDesc.of(
                AbstractClassGenerator.getClassName(actualOwnerClass),
                AbstractClassGenerator.getClassName(constructorOwnerClass),
                paramTypesStr
        );
    }

    public static ConstructorDesc of(String actualOwnerClass, String constructorOwnerClass, String... paramTypes) {
        if (!MethodDesc.checkClassDesc(actualOwnerClass) || actualOwnerClass.charAt(0) == '[')
            throw new IllegalArgumentException("ActualOwnerClass name check failed: " + actualOwnerClass);
        if (!MethodDesc.checkClassDesc(constructorOwnerClass) || constructorOwnerClass.charAt(0) == '[')
            throw new IllegalArgumentException("ConstructorOwnerClass name check failed: " + constructorOwnerClass);
        if (paramTypes != null && paramTypes.length != 0)
            paramTypes = paramTypes.clone();
        else
            paramTypes = MethodDesc.EMPTY_STRING_ARRAY;
        if (!MethodDesc.checkTypesDesc(paramTypes))
            throw new IllegalArgumentException("ParamTypes descriptor check failed: " + Arrays.toString(paramTypes));
        return new ConstructorDesc(actualOwnerClass, constructorOwnerClass, paramTypes);
    }

    static ConstructorDesc ofNoCheck(String actualOwnerClass, String constructorOwnerClass, String[] paramTypes) {
        return new ConstructorDesc(actualOwnerClass, constructorOwnerClass, paramTypes);
    }
}
