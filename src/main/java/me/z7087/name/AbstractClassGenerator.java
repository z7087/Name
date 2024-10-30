package me.z7087.name;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

abstract class AbstractClassGenerator {
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final Map<Class<?>, Byte> PRIMITIVE_CLASS_ID_MAP;
    static {
        Map<Class<?>, Byte> primitiveClassIDMap = new HashMap<Class<?>, Byte>();
        primitiveClassIDMap.put(boolean.class, (byte) 0);
        primitiveClassIDMap.put(byte.class, (byte) 1);
        primitiveClassIDMap.put(char.class, (byte) 2);
        primitiveClassIDMap.put(double.class, (byte) 3);
        primitiveClassIDMap.put(float.class, (byte) 4);
        primitiveClassIDMap.put(int.class, (byte) 5);
        primitiveClassIDMap.put(long.class, (byte) 6);
        primitiveClassIDMap.put(short.class, (byte) 7);
        primitiveClassIDMap.put(void.class, (byte) 8);
        PRIMITIVE_CLASS_ID_MAP = Collections.unmodifiableMap(primitiveClassIDMap);
    }
    private static final Map<String, Byte> PRIMITIVE_TYPE_ID_MAP;
    static {
        Map<String, Byte> primitiveTypeIDMap = new HashMap<String, Byte>();
        primitiveTypeIDMap.put("Z", (byte) 0);
        primitiveTypeIDMap.put("B", (byte) 1);
        primitiveTypeIDMap.put("C", (byte) 2);
        primitiveTypeIDMap.put("D", (byte) 3);
        primitiveTypeIDMap.put("F", (byte) 4);
        primitiveTypeIDMap.put("I", (byte) 5);
        primitiveTypeIDMap.put("J", (byte) 6);
        primitiveTypeIDMap.put("S", (byte) 7);
        primitiveTypeIDMap.put("V", (byte) 8);
        PRIMITIVE_TYPE_ID_MAP = Collections.unmodifiableMap(primitiveTypeIDMap);
    }
    private static final Map<String, String> PRIMITIVE_TYPE_BOXING_MAP;
    static {
        Map<String, String> primitiveTypeBoxingMap = new HashMap<String, String>();
        primitiveTypeBoxingMap.put("Z", "Ljava/lang/Boolean;");
        primitiveTypeBoxingMap.put("B", "Ljava/lang/Byte;");
        primitiveTypeBoxingMap.put("C", "Ljava/lang/Character;");
        primitiveTypeBoxingMap.put("D", "Ljava/lang/Double;");
        primitiveTypeBoxingMap.put("F", "Ljava/lang/Float;");
        primitiveTypeBoxingMap.put("I", "Ljava/lang/Integer;");
        primitiveTypeBoxingMap.put("J", "Ljava/lang/Long;");
        primitiveTypeBoxingMap.put("S", "Ljava/lang/Short;");
        //primitiveTypeBoxingMap.put("V", "Ljava/lang/Void;");
        PRIMITIVE_TYPE_BOXING_MAP = Collections.unmodifiableMap(primitiveTypeBoxingMap);
    }

    protected AbstractClassGenerator() {}

    protected static int getId() {
        return COUNTER.incrementAndGet();
    }

    protected static ClassWriter writeClassHead(String className, String signature, String superName, String[] interfaces) {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_6, ACC_PUBLIC, className, signature, superName, interfaces);
        writeConstructor(cw, superName);
        return cw;
    }

    protected static ClassWriter writeClassHead(String className, String signature, String superName, String[] interfaces, int modifiers, int constructorModifiers) {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_6, modifiers, className, signature, superName, interfaces);
        writeConstructor(cw, superName, constructorModifiers);
        return cw;
    }

    protected static void writeConstructor(ClassWriter cw, String superName) {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    protected static void writeConstructor(ClassWriter cw, String superName, int modifiers) {
        final MethodVisitor mv = cw.visitMethod(modifiers, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    protected static MethodVisitor writeMethodHead(ClassWriter cw, String name, String descriptor) {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, descriptor, null, null);
        mv.visitCode();
        return mv;
    }

    protected static MethodVisitor writeMethodHead(ClassWriter cw, String name, String descriptor, int modifiers) {
        final MethodVisitor mv = cw.visitMethod(modifiers, name, descriptor, null, null);
        mv.visitCode();
        return mv;
    }

    protected static void writeMethodTail(MethodVisitor mv) {
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    protected static byte[] writeClassTail(ClassWriter cw) {
        cw.visitEnd();
        return cw.toByteArray();
    }

    protected static boolean isStatic(int modifiers) {
        return (ACC_STATIC & modifiers) != 0;
    }

    protected static boolean isPrivate(int modifiers) {
        return (ACC_PRIVATE & modifiers) != 0;
    }

    protected static boolean isInterface(int modifiers) {
        return (ACC_INTERFACE & modifiers) != 0;
    }

    protected static String internalize(String className) {
        return className.replace('.', '/');
    }

    protected static String captureName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    protected static String getClassName(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("Unexpected class: class can not be primitive");
        }
        /*
        else if (clazz.isArray()) {
            return clazz.getName();
        }
        */
        return internalize(clazz.getName());
    }

    protected static String getClassName(String classDescName) {
        if (PRIMITIVE_TYPE_ID_MAP.get(classDescName) == null) {
            switch (classDescName.charAt(0)) {
                case '[':
                    return classDescName;
                case 'L':
                    if (classDescName.charAt(classDescName.length() - 1) == ';')
                        return classDescName.substring(1, classDescName.length() - 1);
            }
        }
        throw new IllegalArgumentException("Unexpected type: " + classDescName);
    }

    protected static String getClassDescName(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            final Byte id = PRIMITIVE_CLASS_ID_MAP.get(clazz);
            if (id != null) {
                return getPrimitiveClassDescNameById(id);
            }
            throw new AssertionError();
        } else if (clazz.isArray()) {
            return getClassName(clazz);
        }
        return "L" + getClassName(clazz) + ";";
    }

    protected static String getClassDescName(String className) {
        if (className.charAt(0) == '[')
            return className;
        return "L" + className + ";";
    }

    protected static boolean canBoxTo(String sourceType, String targetType, boolean allowObject) {
        if (sourceType.equals("V"))
            return false;
        final String wrapperType = PRIMITIVE_TYPE_BOXING_MAP.get(sourceType);
        if (wrapperType != null) {
            return (allowObject && targetType.equals("Ljava/lang/Object;"))
                    || targetType.equals(wrapperType);
        }
        return false;
    }

    protected static boolean isParamTypePrimitive(String type) {
        switch (type.charAt(0)) {
            case 'L':
            case '[':
                return false;
            default:
                if (PRIMITIVE_TYPE_BOXING_MAP.get(type) != null)
                    return true;
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    protected static boolean isParamTypeArray(String type) {
        switch (type.charAt(0)) {
            case '[':
                return true;
            case 'L':
                return false;
            default:
                if (PRIMITIVE_TYPE_BOXING_MAP.get(type) != null)
                    return false;
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    protected static String getArrayElementType(String arrayType) {
        if (arrayType.charAt(0) == '[')
            return arrayType.substring(1);

        throw new IllegalArgumentException("type " + arrayType + " is not an array");
    }

    protected static void boxingOnStack(MethodVisitor mv, Class<?> sourceClass) {
        final Byte id = PRIMITIVE_CLASS_ID_MAP.get(sourceClass);
        if (id != null) {
            checkVoid: {
                switch (id) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        break;
                    default:
                        break checkVoid;
                }
                final String wrapperClassName = getPrimitiveWrapperClassNameById(id);
                mv.visitMethodInsn(
                        INVOKESTATIC,
                        wrapperClassName,
                        "valueOf",
                        "(" + getPrimitiveClassDescNameById(id) + ")" + getClassDescName(wrapperClassName),
                        false
                );
                return;
            }
        }
        throw new IllegalArgumentException("Unexpected source class: " + sourceClass.getName());
    }

    protected static void boxingOnStack(MethodVisitor mv, String sourceType) {
        final Byte id = PRIMITIVE_TYPE_ID_MAP.get(sourceType);
        if (id != null) {
            checkVoid: {
                switch (id) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        break;
                    default:
                        break checkVoid;
                }
                final String wrapperClassName = getPrimitiveWrapperClassNameById(id);
                mv.visitMethodInsn(
                        INVOKESTATIC,
                        wrapperClassName,
                        "valueOf",
                        "(" + getPrimitiveClassDescNameById(id) + ")" + getClassDescName(wrapperClassName),
                        false
                );
                return;
            }
        }
        throw new IllegalArgumentException("Unexpected source type: " + sourceType);
    }

    protected static void unboxingOnStack(MethodVisitor mv, Class<?> targetClass) {
        unboxingOnStack(mv, targetClass, true);
    }

    protected static void unboxingOnStack(MethodVisitor mv, Class<?> targetClass, boolean checkCast) {
        final Byte id = PRIMITIVE_CLASS_ID_MAP.get(targetClass);
        if (id != null) {
            checkVoid: {
                switch (id) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        break;
                    default:
                        break checkVoid;
                }
                final String wrapperClassName = getPrimitiveWrapperClassNameById(id);
                if (checkCast)
                    mv.visitTypeInsn(CHECKCAST, wrapperClassName);
                mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        wrapperClassName,
                        getPrimitiveClassTypeNameById(id) + "Value",
                        "()" + getPrimitiveClassDescNameById(id),
                        false
                );
                return;
            }
        }
        throw new IllegalArgumentException("Unexpected target class: " + targetClass.getName());
    }

    protected static void unboxingOnStack(MethodVisitor mv, String targetType) {
        unboxingOnStack(mv, targetType, true);
    }

    protected static void unboxingOnStack(MethodVisitor mv, String targetType, boolean checkCast) {
        final Byte id = PRIMITIVE_TYPE_ID_MAP.get(targetType);
        if (id != null) {
            checkVoid: {
                switch (id) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        break;
                    default:
                        break checkVoid;
                }
                final String wrapperClassName = getPrimitiveWrapperClassNameById(id);
                if (checkCast)
                    mv.visitTypeInsn(CHECKCAST, wrapperClassName);
                mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        wrapperClassName,
                        getPrimitiveClassTypeNameById(id) + "Value",
                        "()" + getPrimitiveClassDescNameById(id),
                        false
                );
                return;
            }
        }
        throw new IllegalArgumentException("Unexpected target type: " + targetType);
    }

    private static String getPrimitiveClassTypeNameById(byte id) {
        switch (id) {
            case 0:
                return "boolean";
            case 1:
                return "byte";
            case 2:
                return "char";
            case 3:
                return "double";
            case 4:
                return "float";
            case 5:
                return "int";
            case 6:
                return "long";
            case 7:
                return "short";
            case 8:
                return "void";
        }
        throw new AssertionError();
    }

    private static String getPrimitiveClassDescNameById(byte id) {
        switch (id) {
            case 0:
                return "Z";
            case 1:
                return "B";
            case 2:
                return "C";
            case 3:
                return "D";
            case 4:
                return "F";
            case 5:
                return "I";
            case 6:
                return "J";
            case 7:
                return "S";
            case 8:
                return "V";
        }
        throw new AssertionError();
    }

    private static String getPrimitiveWrapperClassNameById(byte id) {
        switch (id) {
            case 0:
                return getClassName(Boolean.class);
            case 1:
                return getClassName(Byte.class);
            case 2:
                return getClassName(Character.class);
            case 3:
                return getClassName(Double.class);
            case 4:
                return getClassName(Float.class);
            case 5:
                return getClassName(Integer.class);
            case 6:
                return getClassName(Long.class);
            case 7:
                return getClassName(Short.class);
            case 8:
                return getClassName(Void.class);
        }
        throw new AssertionError();
    }

    protected static void shouldNotReachHere() {
        throw new AssertionError("Should not reach here");
    }

    protected static void throwIllegalArgumentException(String reason) {
        throw new IllegalArgumentException(reason);
    }

    protected static void throwNoSuchFieldException(FieldDesc desc) throws NoSuchFieldException {
        String sb = desc.getOwnerClass() +
                "." +
                desc.getName() +
                " " +
                desc.getType();
        throw new NoSuchFieldException(sb);
    }

    protected static void throwNoSuchMethodException(MethodDesc desc) throws NoSuchMethodException {
        String sb = desc.getOwnerClass() +
                "." +
                desc.getName() +
                " (" +
                desc.getMergedParamTypes() +
                ")" +
                desc.getReturnType();
        throw new NoSuchMethodException(sb);
    }

    protected static void findField(ClassLoader loader, FieldDesc desc, boolean isStatic) throws NoSuchFieldException {
        try {
            for (java.lang.reflect.Field reflectField : Class.forName(desc.getOwnerClass().replace('/', '.'), false, loader).getDeclaredFields()) {
                if (reflectField.getName().equals(desc.getName())
                        && (isStatic == isStatic(reflectField.getModifiers()))
                        && getClassDescName(reflectField.getType()).equals(desc.getType())
                ) {
                    return;
                }
            }
        } catch (ClassNotFoundException ignored) {
        }
        throwNoSuchFieldException(desc);
    }

    protected static void findMethod(ClassLoader loader, MethodDesc desc) throws NoSuchMethodException {
        try {
            for (java.lang.reflect.Method reflectMethod : Class.forName(desc.getOwnerClass().replace('/', '.'), false, loader).getMethods()) {
                if (reflectMethod.getName().equals(desc.getName())
                        && !isStatic(reflectMethod.getModifiers())
                        && getClassDescName(reflectMethod.getReturnType()).equals(desc.getReturnType())
                ) {
                    notFound:
                    {
                        final Class<?>[] reflectParamTypes = reflectMethod.getParameterTypes();
                        for (int i = 0, length = reflectParamTypes.length; i < length; ++i) {
                            if (!getClassDescName(reflectParamTypes[i]).equals(desc.getParamTypesRaw()[i])) {
                                break notFound;
                            }
                        }
                        return;
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {
        }
        throwNoSuchMethodException(desc);
    }

    protected static boolean canCheckCastOrBox(String fromType, String toType) {
        // types are same
        if (fromType.equals(toType))
            return true;
        if (isParamTypePrimitive(fromType)) {
            if (isParamTypePrimitive(toType)) {
                // 2 primitives and fromType != toType
                return false;
            } else {
                // box
                return canBoxTo(fromType, toType, true);
            }
        } else {
            if (isParamTypePrimitive(toType)) {
                // unbox
                return canBoxTo(toType, fromType, true);
            } else {
                // 2 objects and fromType != toType
                // allow if one of them is java.lang.Object
                return "Ljava/lang/Object;".equals(fromType) || "Ljava/lang/Object;".equals(toType);
            }
        }
    }

    protected static void checkCastOrBox(MethodVisitor mv, String fromType, String toType) {
        // types are same
        if (fromType.equals(toType))
            return;
        if (isParamTypePrimitive(fromType)) {
            if (isParamTypePrimitive(toType)) {
                // 2 primitives and fromType != toType
                shouldNotReachHere();
            } else {
                // box
                boxingOnStack(mv, fromType);
            }
        } else {
            if (isParamTypePrimitive(toType)) {
                // unbox
                unboxingOnStack(mv, toType);
            } else {
                // 2 objects and fromType != toType
                //noinspection StatementWithEmptyBody
                if ("Ljava/lang/Object;".equals(toType)) {
                    // fall down
                } else if ("Ljava/lang/Object;".equals(fromType)) {
                    mv.visitTypeInsn(CHECKCAST, getClassName(toType));
                } else {
                    shouldNotReachHere();
                }
            }
        }
    }

    protected static boolean knownNoNeedToCheckCast(String fromType, String toType) {
        if (fromType.equals(toType))
            return true;
        return "Ljava/lang/Object;".equals(toType);
    }
}
