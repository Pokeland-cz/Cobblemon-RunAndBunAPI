package net.ctengine.util;

import net.ctengine.CTEngine;

import java.util.HashMap;
import java.util.Map;

public class CastingUtil {
    public static <T> T safeCast(Object obj, Class<T> desiredClass) {
        return safeCast(obj, desiredClass, false);
    }

    // Safe cast will try cast an object to a specific type, if it can't be cast to
    // that type then return null. Very useful when dealing with a lot of JSON values.
    // Optional doLog to log if the object can't be cast to that type.
    public static <T> T safeCast(Object obj, Class<T> desiredClass, boolean doLog) {
        if (desiredClass.isInstance(obj)) return desiredClass.cast(obj);
        if (doLog && obj != null) CTEngine.LOGGER.info("Object: "+obj+" is not of type: "+desiredClass);

        return null;
    }

    // Sometimes when loading in JSON I do a safe cast to a Map using instanceof
    // but you can't specify types in instanceof so this will rebuild the Map as
    // the normalised Map<String, Object>
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
