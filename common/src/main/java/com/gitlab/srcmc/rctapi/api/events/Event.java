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
package com.gitlab.srcmc.rctapi.api.events;

import org.jetbrains.annotations.NotNull;

/**
 * A generic event for an arbitrary type.
 */
public class Event<T> {
    private EventType<T> type;
    private T value;

    /**
     * Constructs a new {@link Event} for the given {@link EventType} and value.
     * 
     * @param type {@link EventType} of the {@link Event}.
     * @param value Event value.
     */
    Event(@NotNull EventType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Retrieves the values passed along with this {@link Event}.
     * 
     * @return Event value.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Retrieves the {@link EventType} of this {@link Event}.
     * 
     * @return {@link EventType} of this {@link Event}.
     */
    @NotNull
    public EventType<T> getType() {
        return this.type;
    }
}
