package me.z7087.name;

import me.z7087.name.api.FieldAccessor;
import me.z7087.name.generatedclasses.Here;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;

import static org.objectweb.asm.Opcodes.*;

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

    private static ClassWriter writeClassHeadForFinal(String className,
                                                      String signature,
                                                      String superName,
                                                      String[] interfaces,
                                                      FieldDesc targetField,
                                                      boolean isStatic
    ) {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_6, ACC_PUBLIC, className, signature, superName, interfaces);
        cw.visitField(
                ACC_PRIVATE | ACC_FINAL,
                "offset",
                "J",
                null,
                null).visitEnd();
        cw.visitField(
                ACC_PRIVATE | ACC_FINAL,
                "base",
                "Ljava/lang/Object;",
                null,
                null).visitEnd();
        writeConstructorForFinal(cw, className, superName, targetField, isStatic);
        return cw;
    }

    @SuppressWarnings("CommentedOutCode")
    private static void writeConstructorForFinal(ClassWriter cw,
                                                 String className,
                                                 String superName,
                                                 FieldDesc targetField,
                                                 boolean isStatic
    ) {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
        // Field[] fl = targetFieldClass.class.privateGetDeclaredFields(false);
        // int arrayLength = fl.length;
        // int i = 0;
        mv.visitLdcInsn(Type.getType(targetField.getOwnerType()));
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "privateGetDeclaredFields", "(Z)[Ljava/lang/reflect/Field;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 3);
        // for (; i < arrayLength; i++) {
        //     Field temp = fl[i];
        //     if (temp.getName.equals("targetFieldName") && temp.getType.equals(targetFieldType.class)) {
        //        // if static:
        //         this.offset = unsafe.staticFieldOffset(temp);
        //         this.base = unsafe.staticFieldBase(temp);
        //        // if end, else:
        //         this.offset = unsafe.objectFieldOffset(temp);
        //         this.base = null;
        //        // else end
        //         return;
        //     }
        // }
        // throw new IllegalArgumentException(new NoSuchFieldException("targetFieldClassName.targetFieldName targetFieldType"));
        Label loopStart = new Label();
        Label loopEnd = new Label();
        Label loopBackToStart = new Label();
        mv.visitLabel(loopStart);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitJumpInsn(IF_ICMPGE, loopEnd);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, 4);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "getName", "()Ljava/lang/String;", false);
        mv.visitLdcInsn(targetField.getName());
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        mv.visitJumpInsn(IFEQ, loopBackToStart);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "getType", "()Ljava/lang/Class;", false);
        switch (targetField.getType().charAt(0)) {
            case 'L':
            case '[':
                mv.visitLdcInsn(Type.getType(targetField.getType()));
                break;
            case 'Z':
                mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                break;
            case 'B':
                mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                break;
            case 'C':
                mv.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
                break;
            case 'D':
                mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                break;
            case 'F':
                mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                break;
            case 'I':
                mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                break;
            case 'J':
                mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                break;
            case 'S':
                mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                break;
            default:
                shouldNotReachHere();
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
        mv.visitJumpInsn(IFEQ, loopBackToStart);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETSTATIC, "sun/misc/Unsafe", "theUnsafe", "Lsun/misc/Unsafe;");
        mv.visitVarInsn(ALOAD, 4);
        if (isStatic) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "staticFieldOffset", "(Ljava/lang/reflect/Field;)J", false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "objectFieldOffset", "(Ljava/lang/reflect/Field;)J", false);
        }
        mv.visitFieldInsn(PUTFIELD, className, "offset", "J");
        mv.visitVarInsn(ALOAD, 0);
        if (isStatic) {
            mv.visitFieldInsn(GETSTATIC, "sun/misc/Unsafe", "theUnsafe", "Lsun/misc/Unsafe;");
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "staticFieldBase", "(Ljava/lang/reflect/Field;)Ljava/lang/Object;", false);
        } else {
            mv.visitInsn(ACONST_NULL);
        }
        mv.visitFieldInsn(PUTFIELD, className, "base", "Ljava/lang/Object;");
        mv.visitInsn(RETURN);
        mv.visitLabel(loopBackToStart);
        mv.visitIincInsn(3, 1);
        mv.visitJumpInsn(GOTO, loopStart);
        mv.visitLabel(loopEnd);
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/NoSuchFieldException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn(targetField.getOwnerClass() + "." + targetField.getName() + " " + targetField.getType());
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoSuchFieldException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/Throwable;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private static <T> T createField(ClassLoader loader,
                                     Class<T> interfaceClass,
                                     FieldDesc targetField,
                                     MethodDesc interfaceMethodGet,
                                     MethodDesc interfaceMethodSet,
                                     boolean isStatic,
                                     boolean isFinal,
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
            findDeclaredField(loader, targetField, isStatic);
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
        final ClassWriter cw;
        if (isFinal && interfaceMethodSet != null) {
            cw = writeClassHeadForFinal(className, null, superClassName, interfaces, targetField, isStatic);
        } else {
            cw = writeClassHead(className, null, superClassName, interfaces);
        }
        if (interfaceMethodGet != null) {
            final MethodVisitor mvGetter = writeMethodHead(cw, interfaceMethodGet.getName(), "(" + interfaceMethodGet.getMergedParamTypes() + ")" + interfaceMethodGet.getReturnType());
            if (!isStatic) {
                mvGetter.visitVarInsn(ALOAD, 1);
                if (!knownNoNeedToCheckCast(interfaceMethodGet.getParamTypesRaw()[0], targetField.getOwnerType()))
                    mvGetter.visitTypeInsn(CHECKCAST, targetField.getOwnerClass());
            }
            mvGetter.visitFieldInsn(
                    isStatic
                            ? GETSTATIC
                            : GETFIELD,
                    targetField.getOwnerClass(), targetField.getName(), targetField.getType()
            );
            checkCastOrBox(mvGetter,
                    targetField.getType(),
                    interfaceMethodGet.getReturnType()
            );
            switch (interfaceMethodGet.getReturnType().charAt(0)) {
                case 'L':
                case '[':
                    mvGetter.visitInsn(ARETURN);
                    break;
                case 'Z':
                case 'B':
                case 'C':
                case 'I':
                case 'S':
                    mvGetter.visitInsn(IRETURN);
                    break;
                case 'D':
                    mvGetter.visitInsn(DRETURN);
                    break;
                case 'F':
                    mvGetter.visitInsn(FRETURN);
                    break;
                case 'J':
                    mvGetter.visitInsn(LRETURN);
                    break;
                default:
                    shouldNotReachHere();
            }
            writeMethodTail(mvGetter);
        }
        if (interfaceMethodSet != null) {
            final MethodVisitor mvSetter = writeMethodHead(cw, interfaceMethodSet.getName(), "(" + interfaceMethodSet.getMergedParamTypes() + ")" + interfaceMethodSet.getReturnType());
            if (isFinal) {
                mvSetter.visitFieldInsn(GETSTATIC, "sun/misc/Unsafe", "theUnsafe", "Lsun/misc/Unsafe;");
                if (isStatic) {
                    mvSetter.visitVarInsn(ALOAD, 0);
                    mvSetter.visitFieldInsn(GETFIELD, className, "base", "Ljava/lang/Object;");
                } else {
                    mvSetter.visitVarInsn(ALOAD, 1);
                }
                mvSetter.visitVarInsn(ALOAD, 0);
                mvSetter.visitFieldInsn(GETFIELD, className, "offset", "J");
                switch (interfaceMethodSet.getParamTypesRaw()[interfaceMethodSet.getParamTypesRaw().length - 1].charAt(0)) {
                    case 'L':
                    case '[':
                        mvSetter.visitVarInsn(ALOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    case 'Z':
                    case 'B':
                    case 'C':
                    case 'I':
                    case 'S':
                        mvSetter.visitVarInsn(ILOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    case 'D':
                        mvSetter.visitVarInsn(DLOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    case 'F':
                        mvSetter.visitVarInsn(FLOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    case 'J':
                        mvSetter.visitVarInsn(LLOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    default:
                        shouldNotReachHere();
                }
                checkCastOrBox(mvSetter,
                        interfaceMethodSet.getParamTypesRaw()[interfaceMethodSet.getParamTypesRaw().length - 1],
                        targetField.getType()
                );
                String unsafeMethodName = null, unsafeMethodDesc = null;
                switch (targetField.getType().charAt(0)) {
                    case 'L':
                    case '[':
                        unsafeMethodName = "putObjectVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JLjava/lang/Object;)V";
                        break;
                    case 'Z':
                        unsafeMethodName = "putBooleanVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JZ)V";
                        break;
                    case 'B':
                        unsafeMethodName = "putByteVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JB)V";
                        break;
                    case 'C':
                        unsafeMethodName = "putCharVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JC)V";
                        break;
                    case 'I':
                        unsafeMethodName = "putIntVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JI)V";
                        break;
                    case 'S':
                        unsafeMethodName = "putShortVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JS)V";
                        break;
                    case 'D':
                        unsafeMethodName = "putDoubleVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JD)V";
                        break;
                    case 'F':
                        unsafeMethodName = "putFloatVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JF)V";
                        break;
                    case 'J':
                        unsafeMethodName = "putLongVolatile";
                        unsafeMethodDesc = "(Ljava/lang/Object;JJ)V";
                        break;
                    default:
                        shouldNotReachHere();
                }
                mvSetter.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", unsafeMethodName, unsafeMethodDesc, false);
                mvSetter.visitInsn(RETURN);
            } else {
                if (!isStatic) {
                    mvSetter.visitVarInsn(ALOAD, 1);
                    if (!knownNoNeedToCheckCast(interfaceMethodSet.getParamTypesRaw()[0], targetField.getOwnerType()))
                        mvSetter.visitTypeInsn(CHECKCAST, targetField.getOwnerClass());
                }
                switch (interfaceMethodSet.getParamTypesRaw()[interfaceMethodSet.getParamTypesRaw().length - 1].charAt(0)) {
                    case 'L':
                    case '[':
                        mvSetter.visitVarInsn(ALOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    case 'Z':
                    case 'B':
                    case 'C':
                    case 'I':
                    case 'S':
                        mvSetter.visitVarInsn(ILOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    case 'D':
                        mvSetter.visitVarInsn(DLOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    case 'F':
                        mvSetter.visitVarInsn(FLOAD, interfaceMethodSet.getParamTypesRaw().length);
                        break;
                    case 'J':
                        mvSetter.visitVarInsn(LLOAD, interfaceMethodSet.getParamTypesRaw().length);
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
                                ? PUTSTATIC
                                : PUTFIELD,
                        targetField.getOwnerClass(), targetField.getName(), targetField.getType()
                );
                mvSetter.visitInsn(RETURN);
            }
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
                isFinal(field.getModifiers()),
                false
        );
    }

    public static <T> T create(ClassLoader loader,
                               Class<T> getterOrSetterOwnerClass,
                               FieldDesc targetField,
                               MethodDesc interfaceMethodGet,
                               MethodDesc interfaceMethodSet,
                               boolean isStatic,
                               boolean isFinal,
                               boolean checkExist
    ) throws NoSuchMethodException, NoSuchFieldException {
        return createField(loader, getterOrSetterOwnerClass, targetField, interfaceMethodGet, interfaceMethodSet, isStatic, isFinal, checkExist);
    }
}
