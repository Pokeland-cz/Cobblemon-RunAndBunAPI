package net.ctengine.api.util;

import org.jetbrains.annotations.NotNull;

public class Locations {
    /**
     * Creates a resource location path and prepends the defaultNamespace if the given
     * location does not define a namespace itself.
     * 
     * @param defaultNamespace Default namespace to prepend.
     * @param location Resource location path to convert.
     * @return Resource location path with namespace.
     */
    @NotNull
    public static String withNamespace(@NotNull String defaultNamespace, @NotNull String location) {
        if(location.indexOf(':') < 0) {
            location = defaultNamespace + ':' + location;
        }

        return location;
    }

    /**
     * Retrieves the path component of the given resource location path.
     * 
     * @param location Resource location path (potentially with namespace).
     * @return Path component of resource location path.
     */
    @NotNull
    public static String withoutNamespace(@NotNull String location) {
        int i = location.indexOf(':');

        if(i < 0) {
            return location;
        }

        return location.substring(i + 1);
    }
}
