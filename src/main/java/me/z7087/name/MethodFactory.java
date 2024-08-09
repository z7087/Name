package me.z7087.name;

import me.z7087.name.api.BaseMethodAccessor;
import me.z7087.name.api.MethodAccessor;
import me.z7087.name.generatedclasses.Here;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


// warning: you should not use this at runtime now as it uses 60s to generate a class
// BaseMethodAccessorWithObjectParamXXX classes are too big, you should make your own implementation
public class MethodFactory extends AbstractClassGenerator {
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    protected MethodFactory() {}
    private static <T> MethodAccessor<T> createMethod(String name, Class<T> ownerClass, int modifiers,
                     Class<?>[] parameterTypes, Class<?> returnType,
                     boolean forceSpecial, boolean checkExist) throws NoSuchMethodException {
        if (ownerClass.isPrimitive())
            throw new IllegalArgumentException("Owner class cannot be primitive");
        if (checkExist) {
            foundMethod:
            {
                final int parameterTypesLength = parameterTypes.length;
                for (java.lang.reflect.Method reflectMethod : ownerClass.getDeclaredMethods()) {
                    if (reflectMethod.getName().equals(name)
                            && reflectMethod.getModifiers() == modifiers
                            && reflectMethod.getReturnType() == returnType
                    ) {
                        final Class<?>[] methodParameterTypes = reflectMethod.getParameterTypes();
                        checkParamTypes:
                        if (methodParameterTypes.length == parameterTypesLength) {
                            for (int i = 0; i < parameterTypesLength; ++i) {
                                if (methodParameterTypes[i] != parameterTypes[i])
                                    break checkParamTypes;
                            }
                            break foundMethod;
                        }
                    }
                }
                throw new NoSuchMethodException(name);
            }
        }
        final Class<?> superClass = AccessorClassGenerator.getInstance().getGeneratedDoorClass();
        {
            final int length;
            if (parameterTypes == null || (length = parameterTypes.length) == 0) {
                parameterTypes = EMPTY_CLASS_ARRAY;
            } else {
                parameterTypes = Arrays.copyOf(parameterTypes, length);
            }
        }
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
        final boolean canUseInvokeExact = parameterTypes.length <= 4;
        final int objectParamCount;
        {
            int count = 0;
            for (Class<?> p : parameterTypes) {
                if (!p.isPrimitive())
                    count++;
            }
            objectParamCount = count;
        }
        {
            String apiPath = internalize(BaseMethodAccessor.class.getName());
            apiPath = apiPath.substring(0,
                    apiPath.lastIndexOf(BaseMethodAccessor.class.getSimpleName())
            );
            interfaces[0] = apiPath + "MethodAccessor";
        }
        final ClassWriter cw = writeClassHead(className, null, superClassName, interfaces);
        // invoke():
        //   for (IFDZLa/b/SomethingExtendsObject;)S:
        //     return Short.valueOf(object.XXX(args[0].intValue(), args[1].floatValue(), args[2].doubleValue(), args[3].booleanValue(), (a.b.SomethingExtendsObject) args[4]));
        //   for ()V:
        //     object.XXX();
        //     return null;
        //   for (IIIII)I:
        //     return Integer.valueOf(object.XXX(args[0].intValue(), args[1].intValue(), args[2].intValue(), args[3].intValue(), args[4].intValue()));
        //   for static ()I:
        //     return Integer.valueOf(objectClass.XXX());
        // invokeForXXX():
        //   for (IIIII)V:
        //     throw new AbstractMethodError(); // undefined method
        //   for (La/b/SomethingExtendsObject;)La/b/SomethingExtendsObject;:
        //     return (a.b.SomethingExtendsObject) object.XXX((a.b.SomethingExtendsObject) arg0);
        //   for static ()I:
        //     return objectClass.XXX();
        //   unused arg in static calls, should we make invokeStaticForXXX?
        final String descriptor;
        {
            final StringBuilder descBuilder = new StringBuilder("(");
            for (Class<?> paramType : parameterTypes) {
                if (paramType == void.class)
                    throw new IllegalArgumentException("Found void in parameterTypes");
                descBuilder.append(getClassDescName(paramType));
            }
            descBuilder.append(')');
            descBuilder.append(getClassDescName(returnType));
            descriptor = descBuilder.toString();
        }

        {
            final MethodVisitor mvInvoke = writeMethodHead(cw, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            if (!isStatic(modifiers)) {
                mvInvoke.visitVarInsn(Opcodes.ALOAD, 1);
                mvInvoke.visitTypeInsn(Opcodes.CHECKCAST, internalize(ownerClass.getName()));
            }
            if (parameterTypes.length > 0) {
                int count = 0;
                for (Class<?> paramType : parameterTypes) {
                    assert paramType != void.class;
                    mvInvoke.visitVarInsn(Opcodes.ALOAD, 2);
                    if (count <= 32767) {
                        if (count <= 127) {
                            switch (count) {
                                case 0:
                                    mvInvoke.visitInsn(Opcodes.ICONST_0);
                                    break;
                                case 1:
                                    mvInvoke.visitInsn(Opcodes.ICONST_1);
                                    break;
                                case 2:
                                    mvInvoke.visitInsn(Opcodes.ICONST_2);
                                    break;
                                case 3:
                                    mvInvoke.visitInsn(Opcodes.ICONST_3);
                                    break;
                                case 4:
                                    mvInvoke.visitInsn(Opcodes.ICONST_4);
                                    break;
                                case 5:
                                    mvInvoke.visitInsn(Opcodes.ICONST_5);
                                    break;
                                default:
                                    mvInvoke.visitIntInsn(Opcodes.BIPUSH, count);
                            }
                        } else {
                            mvInvoke.visitIntInsn(Opcodes.SIPUSH, count);
                        }
                    } else {
                        mvInvoke.visitLdcInsn(count);
                    }
                    mvInvoke.visitInsn(Opcodes.AALOAD);
                    if (paramType.isPrimitive())
                        unboxingOnStack(mvInvoke, paramType);
                    count++;
                    if (count < 0)
                        throw new ArithmeticException("integer overflow");
                }
            }
            if (isStatic(modifiers)) {
                mvInvoke.visitMethodInsn(Opcodes.INVOKESTATIC, internalize(ownerClass.getName()), name, descriptor, false);
            } else if (forceSpecial || isPrivate(modifiers)) {
                mvInvoke.visitMethodInsn(Opcodes.INVOKESPECIAL, internalize(ownerClass.getName()), name, descriptor, !isPrivate(modifiers) && isInterface(modifiers));
            } else if (isInterface(modifiers)) {
                mvInvoke.visitMethodInsn(Opcodes.INVOKEINTERFACE, internalize(ownerClass.getName()), name, descriptor, true);
            } else {
                mvInvoke.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalize(ownerClass.getName()), name, descriptor, false);
            }
            if (returnType == void.class) {
                mvInvoke.visitInsn(Opcodes.RETURN);
            } else if (returnType.isPrimitive()) {
                boxingOnStack(mvInvoke, returnType);
                mvInvoke.visitInsn(Opcodes.ARETURN);
            } else {
                mvInvoke.visitInsn(Opcodes.ARETURN);
            }
            writeMethodTail(mvInvoke);
        }
        if (canUseInvokeExact) {
            final MethodVisitor mvInvokeExact;
            {
                final StringBuilder methodExactDescriptor = new StringBuilder("(Ljava/lang/Object;");
                for (Class<?> paramType : parameterTypes) {
                    assert paramType != void.class;
                    methodExactDescriptor.append(getClassDescName(paramType.isPrimitive() ? paramType : Object.class));
                }
                methodExactDescriptor.append(')');
                methodExactDescriptor.append(getClassDescName(returnType.isPrimitive() ? returnType : Object.class));
                mvInvokeExact = writeMethodHead(cw, "invokeFor" + captureName(returnType.isPrimitive() ? returnType.getSimpleName() : Object.class.getSimpleName()), methodExactDescriptor.toString());
            }
            if (!isStatic(modifiers)) {
                mvInvokeExact.visitVarInsn(Opcodes.ALOAD, 1);
                mvInvokeExact.visitTypeInsn(Opcodes.CHECKCAST, internalize(ownerClass.getName()));
            }
            {
                int index = 2;
                for (final Class<?> paramType : parameterTypes) {
                    if (paramType.isPrimitive()) {
                        if (paramType == boolean.class
                                || paramType == byte.class
                                || paramType == char.class
                                || paramType == int.class
                                || paramType == short.class
                        ) {
                            mvInvokeExact.visitVarInsn(Opcodes.ILOAD, index);
                        } else if (paramType == double.class) {
                            mvInvokeExact.visitVarInsn(Opcodes.DLOAD, index);
                            ++index;
                        } else if (paramType == float.class) {
                            mvInvokeExact.visitVarInsn(Opcodes.FLOAD, index);
                        } else if (paramType == long.class) {
                            mvInvokeExact.visitVarInsn(Opcodes.LLOAD, index);
                            ++index;
                        } else {
                            // should not happen
                            throw new IllegalArgumentException();
                        }
                    } else {
                        mvInvokeExact.visitVarInsn(Opcodes.ALOAD, index);
                        mvInvokeExact.visitTypeInsn(Opcodes.CHECKCAST, internalize(paramType.getName()));
                    }
                    ++index;
                }
            }
            if (isStatic(modifiers)) {
                mvInvokeExact.visitMethodInsn(Opcodes.INVOKESTATIC, internalize(ownerClass.getName()), name, descriptor, false);
            } else if (forceSpecial || isPrivate(modifiers)) {
                mvInvokeExact.visitMethodInsn(Opcodes.INVOKESPECIAL, internalize(ownerClass.getName()), name, descriptor, !isPrivate(modifiers) && isInterface(modifiers));
            } else if (isInterface(modifiers)) {
                mvInvokeExact.visitMethodInsn(Opcodes.INVOKEINTERFACE, internalize(ownerClass.getName()), name, descriptor, true);
            } else {
                mvInvokeExact.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalize(ownerClass.getName()), name, descriptor, false);
            }
            if (returnType == void.class) {
                mvInvokeExact.visitInsn(Opcodes.RETURN);
            } else if (returnType.isPrimitive()) {
                if (returnType == boolean.class
                        || returnType == byte.class
                        || returnType == char.class
                        || returnType == int.class
                        || returnType == short.class
                ) {
                    mvInvokeExact.visitInsn(Opcodes.IRETURN);
                } else if (returnType == double.class) {
                    mvInvokeExact.visitInsn(Opcodes.DRETURN);
                } else if (returnType == float.class) {
                    mvInvokeExact.visitInsn(Opcodes.FRETURN);
                } else if (returnType == long.class) {
                    mvInvokeExact.visitInsn(Opcodes.LRETURN);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                mvInvokeExact.visitInsn(Opcodes.ARETURN);
            }
            writeMethodTail(mvInvokeExact);
        }
        final byte[] classByteArray = writeClassTail(cw);
        try {
            final Class<?> outClass = UnsafeClassDefiner.define(className, classByteArray, Here.class.getClassLoader(), null);
            return (MethodAccessor<T>) outClass.getConstructor((Class<?>[]) null).newInstance((Object[]) null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> MethodAccessor<T> create(java.lang.reflect.Method method) throws NoSuchMethodException {
        return createMethod(method.getName(), (Class<T>) method.getDeclaringClass(), method.getModifiers(), method.getParameterTypes(), method.getReturnType(), false, false);
    }

    public static <T> MethodAccessor<T> create(String name, Class<T> clazz, int modifiers,
                                Class<?>[] parameterTypes, Class<?> returnType) throws NoSuchMethodException {
        return create(name, clazz, modifiers, parameterTypes, returnType, true);
    }

    public static <T> MethodAccessor<T> create(String name, Class<T> clazz, int modifiers,
                                Class<?>[] parameterTypes, Class<?> returnType,
                                boolean checkExist) throws NoSuchMethodException {
        return createMethod(name, clazz, modifiers, parameterTypes, returnType, false, checkExist);
    }

    // create a handler that invokes a virtual method directly, ignore methods in subclasses
    // NEED MORE TEST
    public static <T> MethodAccessor<T> createSpecial(java.lang.reflect.Method method) throws NoSuchMethodException {
        return createMethod(method.getName(), (Class<T>) method.getDeclaringClass(), method.getModifiers(), method.getParameterTypes(), method.getReturnType(), true, false);
    }

    public static <T> MethodAccessor<T> createSpecial(String name, Class<T> clazz, int modifiers,
                                Class<?>[] parameterTypes, Class<?> returnType) throws NoSuchMethodException {
        return createSpecial(name, clazz, modifiers, parameterTypes, returnType, true);
    }

    public static <T> MethodAccessor<T> createSpecial(String name, Class<T> clazz, int modifiers,
                                Class<?>[] parameterTypes, Class<?> returnType,
                                boolean checkExist) throws NoSuchMethodException {
        return createMethod(name, clazz, modifiers, parameterTypes, returnType, true, checkExist);
    }
}
