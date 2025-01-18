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

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * A pojo class for parsing trainer {@link BagItem}s.
 */
public class BagItemModel {
    private String item;
    private int quantity;

    /**
     * Creates a new BagItemModel.
     */
    public BagItemModel() {
        this("", 1);
    }

    /**
     * Creates a new BagItemModel with the given properties.
     * 
     * @param item Item id (namespace:item).
     * @param quantity Item quantity available per battle.
     */
    public BagItemModel(@NotNull String item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    @NotNull
    public String getItem() {
        return this.item;
    }

    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.item, this.quantity);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BagItemModel other)
            && this.quantity == other.quantity
            && this.item.equals(other.item);
    }
}
