/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers API. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctapi.api.util;

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
