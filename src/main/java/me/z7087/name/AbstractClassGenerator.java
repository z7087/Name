package me.z7087.name;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

abstract class AbstractClassGenerator {
    private static final AtomicInteger Counter = new AtomicInteger();

    protected AbstractClassGenerator() {}

    protected static int getId() {
        return Counter.incrementAndGet();
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
        if (clazz == boolean.class)
            return "Z";
        if (clazz == byte.class)
            return "B";
        if (clazz == char.class)
            return "C";
        if (clazz == double.class)
            return "D";
        if (clazz == float.class)
            return "F";
        if (clazz == int.class)
            return "I";
        if (clazz == long.class)
            return "J";
        if (clazz == short.class)
            return "S";
        if (clazz == void.class)
            return "V";
        return internalize(clazz.getName());
    }

    protected static String getClassDescName(Class<?> clazz) {
        if (clazz.isPrimitive() || clazz.isArray()) {
            return getClassName(clazz);
        }
        return "L" + getClassName(clazz) + ";";
    }

    protected static void boxingOnStack(MethodVisitor mv, Class<?> sourceClass) {
        final String className;
        if (sourceClass == boolean.class) {
            className = internalize(Boolean.class.getName());
            mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(Z)L" + className + ";", false);
        } else if (sourceClass == byte.class) {
            className = internalize(Byte.class.getName());
            mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(B)L" + className + ";", false);
        } else if (sourceClass == char.class) {
            className = internalize(Character.class.getName());
            mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(C)L" + className + ";", false);
        } else if (sourceClass == double.class) {
            className = internalize(Double.class.getName());
            mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(D)L" + className + ";", false);
        } else if (sourceClass == float.class) {
            className = internalize(Float.class.getName());
            mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(F)L" + className + ";", false);
        } else if (sourceClass == int.class) {
            className = internalize(Integer.class.getName());
            mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(I)L" + className + ";", false);
        } else if (sourceClass == long.class) {
            className = internalize(Long.class.getName());
            mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(J)L" + className + ";", false);
        } else if (sourceClass == short.class) {
            className = internalize(Short.class.getName());
            mv.visitMethodInsn(INVOKESTATIC, className, "valueOf", "(S)L" + className + ";", false);
        } else {
            throw new IllegalArgumentException("Unexpected source class");
        }
    }

    protected static void unboxingOnStack(MethodVisitor mv, Class<?> targetClass) {
        final String className;
        if (targetClass == boolean.class) {
            className = internalize(Boolean.class.getName());
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "booleanValue", "()Z", false);
        } else if (targetClass == byte.class) {
            className = internalize(Byte.class.getName());
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "byteValue", "()B", false);
        } else if (targetClass == char.class) {
            className = internalize(Character.class.getName());
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "charValue", "()C", false);
        } else if (targetClass == double.class) {
            className = internalize(Double.class.getName());
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "doubleValue", "()D", false);
        } else if (targetClass == float.class) {
            className = internalize(Float.class.getName());
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "floatValue", "()F", false);
        } else if (targetClass == int.class) {
            className = internalize(Integer.class.getName());
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "intValue", "()I", false);
        } else if (targetClass == long.class) {
            className = internalize(Long.class.getName());
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "longValue", "()J", false);
        } else if (targetClass == short.class) {
            className = internalize(Short.class.getName());
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "shortValue", "()S", false);
        } else {
            throw new IllegalArgumentException("Unexpected target class");
        }
    }
}
