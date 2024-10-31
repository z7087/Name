package me.z7087.name;

import me.z7087.name.api.FieldAccessor;
import me.z7087.name.api.MethodAccessor;

public class Example {
    private static final int i = 0;
    private static void a() {
        System.out.println("YAY");
    }
    public static void main(String[] args) throws Throwable {
        System.out.println("main ok");
        long t = System.nanoTime();
        {
            Class<?> doorClass = AccessorClassGenerator.getInstance().getGeneratedDoorClass();
            assert doorClass != null;
        }
        long t1 = System.nanoTime();
        System.out.println("door class ok: " + (t1-t) / 1000000.0 + "ms");
        t = System.nanoTime();
        MethodAccessor<?, ?> method = MethodFactory.create(
                Example.class.getClassLoader(),
                Example.class.getDeclaredMethod("a", (Class<?>[]) null),
                false
        );
        method.invoke(null, "unused arg");
        t1 = System.nanoTime();
        System.out.println("method accessor class ok: " + (t1-t) / 1000000.0 + "ms");
        t = System.nanoTime();
        MethodAccessor<?, ?> method2 = MethodFactory.create(
                Example.class.getClassLoader(),
                Example.class.getDeclaredMethod("a", (Class<?>[]) null),
                false
        );
        method2.invoke(null, "unused arg");
        t1 = System.nanoTime();
        System.out.println("method accessor class 2 ok: " + (t1-t) / 1000000.0 + "ms");
        t = System.nanoTime();
        @SuppressWarnings("unchecked")
        FieldAccessor<Object, Integer> field = (FieldAccessor<Object, Integer>) FieldFactory.create(
                Example.class.getClassLoader(),
                Example.class.getDeclaredField("i")
        );
        System.out.println(field.get(null)); // 0
        field.set(null, 1); // final fields cannot take effect instantly
        System.out.println(i); // 0
        t1 = System.nanoTime();
        System.out.println("field accessor class ok: " + (t1-t) / 1000000.0 + "ms");
        t = System.nanoTime();
        @SuppressWarnings("unchecked")
        FieldAccessor<Object, Integer> field2 = (FieldAccessor<Object, Integer>) FieldFactory.create(
                Example.class.getClassLoader(),
                Example.class.getDeclaredField("i")
        );
        System.out.println(field2.get(null)); // 1
        field2.set(null, 2); // final fields cannot take effect instantly
        System.out.println(i); // 0
        t1 = System.nanoTime();
        System.out.println("field accessor class 2 ok: " + (t1-t) / 1000000.0 + "ms");
    }
}