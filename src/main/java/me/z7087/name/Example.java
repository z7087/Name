package me.z7087.name;

import me.z7087.name.api.ConstructorAccessor;
import me.z7087.name.api.FieldAccessor;
import me.z7087.name.api.MethodAccessor;

public class Example {
    private Example() {
        System.out.println("Example <init> running");
    }

    private static final int iInline = 0;
    private static final int jNoInline;
    static {
        jNoInline = 0;
    }
    private static final int[] ia = new int[1];
    private static void a() throws NoSuchFieldException, IllegalAccessException {
        System.out.println("YAY");
        java.lang.reflect.Field f = Example.class.getDeclaredField("jNoInline");
        f.setAccessible(true);
        if (f.getInt(null) == -114514)
            throw new NoSuchFieldException();
    }

    private static void printTime(String name, Runnable runnable) {
        long t = System.nanoTime();
        runnable.run();
        t = System.nanoTime() - t;
        System.out.println(name + " ok: " + (t / 1000000.0) + "ms");
    }

    @SuppressWarnings("SameParameterValue")
    private static void printTime2(String name, Runnable runnable) {
        final String name1 = name + " 1";
        final String name2 = name + " 2";
        printTime(name1, runnable);
        printTime(name2, runnable);
    }

    public static void main(String[] args) throws Throwable {
        System.out.println("main ok");
        printTime("door class", new Runnable() {
            @Override
            public void run() {
                Class<?> doorClass = AccessorClassGenerator.getInstance().getGeneratedDoorClass();
                assert doorClass != null;
            }
        });
        printTime2("method accessor class", new Runnable() {
            @Override
            public void run() {
                MethodAccessor<?, ?> method = null;
                try {
                    method = MethodFactory.create(
                            Example.class.getClassLoader(),
                            Example.class.getDeclaredMethod("a", (Class<?>[]) null),
                            false
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                method.invoke(null, "unused arg");
            }
        });
        printTime2("inline field accessor class", new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                FieldAccessor<Object, Integer> field = null;
                try {
                    field = (FieldAccessor<Object, Integer>) FieldFactory.create(
                            Example.class.getClassLoader(),
                            Example.class.getDeclaredField("iInline")
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(field.get(null)); // iInline
                field.set(null, field.get(null) + 1);
                // Example$3.class puts 0 here, not iInline, so this will never change
                System.out.println(iInline); // 0
                System.out.println(field.get(null)); // iInline + 1
            }
        });
        printTime2("no inline field accessor class", new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                FieldAccessor<Object, Integer> field = null;
                try {
                    field = (FieldAccessor<Object, Integer>) FieldFactory.create(
                            Example.class.getClassLoader(),
                            Example.class.getDeclaredField("jNoInline")
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(field.get(null)); // jNoInline
                field.set(null, field.get(null) + 1);
                System.out.println(jNoInline); // jNoInline + 1
                System.out.println(field.get(null)); // jNoInline + 1
            }
        });
        printTime("constructor accessor class 1", new Runnable() {
            @Override
            public void run() {
                ConstructorAccessor<?> constructor = null;
                try {
                    constructor = ConstructorFactory.create(
                            Example.class.getClassLoader(),
                            Example.class.getDeclaredConstructor((Class<?>[]) null)
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                constructor.newInstance("unused arg");
            }
        });
        printTime("constructor accessor class 2", new Runnable() {
            @Override
            public void run() {
                ConstructorAccessor<?> constructor = null;
                try {
                    constructor = ConstructorFactory.create(
                            Example.class.getClassLoader(),
                            Example.class,
                            Object.class.getDeclaredConstructor((Class<?>[]) null)
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                constructor.newInstance("unused arg");
            }
        });
        printTime("System.security field accessor class", new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                FieldAccessor<Object, SecurityManager> field = null;
                try {
                    field = (FieldAccessor<Object, SecurityManager>) FieldFactory.create(
                            Example.class.getClassLoader(),
                            FieldAccessor.class,
                            FieldDesc.of("java/lang/System", "security", "Ljava/lang/SecurityManager;"),
                            MethodDesc.of(FieldAccessor.class, "get", Object.class, Object.class),
                            MethodDesc.of(FieldAccessor.class, "set", void.class, Object.class, Object.class),
                            true,
                            false,
                            false
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                SecurityManager old = field.get(null);
                if (old == null)
                    System.setSecurityManager(new SecurityManager());
                System.out.println(field.get(null));
                field.set(null, null);
                System.out.println(field.get(null));
                if (old != null)
                    field.set(null, old);
            }
        });
    }
}