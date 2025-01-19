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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jetbrains.annotations.NotNull;

/**
 * A context to manage {@link EventListener}s.
 */
public class EventContext {
    private static Set<EventListener<?>> EMPTY_SET = new HashSet<>();
    private Map<EventType<?>, Set<EventListener<?>>> listeners = new HashMap<>();

    /**
     * Registers the given {@link EventListener} to this context.
     * 
     * @param type {@link EventType} to register the {@link EventListener} for.
     * @param listener {@link EventListener} to register.
     */
    public <T> void register(@NotNull EventType<T> type, @NotNull EventListener<T> listener) {
        this.listeners.computeIfAbsent(type, t -> new CopyOnWriteArraySet<>()).add(listener);
    }

    /**
     * Unregisters the given {@link EventListener} from this context for all {@link EventType}s.
     * 
     * @param type {@link EventType} the {@link EventListener} was registered for.
     * @param listener {@link EventListener} to register.
     */
    public <T> void unregister(@NotNull EventListener<T> listener) {
        this.listeners.values().forEach(s -> s.remove(listener));
    }

    /**
     * Unregisters the given {@link EventListener} from this context for the given {@link EventType}.
     * 
     * @param type {@link EventType} the {@link EventListener} was registered for.
     * @param listener {@link EventListener} to register.
     */
    public <T> void unregister(@NotNull EventType<T> type, @NotNull EventListener<T> listener) {
        this.listeners.getOrDefault(type, EMPTY_SET).remove(listener);
    }

    /**
     * Fires the given {@link Event} by notifying all interested {@link EventListener}s.
     * 
     * @param event {@link Event} to fire.
     */
    @SuppressWarnings("unchecked")
    public <T> void fire(@NotNull Event<T> event) {
        this.listeners.getOrDefault(event.getType(), EMPTY_SET).forEach(el -> ((EventListener<T>)el).notify(event));
    }
}
