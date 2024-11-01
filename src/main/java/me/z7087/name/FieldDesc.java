package me.z7087.name;

public final class FieldDesc {
    private final String ownerClass;
    private final String ownerType;
    private final String name;
    private final String type;
    private volatile int hash;

    private FieldDesc(String ownerClass, String name, String returnType) {
        this.ownerClass = ownerClass;
        this.ownerType = AbstractClassGenerator.getClassDescName(ownerClass);
        this.name = name;
        this.type = returnType;
    }

    public String getOwnerClass() {
        return ownerClass;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FieldDesc) {
            FieldDesc that = (FieldDesc) o;
            return ownerClass.equals(that.ownerClass)
                    && name.equals(that.name)
                    && type.equals(that.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            int hc = ownerClass.hashCode()
                    ^ name.hashCode()
                    ^ type.hashCode();
            if (hc == 0)
                hc = -1;
            hash = hc;
        }
        return hash;
    }

    static boolean checkFieldName(String name) {
        if (name == null)
            return false;
        if (name.isEmpty())
            return false;
        for (int p = 0, length = name.length(); p < length; ++p) {
            switch (name.charAt(p)) {
                case '.':
                case ';':
                case '[':
                case '/':
                    return false;
            }
        }
        return true;
    }

    public static FieldDesc of(Class<?> ownerClass, String name, Class<?> type) {
        return FieldDesc.of(
                AbstractClassGenerator.getClassName(ownerClass),
                name,
                AbstractClassGenerator.getClassDescName(type)
        );
    }

    public static FieldDesc of(String ownerClass, String name, String type) {
        if (!MethodDesc.checkClassDesc(ownerClass))
            throw new IllegalArgumentException("OwnerClass name check failed: " + ownerClass);
        if (!checkFieldName(name))
            throw new IllegalArgumentException("Illegal field name: " + name);
        if ("V".equals(type) || !MethodDesc.checkTypeDesc(type))
            throw new IllegalArgumentException("Type descriptor check failed: " + type);
        return new FieldDesc(ownerClass, name, type);
    }

    static FieldDesc ofNoCheck(String ownerClass, String name, String type) {
        return new FieldDesc(ownerClass, name, type);
    }
}
