package me.z7087.name;

import me.z7087.name.api.*;
import me.z7087.name.generatedclasses.Here;
import me.z7087.name.util.ReflectionUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;

public class FieldFactory extends AbstractClassGenerator {
    protected FieldFactory() {}
    private static <T> FieldAccessor<T> createField(String name, Class<T> ownerClass, int modifiers,
                                                         Class<?> type, boolean checkExist)
            throws NoSuchFieldException, NoSuchMethodException {
        if (ownerClass.isPrimitive())
            throw new IllegalArgumentException("Owner class cannot be primitive");
        if (checkExist) {
            foundField:
            {
                for (java.lang.reflect.Field reflectField : ownerClass.getDeclaredFields()) {
                    if (reflectField.getName().equals(name)
                            && reflectField.getModifiers() == modifiers
                            && reflectField.getType() == type
                    ) {
                        break foundField;
                    }
                }
                throw new NoSuchFieldException(name);
            }
        }
        if (type.isPrimitive() && type == void.class)
            throw new IllegalArgumentException("Void can not be field type");
        final Class<?> superClass = AccessorClassGenerator.getInstance().getGeneratedDoorClass();
        final int id = getId();
        final String className;
        {
            String generatedClassesPath = internalize(Here.class.getName());
            generatedClassesPath = generatedClassesPath.substring(0,
                    generatedClassesPath.lastIndexOf(Here.class.getSimpleName())
            );
            className = generatedClassesPath + "GeneratedClass" + id;
        }
        final String superClassName = internalize(superClass.getName());
        final String[] interfaces = new String[1];
        {
            String apiPath = internalize(BaseFieldAccessor.class.getName());
            apiPath = apiPath.substring(0,
                    apiPath.lastIndexOf(BaseFieldAccessor.class.getSimpleName())
            );
            interfaces[0] = apiPath + "FieldAccessor";
        }
        final ClassWriter cw = writeClassHead(className, null, superClassName, interfaces);
        //   unused arg in static calls, should we make get/put-StaticForXXX?
        {
            final MethodVisitor mvCommonGetter = writeMethodHead(cw, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            if (!isStatic(modifiers)) {
                mvCommonGetter.visitVarInsn(Opcodes.ALOAD, 1);
                mvCommonGetter.visitTypeInsn(Opcodes.CHECKCAST, internalize(ownerClass.getName()));
            }
            mvCommonGetter.visitFieldInsn(
                    isStatic(modifiers)
                    ? Opcodes.GETSTATIC
                    : Opcodes.GETFIELD,
                    internalize(ownerClass.getName()), name, getClassDescName(type)
            );
            if (type.isPrimitive()) {
                boxingOnStack(mvCommonGetter, type);
                mvCommonGetter.visitInsn(Opcodes.ARETURN);
            } else {
                mvCommonGetter.visitInsn(Opcodes.ARETURN);
            }
            writeMethodTail(mvCommonGetter);
        }
        {
            final MethodVisitor mvCommonSetter = writeMethodHead(cw, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V");
            if (!isStatic(modifiers)) {
                mvCommonSetter.visitVarInsn(Opcodes.ALOAD, 1);
                mvCommonSetter.visitTypeInsn(Opcodes.CHECKCAST, internalize(ownerClass.getName()));
            }
            mvCommonSetter.visitVarInsn(Opcodes.ALOAD, 2);
            if (type.isPrimitive()) {
                unboxingOnStack(mvCommonSetter, type);
            } else {
                mvCommonSetter.visitTypeInsn(Opcodes.CHECKCAST, internalize(type.getName()));
            }
            mvCommonSetter.visitFieldInsn(
                    isStatic(modifiers)
                            ? Opcodes.PUTSTATIC
                            : Opcodes.PUTFIELD,
                    internalize(ownerClass.getName()), name, getClassDescName(type)
            );
            mvCommonSetter.visitInsn(Opcodes.RETURN);
            writeMethodTail(mvCommonSetter);
        }
        {
            final MethodVisitor mvGetter = writeMethodHead(cw, "get" + captureName(type.isPrimitive() ? type.getSimpleName() : Object.class.getSimpleName()), "(Ljava/lang/Object;)" + (type.isPrimitive() ? getClassDescName(type) : getClassDescName(Object.class)));
            if (!isStatic(modifiers)) {
                mvGetter.visitVarInsn(Opcodes.ALOAD, 1);
                mvGetter.visitTypeInsn(Opcodes.CHECKCAST, internalize(ownerClass.getName()));
            }
            mvGetter.visitFieldInsn(
                    isStatic(modifiers)
                            ? Opcodes.GETSTATIC
                            : Opcodes.GETFIELD,
                    internalize(ownerClass.getName()), name, getClassDescName(type)
            );
            if (type.isPrimitive()) {
                if (type == boolean.class
                        || type == byte.class
                        || type == char.class
                        || type == int.class
                        || type == short.class
                ) {
                    mvGetter.visitInsn(Opcodes.IRETURN);
                } else if (type == double.class) {
                    mvGetter.visitInsn(Opcodes.DRETURN);
                } else if (type == float.class) {
                    mvGetter.visitInsn(Opcodes.FRETURN);
                } else if (type == long.class) {
                    mvGetter.visitInsn(Opcodes.LRETURN);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                mvGetter.visitInsn(Opcodes.ARETURN);
            }
            writeMethodTail(mvGetter);
        }
        {
            final MethodVisitor mvSetter = writeMethodHead(cw, "set" + captureName(type.isPrimitive() ? type.getSimpleName() : Object.class.getSimpleName()), "(Ljava/lang/Object;" + (type.isPrimitive() ? getClassDescName(type) : getClassDescName(Object.class)) + ")V");
            if (!isStatic(modifiers)) {
                mvSetter.visitVarInsn(Opcodes.ALOAD, 1);
                mvSetter.visitTypeInsn(Opcodes.CHECKCAST, internalize(ownerClass.getName()));
            }
            if (type.isPrimitive()) {
                if (type == boolean.class
                        || type == byte.class
                        || type == char.class
                        || type == int.class
                        || type == short.class
                ) {
                    mvSetter.visitVarInsn(Opcodes.ILOAD, 2);
                } else if (type == double.class) {
                    mvSetter.visitVarInsn(Opcodes.DLOAD, 2);
                } else if (type == float.class) {
                    mvSetter.visitVarInsn(Opcodes.FLOAD, 2);
                } else if (type == long.class) {
                    mvSetter.visitVarInsn(Opcodes.LLOAD, 2);
                } else {
                    // should not happen
                    throw new IllegalArgumentException();
                }
            } else {
                mvSetter.visitVarInsn(Opcodes.ALOAD, 2);
                mvSetter.visitTypeInsn(Opcodes.CHECKCAST, internalize(type.getName()));
            }
            mvSetter.visitFieldInsn(
                    isStatic(modifiers)
                            ? Opcodes.PUTSTATIC
                            : Opcodes.PUTFIELD,
                    internalize(ownerClass.getName()), name, getClassDescName(type)
            );
            mvSetter.visitInsn(Opcodes.RETURN);
            writeMethodTail(mvSetter);
        }
        final byte[] classByteArray = writeClassTail(cw);
        try {
            final Class<?> outClass = ReflectionUtil.theUnsafe.defineClass(className, classByteArray, 0, classByteArray.length, Here.class.getClassLoader(), null);
            return (FieldAccessor<T>) outClass.getConstructor((Class<?>[]) null).newInstance((Object[]) null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> FieldAccessor<T> create(java.lang.reflect.Field field)
            throws NoSuchMethodException, NoSuchFieldException {
        return createField(field.getName(), (Class<T>) field.getDeclaringClass(), field.getModifiers(), field.getType(), false);
    }

    public static <T> FieldAccessor<T> create(String name, Class<T> ownerClass, int modifiers, Class<?> type) throws NoSuchMethodException, NoSuchFieldException {
        return create(name, ownerClass, modifiers, type, true);
    }

    public static <T> FieldAccessor<T> create(String name, Class<T> ownerClass, int modifiers, Class<?> type, boolean checkExist) throws NoSuchMethodException, NoSuchFieldException {
        return createField(name, ownerClass, modifiers, type, checkExist);
    }
}
