package me.z7087.name;

import me.z7087.name.api.ConstructorAccessor;
import me.z7087.name.generatedclasses.Here;
import me.z7087.name.util.JavaVersion;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class ConstructorFactory extends AbstractClassGenerator {
    private ConstructorFactory() {}

    private static void verify(Class<?> interfaceClass,
                               ConstructorDesc targetConstructor,
                               MethodDesc interfaceMethodExact,
                               MethodDesc interfaceMethodVarargs) {
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
            verifyExact(targetConstructor, interfaceMethodExact);
        }

        if (interfaceMethodVarargs != null) {
            verifyVarargs(targetConstructor, interfaceMethodVarargs);
        }
    }

    private static void verifyExact(ConstructorDesc targetConstructor,
                                    MethodDesc interfaceMethodExact) {
        if (interfaceMethodExact.getReturnType().charAt(0) != 'L')
            throwIllegalArgumentException("The return value type of interfaceMethodExact cannot be primitive or array: " + interfaceMethodExact.getReturnType());

        if (interfaceMethodExact.getParamTypesRaw().length != targetConstructor.getParamTypesRaw().length) {
            throwIllegalArgumentException("The number of parameters for interfaceMethodExact is not compatible with targetConstructor's: " + interfaceMethodExact.getParamTypesRaw().length + ", " + targetConstructor.getParamTypesRaw().length);
        }
        for (int i = 0, length = targetConstructor.getParamTypesRaw().length; i < length; ++i) {
            if (!canCheckCastOrBox(interfaceMethodExact.getParamTypesRaw()[i], targetConstructor.getParamTypesRaw()[i]))
                throwIllegalArgumentException("The type of interfaceMethodExact parameter " + i + " is not compatible with the type of targetMethod parameter " + i + ": \n" + interfaceMethodExact.getParamTypesRaw()[i] + "\n" + targetConstructor.getParamTypesRaw()[i]);
        }
    }

    private static void verifyVarargs(ConstructorDesc targetConstructor,
                                      MethodDesc interfaceMethodVarargs) {
        if (interfaceMethodVarargs.getReturnType().charAt(0) != 'L')
            throwIllegalArgumentException("The return value type of interfaceMethodVarargs cannot be primitive or array: \n" + interfaceMethodVarargs.getReturnType());
        
        if (interfaceMethodVarargs.getParamTypesRaw().length != 1) {
            throwIllegalArgumentException("The number of parameters for interfaceMethodVarargs is illegal: " + interfaceMethodVarargs.getParamTypesRaw().length);
        }
        String typeOfArrayElement = null;
        {
            final String lastParam = interfaceMethodVarargs.getParamTypesRaw()[0];
            if (isParamTypeArray(lastParam))
                typeOfArrayElement = getArrayElementType(lastParam);
            else
                throwIllegalArgumentException("interfaceMethodVarargs parameter 0 is not an array");
        }
        for (int i = 0, length = targetConstructor.getParamTypesRaw().length; i < length; ++i) {
            if (!canCheckCastOrBox(typeOfArrayElement, targetConstructor.getParamTypesRaw()[i]))
                throwIllegalArgumentException("The type of interfaceMethodVarargs parameter 0 is not compatible with the type of targetMethod parameter " + i + ": \n" + interfaceMethodVarargs.getParamTypesRaw()[0] + "\n" + targetConstructor.getParamTypesRaw()[i]);
        }
    }

    private static <T> T createConstructor(ClassLoader loader,
                                           Class<T> interfaceClass,
                                           ConstructorDesc targetConstructor,
                                           MethodDesc interfaceMethodExact,
                                           MethodDesc interfaceMethodVarargs,
                                           boolean checkExist
    ) throws NoSuchMethodException {
        verify(
                interfaceClass,
                targetConstructor,
                interfaceMethodExact,
                interfaceMethodVarargs
        );
        assert (interfaceMethodExact != null || interfaceMethodVarargs != null);
        if (checkExist) {
            findDeclaredConstructor(loader, targetConstructor);
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
            mvInvokeExact.visitTypeInsn(Opcodes.NEW, targetConstructor.getActualOwnerClass());
            mvInvokeExact.visitInsn(Opcodes.DUP);
            if (!knownNoNeedToCheckCast(targetConstructor.getActualOwnerType(), targetConstructor.getConstructorOwnerType()))
                mvInvokeExact.visitTypeInsn(Opcodes.CHECKCAST, targetConstructor.getConstructorOwnerClass());
            if (targetConstructor.getParamTypesRaw().length > 0) {
                int localVariableTableIndex = 1;
                for (int i = 0, length = targetConstructor.getParamTypesRaw().length; i < length; ++i) {
                    switch (interfaceMethodExact.getParamTypesRaw()[i].charAt(0)) {
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
                    checkCastOrBox(mvInvokeExact, interfaceMethodExact.getParamTypesRaw()[i], targetConstructor.getParamTypesRaw()[i]);
                    localVariableTableIndex++;
                    if (localVariableTableIndex < 0)
                        shouldNotReachHere();
                }
            }
            mvInvokeExact.visitMethodInsn(Opcodes.INVOKESPECIAL, targetConstructor.getConstructorOwnerClass(), "<init>", "(" + targetConstructor.getMergedParamTypes() + ")V", false);
            checkCastOrBox(mvInvokeExact, targetConstructor.getActualOwnerType(), interfaceMethodExact.getReturnType());
            mvInvokeExact.visitInsn(Opcodes.ARETURN);
            writeMethodTail(mvInvokeExact);
        }
        if (interfaceMethodVarargs != null) {
            final MethodVisitor mvInvokeVarargs = writeMethodHead(cw, interfaceMethodVarargs.getName(), "(" + interfaceMethodVarargs.getMergedParamTypes() + ")" + interfaceMethodVarargs.getReturnType());
            mvInvokeVarargs.visitTypeInsn(Opcodes.NEW, targetConstructor.getActualOwnerClass());
            mvInvokeVarargs.visitInsn(Opcodes.DUP);
            if (!knownNoNeedToCheckCast(targetConstructor.getActualOwnerType(), targetConstructor.getConstructorOwnerType()))
                mvInvokeVarargs.visitTypeInsn(Opcodes.CHECKCAST, targetConstructor.getConstructorOwnerClass());
            if (targetConstructor.getParamTypesRaw().length > 0) {
                final String arrayElementType = getArrayElementType(interfaceMethodVarargs.getParamTypesRaw()[0]);
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
                for (int arrayIndex = 0, length = targetConstructor.getParamTypesRaw().length; arrayIndex < length; ++arrayIndex) {
                    mvInvokeVarargs.visitVarInsn(Opcodes.ALOAD, 1);
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
                    checkCastOrBox(mvInvokeVarargs, arrayElementType, targetConstructor.getParamTypesRaw()[arrayIndex]);
                }
            }
            mvInvokeVarargs.visitMethodInsn(Opcodes.INVOKESPECIAL, targetConstructor.getConstructorOwnerClass(), "<init>", "(" + targetConstructor.getMergedParamTypes() + ")V", false);
            checkCastOrBox(mvInvokeVarargs, targetConstructor.getActualOwnerType(), interfaceMethodVarargs.getReturnType());
            mvInvokeVarargs.visitInsn(Opcodes.ARETURN);
            writeMethodTail(mvInvokeVarargs);
        }
        final byte[] classByteArray = writeClassTail(cw);
        final ClassLoader newLoader = JavaVersion.getInstance().getVersion() <= 8
                ? AccessorClassGenerator.getInstance().getGeneratedAccessorInstance().createAccessorClassLoaderJ8(loader)
                : AccessorClassGenerator.getInstance().getGeneratedAccessorInstance().createAccessorClassLoaderJ9(null, loader);
        try {
            final Class<?> outClass = UnsafeClassDefiner.define(className, classByteArray, newLoader, null);
            return interfaceClass.cast(outClass.getConstructor((Class<?>[]) null).newInstance((Object[]) null));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ConstructorAccessor<T> create(ClassLoader loader,
                                                Constructor<T> constructor
    ) throws NoSuchMethodException {
        return (ConstructorAccessor<T>) createConstructor(
                loader,
                ConstructorAccessor.class,
                ConstructorDesc.of(
                        constructor.getDeclaringClass(),
                        constructor.getDeclaringClass(),
                        constructor.getParameterTypes()
                ),
                null,
                MethodDesc.of(ConstructorAccessor.class, "newInstance", Object.class, Object[].class),
                false
        );
    }
    
    @SuppressWarnings("unchecked")
    public static <T> ConstructorAccessor<T> create(ClassLoader loader,
                                                    Class<T> actualOwnerClass,
                                                    Constructor<? super T> constructor
    ) throws NoSuchMethodException {
        return (ConstructorAccessor<T>) createConstructor(
                loader,
                ConstructorAccessor.class,
                ConstructorDesc.of(
                        actualOwnerClass,
                        constructor.getDeclaringClass(),
                        constructor.getParameterTypes()
                ),
                null,
                MethodDesc.of(ConstructorAccessor.class, "newInstance", Object.class, Object[].class),
                false
        );
    }

    public static <T> T create(ClassLoader loader,
                               Class<T> interfaceClass,
                               ConstructorDesc targetConstructor,
                               MethodDesc interfaceMethodExact,
                               MethodDesc interfaceMethodVarargs,
                               boolean checkExist
    ) throws NoSuchMethodException {
        return createConstructor(
                loader,
                interfaceClass,
                targetConstructor,
                interfaceMethodExact,
                interfaceMethodVarargs,
                checkExist
        );
    }
}
