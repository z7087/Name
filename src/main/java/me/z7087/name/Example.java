package me.z7087.name;

import me.z7087.name.api.ConstructorAccessor;
import me.z7087.name.api.FieldAccessor;
import me.z7087.name.api.MethodAccessor;

public class Example {
    private Example() {
        System.out.println("Example <init> running");
    }
    
    private static final int i = 0;
    private static final int[] ia = new int[1];
    private static void a() throws NoSuchFieldException, IllegalAccessException {
        System.out.println("YAY");
        java.lang.reflect.Field f = Example.class.getDeclaredField("i");
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
        printTime("field accessor class 1", new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                FieldAccessor<Object, Integer> field = null;
                try {
                    field = (FieldAccessor<Object, Integer>) FieldFactory.create(
                            Example.class.getClassLoader(),
                            Example.class.getDeclaredField("i")
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(field.get(null)); // 0
                field.set(null, 1); // final fields cannot take effect instantly... but why?
                System.out.println(i); // 0
                System.out.println(field.get(null)); // 1
            }
        });
        printTime("field accessor class 2", new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                FieldAccessor<Object, Integer> field = null;
                try {
                    field = (FieldAccessor<Object, Integer>) FieldFactory.create(
                            Example.class.getClassLoader(),
                            Example.class.getDeclaredField("i")
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(field.get(null)); // 1
                field.set(null, 2);
                System.out.println(i); // 0
                System.out.println(field.get(null)); // 2
            }
        });
        printTime("field accessor class 3", new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                FieldAccessor<Object, int[]> field = null;
                try {
                    field = (FieldAccessor<Object, int[]>) FieldFactory.create(
                            Example.class.getClassLoader(),
                            Example.class.getDeclaredField("ia")
                    );
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(ia.length); // 1
                field.set(null, new int[3]); // instantly, but why?
                System.out.println(ia.length); // 3
                System.out.println(field.get(null).length); // 3
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
    }
}