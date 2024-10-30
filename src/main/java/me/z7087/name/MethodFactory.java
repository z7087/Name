package me.z7087.name;

import me.z7087.name.api.MethodAccessor;
import me.z7087.name.generatedclasses.Here;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;

public final class MethodFactory extends AbstractClassGenerator {
    private MethodFactory() {}

    private static void verify(Class<?> interfaceClass,
                               MethodDesc targetMethod,
                               MethodDesc interfaceMethod,
                               boolean isStatic) {
        if (interfaceClass.isPrimitive())
            throwIllegalArgumentException("interfaceClass cannot be primitive");
        if (interfaceClass.isArray())
            throw new IllegalArgumentException("interfaceClass cannot be array");

        if (!getClassName(interfaceClass).equals(interfaceMethod.getOwnerClass()))
            throwIllegalArgumentException("interfaceClass is not the owner of interfaceMethod");

        if (!canCheckCastOrBox(targetMethod.getReturnType(), interfaceMethod.getReturnType()))
            throwIllegalArgumentException("The return value type of interfaceMethod is not compatible with the return value type of targetMethod: \n" + interfaceMethod.getReturnType() + "\n" + targetMethod.getReturnType());

        final int offsetIndex = interfaceMethod.getParamTypesRaw().length - targetMethod.getParamTypesRaw().length;
        switch (offsetIndex) {
            case 0:
                break;
            case 1:
                if (isStatic)
                    // unused arg, but continue
                    break;
            default:
                throwIllegalArgumentException("The number of parameters for interfaceMethod is not compatible with targetMethod's: " + interfaceMethod.getParamTypesRaw().length + ", " + targetMethod.getParamTypesRaw().length);
        }
        if (offsetIndex == 1) {
            if (isParamTypePrimitive(interfaceMethod.getParamTypesRaw()[0])) {
                throwIllegalArgumentException("interfaceMethod parameter 0 cannot be primitive: " + interfaceMethod.getParamTypesRaw()[0]);
            }
        }
        for (int i = 0, length = targetMethod.getParamTypesRaw().length; i < length; ++i) {
            if (!canCheckCastOrBox(interfaceMethod.getParamTypesRaw()[i + offsetIndex], targetMethod.getParamTypesRaw()[i]))
                throwIllegalArgumentException("The type of interfaceMethod parameter" + (i + offsetIndex) + " is not compatible with the type of targetMethod parameter " + i + ": \n" + interfaceMethod.getParamTypesRaw()[i + offsetIndex] + "\n" + targetMethod.getParamTypesRaw()[i]);
        }
    }

    private static <T> T createMethod(ClassLoader loader,
                                      Class<T> interfaceClass,
                                      MethodDesc targetMethod,
                                      MethodDesc interfaceMethod,
                                      boolean isStatic,
                                      boolean targetClassIsInterface,
                                      boolean targetMethodIsInterface,
                                      boolean invokeSpecial,
                                      boolean checkExist
    ) throws NoSuchMethodException {
        if (targetMethodIsInterface && !targetClassIsInterface)
            throwIllegalArgumentException("targetMethodIsInterface cannot be true if targetClassIsInterface is false");
        verify(
                interfaceClass,
                targetMethod,
                interfaceMethod,
                isStatic
        );

        if (checkExist) {
            findMethod(loader, targetMethod);
            findMethod(loader, interfaceMethod);
        }
        final Class<?> superClass = AccessorClassGenerator.getInstance().getGeneratedDoorClass();
        final String className = Here.PATH + "GeneratedClass" + getId();
        final String superClassName = getClassName(superClass);
        final String[] interfaces = new String[1];
        interfaces[0] = interfaceMethod.getOwnerClass();
        final ClassWriter cw = writeClassHead(className, null, superClassName, interfaces);
        {
            final MethodVisitor mvInvoke = writeMethodHead(cw, interfaceMethod.getName(), "(" + interfaceMethod.getMergedParamTypes() + ")" + interfaceMethod.getReturnType());
            if (!isStatic) {
                mvInvoke.visitVarInsn(Opcodes.ALOAD, 1);
                if (!knownNoNeedToCheckCast(interfaceMethod.getParamTypesRaw()[0], targetMethod.getOwnerType()))
                    mvInvoke.visitTypeInsn(Opcodes.CHECKCAST, targetMethod.getOwnerClass());
            }
            if (targetMethod.getParamTypesRaw().length > 0) {
                final int offset = interfaceMethod.getParamTypesRaw().length - targetMethod.getParamTypesRaw().length;
                assert offset == 0 || offset == 1;
                int localVariableTableIndex = 1 + offset;
                for (int i = 0, length = targetMethod.getParamTypesRaw().length; i < length; ++i) {
                    switch (interfaceMethod.getParamTypesRaw()[i + offset].charAt(0)) {
                        case 'L':
                        case '[':
                            mvInvoke.visitVarInsn(Opcodes.ALOAD, localVariableTableIndex);
                            break;
                        case 'Z':
                        case 'B':
                        case 'C':
                        case 'I':
                        case 'S':
                            mvInvoke.visitVarInsn(Opcodes.ILOAD, localVariableTableIndex);
                            break;
                        case 'F':
                            mvInvoke.visitVarInsn(Opcodes.FLOAD, localVariableTableIndex);
                            break;
                        case 'D':
                            mvInvoke.visitVarInsn(Opcodes.DLOAD, localVariableTableIndex);
                            localVariableTableIndex++;
                            break;
                        case 'J':
                            mvInvoke.visitVarInsn(Opcodes.LLOAD, localVariableTableIndex);
                            localVariableTableIndex++;
                            break;
                        default:
                            shouldNotReachHere();
                    }
                    checkCastOrBox(mvInvoke, interfaceMethod.getParamTypesRaw()[i + offset], targetMethod.getParamTypesRaw()[i]);
                    localVariableTableIndex++;
                    if (localVariableTableIndex < 0)
                        shouldNotReachHere();
                }
            }
            {
                final int opcode;
                if (isStatic) {
                    opcode = Opcodes.INVOKESTATIC;
                } else if (invokeSpecial) {
                    opcode = Opcodes.INVOKESPECIAL;
                } else if (targetMethodIsInterface) {
                    opcode = Opcodes.INVOKEINTERFACE;
                } else {
                    opcode = Opcodes.INVOKEVIRTUAL;
                }
                mvInvoke.visitMethodInsn(opcode, targetMethod.getOwnerClass(), targetMethod.getName(), "(" + targetMethod.getMergedParamTypes() + ")" + targetMethod.getReturnType(), targetClassIsInterface);
            }
            checkCastOrBox(mvInvoke, targetMethod.getReturnType(), interfaceMethod.getReturnType());
            switch (interfaceMethod.getReturnType().charAt(0)) {
                case 'V':
                    mvInvoke.visitInsn(Opcodes.RETURN);
                case 'L':
                case '[':
                    mvInvoke.visitInsn(Opcodes.ARETURN);
                    break;
                case 'Z':
                case 'B':
                case 'C':
                case 'I':
                case 'S':
                    mvInvoke.visitInsn(Opcodes.IRETURN);
                    break;
                case 'F':
                    mvInvoke.visitInsn(Opcodes.FRETURN);
                    break;
                case 'D':
                    mvInvoke.visitInsn(Opcodes.DRETURN);
                    break;
                case 'J':
                    mvInvoke.visitInsn(Opcodes.LRETURN);
                    break;
                default:
                    shouldNotReachHere();
            }
            writeMethodTail(mvInvoke);
        }
        final byte[] classByteArray = writeClassTail(cw);
        try {
            final Class<?> outClass = UnsafeClassDefiner.define(className, classByteArray, Here.class.getClassLoader(), null);
            return interfaceClass.cast(outClass.getConstructor((Class<?>[]) null).newInstance((Object[]) null));
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
