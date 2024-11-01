package me.z7087.name.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class JavaVersion {
    private static final Map<String, Byte> supportVersionsMap;
    static {
        final Map<String, Byte> map = new HashMap<String, Byte>();
        map.put("1.6", (byte) 6);
        map.put("1.7", (byte) 7);
        map.put("1.8", (byte) 8);
        map.put("6", (byte) 6);
        map.put("7", (byte) 7);
        map.put("8", (byte) 8);
        map.put("9", (byte) 9);
        map.put("10", (byte) 10);
        map.put("11", (byte) 11);
        map.put("12", (byte) 12);
        map.put("13", (byte) 13);
        map.put("14", (byte) 14);
        map.put("15", (byte) 15);
        map.put("16", (byte) 16);
        map.put("17", (byte) 17);
        map.put("18", (byte) 18);
        map.put("19", (byte) 19);
        map.put("20", (byte) 20);
        map.put("21", (byte) 21);
        map.put("22", (byte) 22);
        map.put("23", (byte) 23);
        supportVersionsMap = Collections.unmodifiableMap(map);
    }

    private static final JavaVersion INSTANCE = new JavaVersion();
    
    public static JavaVersion getInstance() {
        return INSTANCE;
    }

    private final byte version;

    private JavaVersion() {
        final String ver = System.getProperty("java.specification.version");
        final Byte version = supportVersionsMap.get(ver);
        if (version == null)
            throw new IllegalStateException("java.specification.version has unexpected value: " + ver);
        this.version = version;
    }

    public JavaVersion(String ver) {
        final Byte version = supportVersionsMap.get(ver);
        if (version == null)
            throw new IllegalStateException("Unexpected version: " + ver);
        this.version = version;
    }

    public byte getVersion() {
        return version;
    }
}
