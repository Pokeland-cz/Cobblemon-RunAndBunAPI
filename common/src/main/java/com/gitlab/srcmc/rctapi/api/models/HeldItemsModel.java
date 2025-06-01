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
package com.gitlab.srcmc.rctapi.api.models;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * A pojo class for parsing lists of held items to allow fallback values. Supports
 * assignment from string literals for lists with 1 element.
 */
public class HeldItemsModel implements Serializable {
    private static final long serialVersionUID = 0L;

    private String[] itemIds;

    public HeldItemsModel() {
        this.itemIds = new String[0];
    }

    public HeldItemsModel(String itemId) {
        if(itemId.isBlank()) {
            this.itemIds = new String[0];
        } else {
            this.itemIds = new String[]{itemId};
        }
    }

    public HeldItemsModel(Collection<String> itemIds) {
        this.itemIds = itemIds.stream().filter(id -> !id.isBlank()).toArray(n -> new String[n]);
    }

    public String[] getItemIds() {
        return this.itemIds;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.itemIds);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof HeldItemsModel other) && (this == other || Arrays.equals(this.itemIds, other.itemIds));
    }

    public static class Deserializer implements JsonDeserializer<HeldItemsModel> {
        @Override
        public HeldItemsModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                var id = json.getAsString();
                return new HeldItemsModel(id);
            } catch(UnsupportedOperationException | IllegalStateException e) {
                var list = new ArrayList<String>();
                json.getAsJsonArray().forEach(je -> list.add(je.getAsString()));
                return new HeldItemsModel(list);
            }
        }
    }
}
