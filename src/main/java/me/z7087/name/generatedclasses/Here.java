package me.z7087.name.generatedclasses;

// for shadow plugin
public final class Here {
    private Here() {}

    public static final String NAME = Here.class.getName();
    public static final String SIMPLE_NAME = Here.class.getSimpleName();
    public static final String PATH = NAME.substring(0, NAME.lastIndexOf(SIMPLE_NAME)).replace('.', '/');
}
