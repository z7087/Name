package me.z7087.name;

import me.z7087.name.api.MethodAccessor;
import me.z7087.name.generatedclasses.Here;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MethodFactory extends AbstractClassGenerator {
    private MethodFactory() {}

    private static void verify(Class<?> interfaceClass,
                               MethodDesc targetMethod,
                               MethodDesc interfaceMethodExact,
                               MethodDesc interfaceMethodVarargs,
                               boolean isStatic) {
        if (interfaceClass.isPrimitive())
            throwIllegalArgumentException("interfaceClass cannot be primitive");
        if (interfaceClass.isArray())
            throwIllegalArgumentException("interfaceClass cannot be array");

        if (interfaceMethodExact == null && interfaceMethodVarargs == null) {
            throwIllegalArgumentException("interfaceMethodExact and interfaceMethodVarargs cannot be both null");
        } else if (interfaceMethodExact != null && interfaceMethodVarargs != null) {
            if (interfaceMethodExact.equalsDescriptor(interfaceMethodVarargs))
                throwIllegalArgumentException("interfaceMethodExact and interfaceMethodVarargs cannot have same descriptor");
        }

        {
            final String className = getClassName(interfaceClass);
            if ((interfaceMethodExact == null || !className.equals(interfaceMethodExact.getOwnerClass()))
                    && (interfaceMethodVarargs == null || !className.equals(interfaceMethodVarargs.getOwnerClass()))) {
                throwIllegalArgumentException("interfaceClass is not the owner of any of interfaceMethodExact and interfaceMethodVarargs");
            }
        }

        if (interfaceMethodExact != null) {
            verifyExact(targetMethod, interfaceMethodExact, isStatic);
        }

        if (interfaceMethodVarargs != null) {
            verifyVarargs(targetMethod, interfaceMethodVarargs, isStatic);
        }
    }

    private static void verifyExact(MethodDesc targetMethod,
                                    MethodDesc interfaceMethodExact,
                                    boolean isStatic) {
        if (!canCheckCastOrBox(targetMethod.getReturnType(), interfaceMethodExact.getReturnType()))
            throwIllegalArgumentException("The return value type of interfaceMethodExact is not compatible with the return value type of targetMethod: \n" + interfaceMethodExact.getReturnType() + "\n" + targetMethod.getReturnType());

        final int offsetIndex = interfaceMethodExact.getParamTypesRaw().length - targetMethod.getParamTypesRaw().length;
        switch (offsetIndex) {
            case 0:
                break;
            case 1:
                if (isStatic)
                    // unused arg, but continue
                    break;
            default:
                throwIllegalArgumentException("The number of parameters for interfaceMethodExact is not compatible with targetMethod's: " + interfaceMethodExact.getParamTypesRaw().length + ", " + targetMethod.getParamTypesRaw().length);
        }
        if (offsetIndex == 1) {
            if (isParamTypePrimitive(interfaceMethodExact.getParamTypesRaw()[0])) {
                throwIllegalArgumentException("interfaceMethodExact parameter 0 cannot be primitive: " + interfaceMethodExact.getParamTypesRaw()[0]);
            }
        }
        for (int i = 0, length = targetMethod.getParamTypesRaw().length; i < length; ++i) {
            if (!canCheckCastOrBox(interfaceMethodExact.getParamTypesRaw()[i + offsetIndex], targetMethod.getParamTypesRaw()[i]))
                throwIllegalArgumentException("The type of interfaceMethodExact parameter" + (i + offsetIndex) + " is not compatible with the type of targetMethod parameter " + i + ": \n" + interfaceMethodExact.getParamTypesRaw()[i + offsetIndex] + "\n" + targetMethod.getParamTypesRaw()[i]);
        }
    }

    private static void verifyVarargs(MethodDesc targetMethod,
                                    MethodDesc interfaceMethodVarargs,
                                    boolean isStatic) {
        // exempt for void -> Object and void -> Void
        if (!targetMethod.getReturnType().equals("V") || !(interfaceMethodVarargs.getReturnType().equals("Ljava/lang/Object;") || interfaceMethodVarargs.getReturnType().equals("Ljava/lang/Void;"))) {
            if (!canCheckCastOrBox(targetMethod.getReturnType(), interfaceMethodVarargs.getReturnType()))
                throwIllegalArgumentException("The return value type of interfaceMethodVarargs is not compatible with the return value type of targetMethod: \n" + interfaceMethodVarargs.getReturnType() + "\n" + targetMethod.getReturnType());
        }
        switch (interfaceMethodVarargs.getParamTypesRaw().length) {
            case 2:
                if (!isStatic)
                    break;
                // unused arg, but continue
                break;
            case 1:
                if (isStatic)
                    break;
            default:
                throwIllegalArgumentException("The number of parameters for interfaceMethodVarargs is illegal: " + interfaceMethodVarargs.getParamTypesRaw().length);
        }
        if (interfaceMethodVarargs.getParamTypesRaw().length == 2) {
            if (isParamTypePrimitive(interfaceMethodVarargs.getParamTypesRaw()[0])) {
                throwIllegalArgumentException("interfaceMethodVarargs parameter 0 cannot be primitive: " + interfaceMethodVarargs.getParamTypesRaw()[0]);
            }
        }
        String typeOfArrayElement = null;
        {
            final String lastParam = interfaceMethodVarargs.getParamTypesRaw()[interfaceMethodVarargs.getParamTypesRaw().length - 1];
            if (isParamTypeArray(lastParam))
                typeOfArrayElement = getArrayElementType(lastParam);
            else
                throwIllegalArgumentException("interfaceMethodVarargs parameter " + (interfaceMethodVarargs.getParamTypesRaw().length - 1) + " is not an array");
        }
        for (int i = 0, length = targetMethod.getParamTypesRaw().length; i < length; ++i) {
            if (!canCheckCastOrBox(typeOfArrayElement, targetMethod.getParamTypesRaw()[i]))
                throwIllegalArgumentException("The type of interfaceMethodVarargs parameter" + (interfaceMethodVarargs.getParamTypesRaw().length - 1) + " is not compatible with the type of targetMethod parameter " + i + ": \n" + interfaceMethodVarargs.getParamTypesRaw()[interfaceMethodVarargs.getParamTypesRaw().length - 1] + "\n" + targetMethod.getParamTypesRaw()[i]);
        }
    }

    private static <T> T createMethod(ClassLoader loader,
                                      Class<T> interfaceClass,
                                      MethodDesc targetMethod,
                                      MethodDesc interfaceMethodExact,
                                      MethodDesc interfaceMethodVarargs,
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
                interfaceMethodExact,
                interfaceMethodVarargs,
                isStatic
        );
        assert (interfaceMethodExact != null || interfaceMethodVarargs != null);
        if (checkExist) {
            findMethod(loader, targetMethod);
            if (interfaceMethodExact != null)
                findMethod(loader, interfaceMethodExact);
            if (interfaceMethodVarargs != null)
                findMethod(loader, interfaceMethodVarargs);
        }
        final Class<?> superClass = AccessorClassGenerator.getInstance().getGeneratedDoorClass();
        final String className = Here.PATH + "GeneratedClass" + getId();
        final String superClassName = getClassName(superClass);
        final String[] interfaces;
        {
            if (interfaceMethodExact == null) {
                interfaces = new String[1];
                interfaces[0] = interfaceMethodVarargs.getOwnerClass();
            } else if (interfaceMethodVarargs == null) {
                interfaces = new String[1];
                interfaces[0] = interfaceMethodExact.getOwnerClass();
            } else {
                final String exactOwnerClass = interfaceMethodExact.getOwnerClass();
                final String varargsOwnerClass = interfaceMethodVarargs.getOwnerClass();
                if (exactOwnerClass.equals(varargsOwnerClass)) {
                    interfaces = new String[1];
                    interfaces[0] = exactOwnerClass;
                } else {
                    interfaces = new String[2];
                    interfaces[0] = exactOwnerClass;
                    interfaces[1] = varargsOwnerClass;
                }
            }
        }
        final ClassWriter cw = writeClassHead(className, null, superClassName, interfaces);
        if (interfaceMethodExact != null) {
            final MethodVisitor mvInvokeExact = writeMethodHead(cw, interfaceMethodExact.getName(), "(" + interfaceMethodExact.getMergedParamTypes() + ")" + interfaceMethodExact.getReturnType());
            if (!isStatic) {
                mvInvokeExact.visitVarInsn(Opcodes.ALOAD, 1);
                if (!knownNoNeedToCheckCast(interfaceMethodExact.getParamTypesRaw()[0], targetMethod.getOwnerType()))
                    mvInvokeExact.visitTypeInsn(Opcodes.CHECKCAST, targetMethod.getOwnerClass());
            }
            if (targetMethod.getParamTypesRaw().length > 0) {
                final int offset = interfaceMethodExact.getParamTypesRaw().length - targetMethod.getParamTypesRaw().length;
                assert offset == 0 || offset == 1;
                int localVariableTableIndex = 1 + offset;
                for (int i = 0, length = targetMethod.getParamTypesRaw().length; i < length; ++i) {
                    switch (interfaceMethodExact.getParamTypesRaw()[i + offset].charAt(0)) {
                        case 'L':
                        case '[':
                            mvInvokeExact.visitVarInsn(Opcodes.ALOAD, localVariableTableIndex);
                            break;
                        case 'Z':
                        case 'B':
                        case 'C':
                        case 'I':
                        case 'S':
                            mvInvokeExact.visitVarInsn(Opcodes.ILOAD, localVariableTableIndex);
                            break;
                        case 'F':
                            mvInvokeExact.visitVarInsn(Opcodes.FLOAD, localVariableTableIndex);
                            break;
                        case 'D':
                            mvInvokeExact.visitVarInsn(Opcodes.DLOAD, localVariableTableIndex);
                            localVariableTableIndex++;
                            break;
                        case 'J':
                            mvInvokeExact.visitVarInsn(Opcodes.LLOAD, localVariableTableIndex);
                            localVariableTableIndex++;
                            break;
                        default:
                            shouldNotReachHere();
                    }
                    checkCastOrBox(mvInvokeExact, interfaceMethodExact.getParamTypesRaw()[i + offset], targetMethod.getParamTypesRaw()[i]);
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
                mvInvokeExact.visitMethodInsn(opcode, targetMethod.getOwnerClass(), targetMethod.getName(), "(" + targetMethod.getMergedParamTypes() + ")" + targetMethod.getReturnType(), targetClassIsInterface);
            }
            checkCastOrBox(mvInvokeExact, targetMethod.getReturnType(), interfaceMethodExact.getReturnType());
            switch (interfaceMethodExact.getReturnType().charAt(0)) {
                case 'V':
                    mvInvokeExact.visitInsn(Opcodes.RETURN);
                    break;
                case 'L':
                case '[':
                    mvInvokeExact.visitInsn(Opcodes.ARETURN);
                    break;
                case 'Z':
                case 'B':
                case 'C':
                case 'I':
                case 'S':
                    mvInvokeExact.visitInsn(Opcodes.IRETURN);
                    break;
                case 'F':
                    mvInvokeExact.visitInsn(Opcodes.FRETURN);
                    break;
                case 'D':
                    mvInvokeExact.visitInsn(Opcodes.DRETURN);
                    break;
                case 'J':
                    mvInvokeExact.visitInsn(Opcodes.LRETURN);
                    break;
                default:
                    shouldNotReachHere();
            }
            writeMethodTail(mvInvokeExact);
        }
        if (interfaceMethodVarargs != null) {
            final MethodVisitor mvInvokeVarargs = writeMethodHead(cw, interfaceMethodVarargs.getName(), "(" + interfaceMethodVarargs.getMergedParamTypes() + ")" + interfaceMethodVarargs.getReturnType());
            if (!isStatic) {
                mvInvokeVarargs.visitVarInsn(Opcodes.ALOAD, 1);
                if (!knownNoNeedToCheckCast(interfaceMethodVarargs.getParamTypesRaw()[0], targetMethod.getOwnerType()))
                    mvInvokeVarargs.visitTypeInsn(Opcodes.CHECKCAST, targetMethod.getOwnerClass());
            }
            if (targetMethod.getParamTypesRaw().length > 0) {
                final int arrayOffset = interfaceMethodVarargs.getParamTypesRaw().length;
                assert isParamTypeArray(interfaceMethodVarargs.getParamTypesRaw()[arrayOffset - 1]);
                final String arrayElementType = getArrayElementType(interfaceMethodVarargs.getParamTypesRaw()[arrayOffset - 1]);
                int opcodeForLoadFromArray = 0;
                switch (arrayElementType.charAt(0)) {
                    case 'Z':
                    case 'B':
                        opcodeForLoadFromArray = Opcodes.BALOAD;
                        break;
                    case 'C':
                        opcodeForLoadFromArray = Opcodes.CALOAD;
                        break;
                    case 'D':
                        opcodeForLoadFromArray = Opcodes.DALOAD;
                        break;
                    case 'F':
                        opcodeForLoadFromArray = Opcodes.FALOAD;
                        break;
                    case 'I':
                        opcodeForLoadFromArray = Opcodes.IALOAD;
                        break;
                    case 'J':
                        opcodeForLoadFromArray = Opcodes.LALOAD;
                        break;
                    case 'S':
                        opcodeForLoadFromArray = Opcodes.SALOAD;
                        break;
                    case 'L':
                    case '[':
                        opcodeForLoadFromArray = Opcodes.AALOAD;
                        break;
                    default:
                        shouldNotReachHere();
                }
                for (int arrayIndex = 0, length = targetMethod.getParamTypesRaw().length; arrayIndex < length; ++arrayIndex) {
                    mvInvokeVarargs.visitVarInsn(Opcodes.ALOAD, arrayOffset);
                    if (arrayIndex <= 32767) {
                        if (arrayIndex <= 127) {
                            switch (arrayIndex) {
                                case 0:
                                    mvInvokeVarargs.visitInsn(Opcodes.ICONST_0);
                                    break;
                                case 1:
                                    mvInvokeVarargs.visitInsn(Opcodes.ICONST_1);
                                    break;
                                case 2:
                                    mvInvokeVarargs.visitInsn(Opcodes.ICONST_2);
                                    break;
                                case 3:
                                    mvInvokeVarargs.visitInsn(Opcodes.ICONST_3);
                                    break;
                                case 4:
                                    mvInvokeVarargs.visitInsn(Opcodes.ICONST_4);
                                    break;
                                case 5:
                                    mvInvokeVarargs.visitInsn(Opcodes.ICONST_5);
                                    break;
                                default:
                                    mvInvokeVarargs.visitIntInsn(Opcodes.BIPUSH, arrayIndex);
                            }
                        } else {
                            mvInvokeVarargs.visitIntInsn(Opcodes.SIPUSH, arrayIndex);
                        }
                    } else {
                        mvInvokeVarargs.visitLdcInsn(arrayIndex);
                    }
                    mvInvokeVarargs.visitInsn(opcodeForLoadFromArray);
                    checkCastOrBox(mvInvokeVarargs, arrayElementType, targetMethod.getParamTypesRaw()[arrayIndex]);
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
                mvInvokeVarargs.visitMethodInsn(opcode, targetMethod.getOwnerClass(), targetMethod.getName(), "(" + targetMethod.getMergedParamTypes() + ")" + targetMethod.getReturnType(), targetClassIsInterface);
            }
            if (targetMethod.getReturnType().equals("V") && (interfaceMethodVarargs.getReturnType().equals("Ljava/lang/Object;") || interfaceMethodVarargs.getReturnType().equals("Ljava/lang/Void;"))) {
                mvInvokeVarargs.visitInsn(Opcodes.ACONST_NULL);
                mvInvokeVarargs.visitInsn(Opcodes.ARETURN);
            } else {
                checkCastOrBox(mvInvokeVarargs, targetMethod.getReturnType(), interfaceMethodVarargs.getReturnType());
                switch (interfaceMethodVarargs.getReturnType().charAt(0)) {
                    case 'V':
                        mvInvokeVarargs.visitInsn(Opcodes.RETURN);
                        break;
                    case 'L':
                    case '[':
                        mvInvokeVarargs.visitInsn(Opcodes.ARETURN);
                        break;
                    case 'Z':
                    case 'B':
                    case 'C':
                    case 'I':
                    case 'S':
                        mvInvokeVarargs.visitInsn(Opcodes.IRETURN);
                        break;
                    case 'F':
                        mvInvokeVarargs.visitInsn(Opcodes.FRETURN);
                        break;
                    case 'D':
                        mvInvokeVarargs.visitInsn(Opcodes.DRETURN);
                        break;
                    case 'J':
                        mvInvokeVarargs.visitInsn(Opcodes.LRETURN);
                        break;
                    default:
                        shouldNotReachHere();
                }
            }
            writeMethodTail(mvInvokeVarargs);
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

    public static MethodAccessor<?, ?> create(ClassLoader loader,
                                                     Method method,
                                                     boolean invokeSpecial
    ) throws NoSuchMethodException {
        return createMethod(
                loader,
                MethodAccessor.class,
                MethodDesc.of(
                        method.getDeclaringClass(),
                        method.getName(),
                        method.getReturnType(),
                        method.getParameterTypes()
                ),
                null,
                MethodDesc.of(MethodAccessor.class, "invoke", Object.class, Object.class, Object[].class),
                isStatic(method.getModifiers()),
                isInterface(method.getDeclaringClass().getModifiers()),
                isInterface(method.getModifiers()),
                invokeSpecial,
                false
        );
    }

    public static <T> T create(ClassLoader loader,
                                      Class<T> interfaceClass,
                                      MethodDesc targetMethod,
                                      MethodDesc interfaceMethodExact,
                                      MethodDesc interfaceMethodVarargs,
                                      boolean isStatic,
                                      boolean targetClassIsInterface,
                                      boolean targetMethodIsInterface,
                                      boolean invokeSpecial,
                                      boolean checkExist
    ) throws NoSuchMethodException {
        return createMethod(
                loader,
                interfaceClass,
                targetMethod,
                interfaceMethodExact,
                interfaceMethodVarargs,
                isStatic,
                targetClassIsInterface,
                targetMethodIsInterface,
                invokeSpecial,
                checkExist
        );
    }
}
