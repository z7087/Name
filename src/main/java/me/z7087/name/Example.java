package me.z7087.name;

import me.z7087.name.api.BaseMethodAccessor;
import me.z7087.name.api.FieldAccessor;

public class Example {
    private static int i = 0;
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
        BaseMethodAccessor<Example> method = MethodFactory.create(Example.class.getDeclaredMethod("a", (Class<?>[]) null));
        method.invoke(null, "1");
        method.invokeForVoid(null);
        t1 = System.nanoTime();
        System.out.println("method accessor class ok: " + (t1-t) / 1000000.0 + "ms");
        t = System.nanoTime();
        BaseMethodAccessor<Example> method2 = MethodFactory.create(Example.class.getDeclaredMethod("a", (Class<?>[]) null));
        method2.invoke(null, "1");
        method2.invokeForVoid(null);
        t1 = System.nanoTime();
        System.out.println("method accessor class 2 ok: " + (t1-t) / 1000000.0 + "ms");
        t = System.nanoTime();
        @SuppressWarnings("unchecked")
        FieldAccessor<Object, Integer> field = (FieldAccessor<Object, Integer>) FieldFactory.create(
                Example.class.getClassLoader(),
                Example.class.getDeclaredField("i")
        );
        System.out.println(field.get(null));
        field.set(null, 1);
        System.out.println(i);
        t1 = System.nanoTime();
        System.out.println("field accessor class ok: " + (t1-t) / 1000000.0 + "ms");
    }
}