package me.z7087.name;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator {
    private static final Class<?>[] classes = {
            boolean.class,
            byte.class,
            char.class,
            double.class,
            float.class,
            int.class,
            long.class,
            short.class,
            void.class,
            Object.class
    };

    private static int mpc = 5;

    private static String getClassDescName(Class<?> clazz) {
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
        return "Ljava/lang/Object;";
    }

    private static class ClassesListGenerator implements Iterable<Class<?>[]> {
        private final int maxCount;
        public ClassesListGenerator(int count) {
            maxCount = count;
        }
        @Override
        public Iterator<Class<?>[]> iterator() {
            return new IHateThis(maxCount);
        }
        private static class IHateThis implements Iterator<Class<?>[]> {
            private final int maxCount;
            private final int[] counts;
            private boolean ended = false;
            public IHateThis(int count) {
                maxCount = count;
                counts = new int[count];
            }
            @Override
            public boolean hasNext() {
                return !ended;
            }
            @Override
            public Class<?>[] next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                Class<?>[] allc = new Class<?>[maxCount];
                for (int i = 0; i < maxCount; ++i) {
                    allc[i] = classes[counts[i]];
                }
                ++counts[maxCount - 1];
                for (int i = maxCount - 1; i > -1; --i) {
                    if (counts[i] > (classes.length - 1)) {
                        counts[i] -= (classes.length - 1);
                        int up = i - 1;
                        if (up < 0)
                            ended = true;
                        else
                            ++counts[up];
                    }
                }
                return allc;
            }
        }
    }

    private static void assertTrue(boolean isTrue) {
        if (!isTrue)
            throw new RuntimeException();
    }

    private static String captureName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Deprecated
    private static void generateBaseClass(int maxObjectParamCount) {
        generateBaseClass(maxObjectParamCount, null);
    }

    private static void generateBaseClass(int maxObjectParamCount, String[] interfaces) {
        generateBaseClass(maxObjectParamCount, mpc, interfaces);
    }

    private static void generateBaseClass(int maxObjectParamCount, int maxParamCount, String[] interfaces) {
        assertTrue(maxObjectParamCount <= maxParamCount);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String className = "me/z7087/name/api/BaseMethodAccessorWithObjectParam" + maxObjectParamCount;
        StringBuilder classSig = new StringBuilder("<T:Ljava/lang/Object;");
        for (int i = 1; i <= maxObjectParamCount; ++i) {
            classSig.append("O");
            classSig.append(i);
            classSig.append(":Ljava/lang/Object;");
        }
        classSig.append(">Ljava/lang/Object;");
        cw.visit(V1_6, ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT, className, classSig.toString(), "java/lang/Object", interfaces);
        for (int i = Math.max(1, maxObjectParamCount); i <= maxParamCount; ++i) {
            for (Class<?>[] clazzes : new ClassesListGenerator(i)) {
                int objectCount = 0;
                for (Class<?> clazz : clazzes) {
                    if (clazz == void.class) {
                        objectCount = -1;
                        break;
                    }
                    if (clazz == Object.class) {
                        if (++objectCount > maxObjectParamCount)
                            break;
                    }
                }
                if (objectCount != maxObjectParamCount)
                    continue;
                for (Class<?> typeClass : classes) {
                    final StringBuilder desc = new StringBuilder("(");
                    final StringBuilder sig = new StringBuilder("(");
                    desc.append(getClassDescName(Object.class));
                    sig.append("TT;");
                    int count = 0;
                    for (Class<?> clazz : clazzes) {
                        final String descName = getClassDescName(clazz);
                        desc.append(descName);
                        if (clazz == Object.class) {
                            sig.append("TO");
                            sig.append(++count);
                            sig.append(";");
                        } else {
                            sig.append(descName);
                        }
                    }
                    assertTrue(count == maxObjectParamCount);
                    desc.append(")");
                    sig.append(")");
                    desc.append(getClassDescName(typeClass));
                    sig.append(getClassDescName(typeClass));
                    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "invokeFor" + captureName(typeClass.getSimpleName()), desc.toString(), sig.toString(), null);
                    mv.visitEnd();
                }
            }
        }
        cw.visitEnd();
        writeToFile(className, cw.toByteArray());
    }

    private static void writeToFile(String className, byte[] byteArray) {
        String classesPath = "generatedclasses/";
        String fullPath = classesPath + className;
        int pathName_index_fileName = fullPath.lastIndexOf('/');
        assertTrue(pathName_index_fileName > 0);
        String pathName = fullPath.substring(0, pathName_index_fileName + 1);
        FileOutputStream fos;
        try {
            new File(pathName).mkdirs();
            final File file = new File(fullPath + ".class");
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.close();
        } catch (Exception e) {
            System.out.print("FileOutputStream error " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateClass(int maxObjectParamCount) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String className = "me/z7087/name/api/MethodAccessorO" + maxObjectParamCount;
        StringBuilder classSig = new StringBuilder("<T:Ljava/lang/Object;");
        for (int i = 1; i <= maxObjectParamCount; ++i) {
            classSig.append("O");
            classSig.append(i);
            classSig.append(":Ljava/lang/Object;");
        }
        classSig.append(">Ljava/lang/Object;Lme/z7087/name/api/BaseMethodAccessor<TT;>;Lme/z7087/name/api/BaseMethodAccessorWithObjectParam");
        classSig.append(maxObjectParamCount);
        classSig.append("<TT;");
        for (int i = 1; i <= maxObjectParamCount; ++i) {
            classSig.append("TO");
            classSig.append(i);
            classSig.append(";");
        }
        classSig.append(">;");
        String[] interfaces = new String[2];
        interfaces[0] = "me/z7087/name/api/BaseMethodAccessor";
        interfaces[1] = "me/z7087/name/api/BaseMethodAccessorWithObjectParam" + maxObjectParamCount;
        cw.visit(V1_6, ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT, className, classSig.toString(), "java/lang/Object", interfaces);
        cw.visitEnd();
        writeToFile(className, cw.toByteArray());
    }

    private static void generateAll() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String className = "me/z7087/name/api/MethodAccessor";
        StringBuilder classSig = new StringBuilder("<T:Ljava/lang/Object;>Ljava/lang/Object;Lme/z7087/name/api/BaseMethodAccessor<TT;>;");
        for (int i = 0; i <= mpc; ++i) {
            classSig.append("Lme/z7087/name/api/MethodAccessorO");
            classSig.append(i);
            classSig.append("<TT;");
            for (int j = 0; j < i; ++j) {
                //classSig.append("TLjava/lang/Object;"); // this is wrong
                classSig.append("Ljava/lang/Object;");
            }
            classSig.append(">;");
        }
        String[] interfaces = new String[mpc + 2];
        interfaces[0] = "me/z7087/name/api/BaseMethodAccessor";
        for (int i=0; i<=mpc; ++i) {
            interfaces[i+1] = ("me/z7087/name/api/MethodAccessorO" + i);
        }
        cw.visit(V1_6, ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT, className, classSig.toString(), "java/lang/Object", interfaces);
        cw.visitEnd();
        writeToFile(className, cw.toByteArray());
    }

    private static void generateMethodAccessorCast() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String className = "me/z7087/name/MethodAccessorCast";
        cw.visit(V1_6, ACC_PUBLIC | ACC_FINAL, className, null, "java/lang/Object", null);
        {
            MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "toMethodAccessor", "(Lme/z7087/name/api/BaseMethodAccessor;)Lme/z7087/name/api/MethodAccessor;", "<T:Ljava/lang/Object;>(Lme/z7087/name/api/BaseMethodAccessor<TT;>;)Lme/z7087/name/api/MethodAccessor<TT;>;", null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(CHECKCAST, "me/z7087/name/api/MethodAccessor");
            mv.visitInsn(ARETURN);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "toMethodAccessorO0", "(Lme/z7087/name/api/BaseMethodAccessor;)Lme/z7087/name/api/MethodAccessorO0;", "<T:Ljava/lang/Object;>(Lme/z7087/name/api/BaseMethodAccessor<TT;>;)Lme/z7087/name/api/MethodAccessorO0<TT;>;", null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(CHECKCAST, "me/z7087/name/api/MethodAccessorO0");
            mv.visitInsn(ARETURN);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "toMethodAccessorO1", "(Lme/z7087/name/api/BaseMethodAccessor;)Lme/z7087/name/api/MethodAccessorO1;", "<T:Ljava/lang/Object;O1:Ljava/lang/Object;>(Lme/z7087/name/api/BaseMethodAccessor<TT;>;)Lme/z7087/name/api/MethodAccessorO1<TT;TO1;>;", null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(CHECKCAST, "me/z7087/name/api/MethodAccessorO1");
            mv.visitInsn(ARETURN);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "toMethodAccessorO2", "(Lme/z7087/name/api/BaseMethodAccessor;)Lme/z7087/name/api/MethodAccessorO2;", "<T:Ljava/lang/Object;O1:Ljava/lang/Object;O2:Ljava/lang/Object;>(Lme/z7087/name/api/BaseMethodAccessor<TT;>;)Lme/z7087/name/api/MethodAccessorO1<TT;TO1;TO2;>;", null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(CHECKCAST, "me/z7087/name/api/MethodAccessorO2");
            mv.visitInsn(ARETURN);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "toMethodAccessorO3", "(Lme/z7087/name/api/BaseMethodAccessor;)Lme/z7087/name/api/MethodAccessorO3;", "<T:Ljava/lang/Object;O1:Ljava/lang/Object;O2:Ljava/lang/Object;O3:Ljava/lang/Object;>(Lme/z7087/name/api/BaseMethodAccessor<TT;>;)Lme/z7087/name/api/MethodAccessorO1<TT;TO1;TO2;TO3;>;", null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(CHECKCAST, "me/z7087/name/api/MethodAccessorO3");
            mv.visitInsn(ARETURN);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "toMethodAccessorO4", "(Lme/z7087/name/api/BaseMethodAccessor;)Lme/z7087/name/api/MethodAccessorO4;", "<T:Ljava/lang/Object;O1:Ljava/lang/Object;O2:Ljava/lang/Object;O3:Ljava/lang/Object;O4:Ljava/lang/Object;>(Lme/z7087/name/api/BaseMethodAccessor<TT;>;)Lme/z7087/name/api/MethodAccessorO1<TT;TO1;TO2;TO3;TO4;>;", null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(CHECKCAST, "me/z7087/name/api/MethodAccessorO4");
            mv.visitInsn(ARETURN);
            mv.visitEnd();
        }
        cw.visitEnd();
        writeToFile(className, cw.toByteArray());
    }

    public static void main(String[] args) {
        mpc = 4;
        //String[] base = new String[1];
        //base[0] = "me/z7087/name/api/BaseMethodAccessor";
        String[] base = null;
        for (int i = 0; i <= mpc; ++i) {
            generateBaseClass(i, base);
        }
        for (int i = 0; i <= mpc; ++i) {
            generateClass(i);
        }
        generateAll();
        generateMethodAccessorCast();
    }
}
