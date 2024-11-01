package me.z7087.name;

import me.z7087.name.generatedclasses.Accessor;
import me.z7087.name.generatedclasses.GeneratedClassesAccess;
import me.z7087.name.generatedclasses.Here;
import me.z7087.name.util.JavaVersion;
import me.z7087.name.util.ReflectionUtil;
import org.objectweb.asm.ClassWriter;

import java.lang.invoke.MethodHandles;

import static org.objectweb.asm.Opcodes.*;

// this class generates a dangerous class that extends MagicAccessorImpl and make it public,
// and user can define classes that extends it at system classloader
// to bypass access control and class bytecode verify.
public final class AccessorClassGenerator extends AbstractClassGenerator {
    private static final AccessorClassGenerator INSTANCE;

    @SuppressWarnings("UnusedAssignment")
    private static volatile boolean done = false;

    static {
        INSTANCE = new AccessorClassGenerator(Here.PATH, JavaVersion.getInstance().getVersion() >= 9);
        done = true;
    }

    public static AccessorClassGenerator getInstance() {
        return INSTANCE;
    }

    private final Class<?> generatedDoorClass;

    private final Accessor generatedAccessorInstance;

    private AccessorClassGenerator(String path, boolean isJ9) {
        {
            final String className = path + "Door";
            final byte[] classByteArray;
            {
                final ClassWriter cw = new ClassWriter(0);
                if (!isJ9) {
                    cw.visit(V1_6, ACC_PUBLIC, className, null, "sun/reflect/MagicAccessorImpl", null);
                    writeConstructor(cw, "sun/reflect/MagicAccessorImpl");
                } else {
                    cw.visit(V1_6, ACC_PUBLIC, className, null, "jdk/internal/reflect/MagicAccessorImpl", null);
                    writeConstructor(cw, "jdk/internal/reflect/MagicAccessorImpl");
                }
                classByteArray = writeClassTail(cw);
            }
            assert ReflectionUtil.theUnsafe != null;
            generatedDoorClass = UnsafeClassDefiner.define(className, classByteArray);
        }
        {
            generatedAccessorInstance = GeneratedClassesAccess.createAccessorImpl();
        }
    }

    public static boolean isDone() {
        return done;
    }

    public Class<?> getGeneratedDoorClass() {
        return generatedDoorClass;
    }

    public Accessor getGeneratedAccessorInstance() {
        return generatedAccessorInstance;
    }
}
