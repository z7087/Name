package me.z7087.name;

import me.z7087.name.api.*;
import me.z7087.name.generatedclasses.Here;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;

public final class FieldFactory extends AbstractClassGenerator {
    private FieldFactory() {}

    private static void verify(Class<?> interfaceClass,
                               FieldDesc targetField,
                               MethodDesc interfaceMethodGet,
                               MethodDesc interfaceMethodSet,
                               boolean isStatic) {
        if (interfaceClass.isPrimitive())
            throwIllegalArgumentException("interfaceClass cannot be primitive");
        if (interfaceClass.isArray())
            throwIllegalArgumentException("interfaceClass cannot be array");

        if (interfaceMethodGet == null && interfaceMethodSet == null) {
            throwIllegalArgumentException("getter and setter cannot be both null");
        } else if (interfaceMethodGet != null && interfaceMethodSet != null) {
            if (interfaceMethodGet.equalsDescriptor(interfaceMethodSet))
                throwIllegalArgumentException("getter and setter cannot have same descriptor");
        }

        {
            final String className = getClassName(interfaceClass);
            if ((interfaceMethodGet == null || !className.equals(interfaceMethodGet.getOwnerClass()))
                    && (interfaceMethodSet == null || !className.equals(interfaceMethodSet.getOwnerClass()))) {
                throwIllegalArgumentException("interfaceClass is not the owner of any of the getter and setter");
            }
        }

        if (interfaceMethodGet != null) {
            verifyGetter(targetField, interfaceMethodGet, isStatic);
        }
        if (interfaceMethodSet != null) {
            verifySetter(targetField, interfaceMethodSet, isStatic);
        }
    }

    private static void verifyGetter(FieldDesc targetField,
                                     MethodDesc interfaceMethodGet,
                                     boolean isStatic) {
        if (isStatic) {
            switch (interfaceMethodGet.getParamTypesRaw().length) {
                case 0:
                    break;
                case 1:
                    // unused arg, but continue
                    break;
                default:
                    throwIllegalArgumentException("interfaceMethodGet has too many parameters: " + interfaceMethodGet.getParamTypesRaw().length);
            }
        } else {
            if (interfaceMethodGet.getParamTypesRaw().length != 1) {
                throwIllegalArgumentException("interfaceMethodGet has too many or too few parameters: " + interfaceMethodGet.getParamTypesRaw().length);
            }
        }
        if (interfaceMethodGet.getParamTypesRaw().length == 1) {
            if (isParamTypePrimitive(interfaceMethodGet.getParamTypesRaw()[0])) {
                throwIllegalArgumentException("interfaceMethodGet parameter 0 cannot be primitive: " + interfaceMethodGet.getParamTypesRaw()[0]);
            }
        }
        if (!canCheckCastOrBox(targetField.getType(), interfaceMethodGet.getReturnType())) {
            throwIllegalArgumentException("The return value type of interfaceMethodGet is not compatible with the type of targetField: \n" + interfaceMethodGet.getReturnType() + "\n" + targetField.getType());
        }
    }

    private static void verifySetter(FieldDesc targetField,
                                     MethodDesc interfaceMethodSet,
                                     boolean isStatic) {
        if (isStatic) {
            switch (interfaceMethodSet.getParamTypesRaw().length) {
                case 0:
                    throwIllegalArgumentException("interfaceMethodSet has not enough parameters: " + interfaceMethodSet.getParamTypesRaw().length);
                case 1:
                    break;
                case 2:
                    // unused arg, but continue
                    break;
                default:
                    throwIllegalArgumentException("interfaceMethodSet has too many parameters: " + interfaceMethodSet.getParamTypesRaw().length);
            }
        } else {
            if (interfaceMethodSet.getParamTypesRaw().length != 2) {
                throwIllegalArgumentException("interfaceMethodSet has too many or too few parameters: " + interfaceMethodSet.getParamTypesRaw().length);
            }
        }
        if (!interfaceMethodSet.getReturnType().equals("V")) {
            throwIllegalArgumentException("The return value type of interfaceMethodSet is not void: " + interfaceMethodSet.getReturnType());
        }
        if (interfaceMethodSet.getParamTypesRaw().length == 2) {
            if (isParamTypePrimitive(interfaceMethodSet.getParamTypesRaw()[0])) {
                throwIllegalArgumentException("interfaceMethodSet parameter 0 cannot be primitive: " + interfaceMethodSet.getParamTypesRaw()[0]);
            }
        }
        if (!canCheckCastOrBox(interfaceMethodSet.getParamTypesRaw()[interfaceMethodSet.getParamTypesRaw().length - 1], targetField.getType())) {
            throwIllegalArgumentException("The type of the last parameter of interfaceMethodSet is not compatible with the type of targetField: \n" + interfaceMethodSet.getParamTypesRaw()[interfaceMethodSet.getParamTypesRaw().length - 1] + "\n" + targetField.getType());
        }
    }

    private static <T> T createField(ClassLoader loader,
                                     Class<T> interfaceClass,
                                     FieldDesc targetField,
                                     MethodDesc interfaceMethodGet,
                                     MethodDesc interfaceMethodSet,
                                     boolean isStatic,
                                     boolean checkExist
    ) throws NoSuchFieldException, NoSuchMethodException {
        verify(
                interfaceClass,
                targetField,
                interfaceMethodGet,
                interfaceMethodSet,
                isStatic
        );
        assert (interfaceMethodGet != null || interfaceMethodSet != null);
        if (checkExist) {
            findField(loader, targetField, isStatic);
            if (interfaceMethodGet != null)
                findMethod(loader, interfaceMethodGet);
            if (interfaceMethodSet != null)
                findMethod(loader, interfaceMethodSet);
        }
        final Class<?> superClass = AccessorClassGenerator.getInstance().getGeneratedDoorClass();
        final String className = Here.PATH + "GeneratedClass" + getId();
        final String superClassName = getClassName(superClass);
        final String[] interfaces;
        {
            if (interfaceMethodGet == null) {
                interfaces = new String[1];
                interfaces[0] = interfaceMethodSet.getOwnerClass();
            } else if (interfaceMethodSet == null) {
                interfaces = new String[1];
                interfaces[0] = interfaceMethodGet.getOwnerClass();
            } else {
                final String getterOwnerClass = interfaceMethodGet.getOwnerClass();
                final String setterOwnerClass = interfaceMethodSet.getOwnerClass();
                if (getterOwnerClass.equals(setterOwnerClass)) {
                    interfaces = new String[1];
                    interfaces[0] = getterOwnerClass;
                } else {
                    interfaces = new String[2];
                    interfaces[0] = getterOwnerClass;
                    interfaces[1] = setterOwnerClass;
                }
            }
        }
        final ClassWriter cw = writeClassHead(className, null, superClassName, interfaces);
        if (interfaceMethodGet != null) {
            final MethodVisitor mvGetter = writeMethodHead(cw, interfaceMethodGet.getName(), "(" + interfaceMethodGet.getMergedParamTypes() + ")" + interfaceMethodGet.getReturnType());
            if (!isStatic) {
                mvGetter.visitVarInsn(Opcodes.ALOAD, 1);
                if (!knownNoNeedToCheckCast(interfaceMethodGet.getParamTypesRaw()[0], targetField.getOwnerType()))
                    mvGetter.visitTypeInsn(Opcodes.CHECKCAST, targetField.getOwnerClass());
            }
            mvGetter.visitFieldInsn(
                    isStatic
                            ? Opcodes.GETSTATIC
                            : Opcodes.GETFIELD,
                    targetField.getOwnerClass(), targetField.getName(), targetField.getType()
            );
            checkCastOrBox(mvGetter,
                    targetField.getType(),
                    interfaceMethodGet.getReturnType()
            );
            switch (interfaceMethodGet.getReturnType().charAt(0)) {
                case 'L':
                case '[':
                    mvGetter.visitInsn(Opcodes.ARETURN);
                    break;
                case 'Z':
                case 'B':
                case 'C':
                case 'I':
                case 'S':
                    mvGetter.visitInsn(Opcodes.IRETURN);
                    break;
                case 'D':
                    mvGetter.visitInsn(Opcodes.DRETURN);
                    break;
                case 'F':
                    mvGetter.visitInsn(Opcodes.FRETURN);
                    break;
                case 'J':
                    mvGetter.visitInsn(Opcodes.LRETURN);
                    break;
                default:
                    shouldNotReachHere();
            }
            writeMethodTail(mvGetter);
        }
        if (interfaceMethodSet != null) {
            final MethodVisitor mvSetter = writeMethodHead(cw, interfaceMethodSet.getName(), "(" + interfaceMethodSet.getMergedParamTypes() + ")" + interfaceMethodSet.getReturnType());
            if (!isStatic) {
                mvSetter.visitVarInsn(Opcodes.ALOAD, 1);
                if (!knownNoNeedToCheckCast(interfaceMethodSet.getParamTypesRaw()[0], targetField.getOwnerType()))
                    mvSetter.visitTypeInsn(Opcodes.CHECKCAST, targetField.getOwnerClass());
            }
            switch (interfaceMethodSet.getParamTypesRaw()[interfaceMethodSet.getParamTypesRaw().length - 1].charAt(0)) {
                case 'L':
                case '[':
                    mvSetter.visitVarInsn(Opcodes.ALOAD, interfaceMethodSet.getParamTypesRaw().length);
                    break;
                case 'Z':
                case 'B':
                case 'C':
                case 'I':
                case 'S':
                    mvSetter.visitVarInsn(Opcodes.ILOAD, interfaceMethodSet.getParamTypesRaw().length);
                    break;
                case 'D':
                    mvSetter.visitVarInsn(Opcodes.DLOAD, interfaceMethodSet.getParamTypesRaw().length);
                    break;
                case 'F':
                    mvSetter.visitVarInsn(Opcodes.FLOAD, interfaceMethodSet.getParamTypesRaw().length);
                    break;
                case 'J':
                    mvSetter.visitVarInsn(Opcodes.LLOAD, interfaceMethodSet.getParamTypesRaw().length);
                    break;
                default:
                    shouldNotReachHere();
            }
            checkCastOrBox(mvSetter,
                    interfaceMethodSet.getParamTypesRaw()[interfaceMethodSet.getParamTypesRaw().length - 1],
                    targetField.getType()
            );
            mvSetter.visitFieldInsn(
                    isStatic
                            ? Opcodes.PUTSTATIC
                            : Opcodes.PUTFIELD,
                    targetField.getOwnerClass(), targetField.getName(), targetField.getType()
            );
            mvSetter.visitInsn(Opcodes.RETURN);
            writeMethodTail(mvSetter);
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

    public static FieldAccessor<?, ?> create(ClassLoader loader, java.lang.reflect.Field field)
            throws NoSuchMethodException, NoSuchFieldException {
        return createField(
                loader,
                FieldAccessor.class,
                FieldDesc.of(field.getDeclaringClass(), field.getName(), field.getType()),
                MethodDesc.of(FieldAccessor.class, "get", Object.class, Object.class),
                MethodDesc.of(FieldAccessor.class, "set", void.class, Object.class, Object.class),
                isStatic(field.getModifiers()),
                false
        );
    }

    public static <T> T create(ClassLoader loader,
                               Class<T> getterOrSetterOwnerClass,
                               FieldDesc targetField,
                               MethodDesc interfaceMethodGet,
                               MethodDesc interfaceMethodSet,
                               boolean isStatic,
                               boolean checkExist
    ) throws NoSuchMethodException, NoSuchFieldException {
        return createField(loader, getterOrSetterOwnerClass, targetField, interfaceMethodGet, interfaceMethodSet, isStatic, checkExist);
    }
}
