package dev.velix.imperat.util.jeflect;

import java.util.HashMap;
import java.util.Map;

final class Util {
    private Util() {
    }

    static Map<String, Class<?>> getPrimitives() {
        var ret = new HashMap<String, Class<?>>();
        ret.put("void", void.class);
        ret.put("byte", byte.class);
        ret.put("short", short.class);
        ret.put("int", int.class);
        ret.put("long", long.class);
        ret.put("char", char.class);
        ret.put("float", float.class);
        ret.put("double", double.class);
        ret.put("boolean", boolean.class);
        return ret;
    }
}
