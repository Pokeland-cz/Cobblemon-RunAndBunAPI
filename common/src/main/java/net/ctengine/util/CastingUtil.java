package net.ctengine.util;

import net.ctengine.CTEngine;

import java.util.HashMap;
import java.util.Map;

public class CastingUtil {
    public static <T> T safeCast(Object obj, Class<T> desiredClass) {
        return safeCast(obj, desiredClass, false);
    }

    public static <T> T safeCast(Object obj, Class<T> desiredClass, boolean doLog) {
        if (desiredClass.isInstance(obj)) return desiredClass.cast(obj);
        if (doLog) CTEngine.LOGGER.info("Object: "+obj+" is not of type: "+desiredClass);

        return null;
    }

    public static Map<String, Object> rebuildMap(Map<?, ?> originalMap) {
        Map<String, Object> newMap = new HashMap<>();

        // Iterate over the entries directly to avoid a second lookup
        for (Map.Entry<?, ?> entry : originalMap.entrySet()) {
            if (entry.getKey() instanceof String stringKey) {
                newMap.put(stringKey, entry.getValue());
            }
        }

        return newMap;
    }
}
