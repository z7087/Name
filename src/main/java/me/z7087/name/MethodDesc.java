package me.z7087.name;

import java.util.Arrays;

public final class MethodDesc {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final String ownerClass;
    private final String ownerType;
    private final String name;
    private final String returnType;
    private final String[] paramTypes;
    private final String mergedParamTypes;
    private volatile int hash;

    private MethodDesc(String ownerClass, String name, String returnType, String[] paramTypes) {
        this.ownerClass = ownerClass;
        this.ownerType = AbstractClassGenerator.getClassDescName(ownerClass);
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
        {
            final StringBuilder sb = new StringBuilder();
            for (String paramType : paramTypes) {
                sb.append(paramType);
            }
            this.mergedParamTypes = sb.toString();
        }
    }

    public String getOwnerClass() {
        return ownerClass;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
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

    public String getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MethodDesc) {
            MethodDesc that = (MethodDesc) o;
            return ownerClass.equals(that.ownerClass)
                    && name.equals(that.name)
                    && returnType.equals(that.returnType)
                    && mergedParamTypes.equals(that.mergedParamTypes);
        }
        return false;
    }

    public boolean equalsDescriptor(Object o) {
        if (this == o) return true;
        if (o instanceof MethodDesc) {
            MethodDesc that = (MethodDesc) o;
            return name.equals(that.name)
                    && returnType.equals(that.returnType)
                    && mergedParamTypes.equals(that.mergedParamTypes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            int hc = ownerClass.hashCode()
                    ^ name.hashCode()
                    ^ returnType.hashCode()
                    ^ mergedParamTypes.hashCode();
            if (hc == 0)
                hc = -1;
            hash = hc;
        }
        return hash;
    }

    static boolean checkClassDesc(String desc) {
        if (desc == null)
            return false;
        final int length = desc.length();
        if (length == 0)
            return false;
        int p = 0;
        if (desc.charAt(p) == '[') {
            // array class
            do {
                ++p;
                if (length == p)
                    return false;
            } while (desc.charAt(p) == '[');
            switch (desc.charAt(p)) {
                case 'L': {
                    boolean hasChar = false;
                    for (++p; p < length; ++p) {
                        switch (desc.charAt(p)) {
                            case '.':
                            case '[':
                                return false;
                            case ';':
                                return hasChar && length - p == 1;
                            default:
                                hasChar = true;
                        }
                    }
                    return false;
                }
                case 'Z':
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                    return length - p == 1;
                case 'V':
                    // void array doesn't exists
                default:
                    return false;
            }
        } else {
            // class
            for (; p < length; ++p) {
                switch (desc.charAt(p)) {
                    case '.':
                    case ';':
                    case '[':
                        return false;
                }
            }
            return true;
        }
    }

    static boolean checkTypeDesc(String desc) {
        if (desc == null)
            return false;
        final int length = desc.length();
        if (length == 0)
            return false;
        int p = 0;
        // check for [[[[[[I
        while (desc.charAt(p) == '[') {
            ++p;
            if (length == p)
                return false;
        }
        switch (desc.charAt(p)) {
            case 'L': {
                boolean hasChar = false;
                for (++p; p < length; ++p) {
                    switch (desc.charAt(p)) {
                        case '.':
                        case '[':
                            return false;
                        case ';':
                            return hasChar && length - p == 1;
                        default:
                            hasChar = true;
                    }
                }
                return false;
            }
            case 'Z':
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
                return length - p == 1;
            case 'V':
                // void array doesn't exists
                return p == 0 && length - p == 1;
            default:
                return false;
        }
    }

    static boolean checkTypesDesc(String[] descriptors) {
        if (descriptors == null)
            return false;
        for (String desc : descriptors) {
            if (desc == null || desc.equals("V") || !checkTypeDesc(desc))
                return false;
        }
        return true;
    }

    static boolean checkTypesDesc(String desc) {
        if (desc == null)
            return false;
        for (int p = 0, length = desc.length(); p < length; ++p) {
            while (desc.charAt(p) == '[') {
                ++p;
                if (length == p)
                    return false;
            }
            switch (desc.charAt(p)) {
                case 'L': {
                    boolean hasChar = false;
                    loop: {
                        for (++p; p < length; ++p) {
                            switch (desc.charAt(p)) {
                                case '.':
                                case '[':
                                    return false;
                                case ';':
                                    if (!hasChar)
                                        return false;
                                    break loop;
                                default:
                                    hasChar = true;
                            }
                        }
                        return false;
                    }
                    break;
                }
                case 'Z':
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                    break;
                case 'V':
                    // void cannot be parameter
                default:
                    return false;
            }
        }
        return true;
    }

    static boolean checkMethodName(String name) {
        if (name == null)
            return false;
        if (name.isEmpty())
            return false;
        if (name.equals("<init>") || name.equals("<clinit>"))
            return true;
        for (int p = 0, length = name.length(); p < length; ++p) {
            switch (name.charAt(p)) {
                case '.':
                case ';':
                case '[':
                case '/':
                case '<':
                case '>':
                    return false;
            }
        }
        return true;
    }

    public static MethodDesc of(Class<?> ownerClass, String name, Class<?> returnType, Class<?>... paramTypes) {
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
        return MethodDesc.of(
                AbstractClassGenerator.getClassName(ownerClass),
                name,
                AbstractClassGenerator.getClassDescName(returnType),
                paramTypesStr
        );
    }

    public static MethodDesc of(String ownerClass, String name, String returnType, String... paramTypes) {
        if (!checkClassDesc(ownerClass))
            throw new IllegalArgumentException("OwnerClass descriptor check failed: " + ownerClass);
        if (!checkMethodName(name))
            throw new IllegalArgumentException("Illegal method name: " + name);
        if (!checkTypeDesc(returnType))
            throw new IllegalArgumentException("ReturnType descriptor check failed: " + returnType);
        if (paramTypes != null && paramTypes.length != 0)
            paramTypes = paramTypes.clone();
        else
            paramTypes = EMPTY_STRING_ARRAY;
        if (!checkTypesDesc(paramTypes))
            throw new IllegalArgumentException("ParamTypes descriptor check failed: " + Arrays.toString(paramTypes));
        return new MethodDesc(ownerClass, name, returnType, paramTypes);
    }

    static MethodDesc ofNoCheck(String ownerClass, String name, String returnType, String[] paramTypes) {
        return new MethodDesc(ownerClass, name, returnType, paramTypes);
    }
}
