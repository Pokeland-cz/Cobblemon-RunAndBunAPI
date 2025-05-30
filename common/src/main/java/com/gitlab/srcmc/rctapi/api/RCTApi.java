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
package com.gitlab.srcmc.rctapi.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.events.EventContext;
import com.gitlab.srcmc.rctapi.api.models.HeldItemsModel;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerRegistry;
import com.gitlab.srcmc.rctapi.api.util.Text;
import com.google.gson.GsonBuilder;

/**
 * API entrypoint (see {@link RCTApi#initInstance(String)} and {@link
 * RCTApi#getInstance(String)})).
 */
public class RCTApi {
    private TrainerRegistry trainerRegistry;
    private BattleManager battleManager;
    private EventContext eventContext;

    private RCTApi(TrainerRegistry trainerRegistry, BattleManager battleManager, EventContext eventContext) {
        this.trainerRegistry = trainerRegistry;
        this.battleManager = battleManager;
        this.eventContext = eventContext;
    }

    /**
     * Constructs a new {@link GsonBuilder} that is preconfigred with type adapters for
     * classes provided by this api.
     * 
     * @return Configured {@link GsonBuilder}
     * @see Text.Deserializer
     */
    public GsonBuilder gsonBuilder() {
        return this.configureGsonBuilder(new GsonBuilder());
    }

    /**
     * Configures type adapters for a {@link GsonBuilder} for classes provided by this
     * api.
     * 
     * @param builder {@link GsonBuilder} to configure.
     * @return Configured {@link GsonBuilder}
     * @see Text.Deserializer
     */
    public GsonBuilder configureGsonBuilder(GsonBuilder builder) {
        return builder
            .registerTypeAdapter(Text.class, new Text.Deserializer())
            .registerTypeAdapter(HeldItemsModel.class, new HeldItemsModel.Deserializer());
    }

    /**
     * Retrieves the {@link TrainerRegistry}.
     * 
     * @return {@link TrainerRegistry} instance.
     */
    public TrainerRegistry getTrainerRegistry() {
        return this.trainerRegistry;
    }

    /**
     * Retrieves the {@link BattleManager}.
     * 
     * @return {@link BattleManager} instance.
     */
    public BattleManager getBattleManager() {
        return this.battleManager;
    }

    /**
     * Retrieves the {@link EventContext} of this service.
     * 
     * @return {@link EventContext} of this service.
     */
    public EventContext getEventContext() {
        return this.eventContext;
    }

    //////////////////////////////////////////////////////////////////////////////////
    //                                    STATIC                                    //
    //////////////////////////////////////////////////////////////////////////////////

    /**
     * The default {@link EventContext} used by the {@link TrainerRegistry} and {@link
     * BattleManager} of the {@link RCTApi#DEFEAULT_INSTANCE}.
     */
    private static final EventContext DEFAULT_EVENT_CONTEXT = new EventContext();

    /**
     * The singleton instance that is returned by {@link RCTApi#getInstance()} and by
     * {@link RCTApi#getInstance(String)} if no registered instance was found.
     */
    private static final RCTApi DEFEAULT_INSTANCE = new RCTApi(new TrainerRegistry("", DEFAULT_EVENT_CONTEXT), new BattleManager(DEFAULT_EVENT_CONTEXT), DEFAULT_EVENT_CONTEXT);

    // All registered instances (does not include DEFAULT_INSTANCE).
    private static Map<String, RCTApi> instances = new ConcurrentHashMap<String, RCTApi>();

    /**
     * Retrieves the {@link RCTApi} instance for the given id.
     * 
     * @param id Id of the {@link RCTApi} instance (usually a mod id).
     * @return Registered {@link RCTApi} instance or {@link RCTApi#DEFEAULT_INSTANCE}.
     */
    public static RCTApi getInstance(String id) {
        return RCTApi.instances.getOrDefault(id, RCTApi.DEFEAULT_INSTANCE);
    }

    /**
     * Retrieves a stream of all registered {@link RCTApi} instances and the {@link
     * RCTApi#DEFEAULT_INSTANCE} (with an empty string for the id).
     * 
     * @return Stream of all {@link RCTApi} instances.
     */
    public static Stream<Map.Entry<String, RCTApi>> getInstances() {
        return Stream.concat(RCTApi.instances.entrySet().stream(), Stream.of(Map.entry("", DEFEAULT_INSTANCE)));
    }

    /**
     * Creates and registers a new instance of the {@link RCTApi} service, utilizing a
     * newly instantiated {@link TrainerRegistry} and {@link BattleManager} that share
     * the same {@link EventContext}. If an instance with the specified id is already
     * registered, no action is taken.
     * 
     * @param id Unique id to register the {@link RCTApi} instance for (usually a mod id).
     * @return Registered {@link RCTApi} instance.
     */
    public static RCTApi initInstance(String id) {
        var eventContext = new EventContext();
        return RCTApi.initInstance(id, new TrainerRegistry(id, eventContext), new BattleManager(eventContext), eventContext);
    }

    /**
     * Creates and registers a new instance of the {@link RCTApi} service. Does nothing
     * if an instance for the given id is already registered.
     * 
     * @param id Unique id to register the {@link RCTApi} instance for (usually a mod id).
     * @param trainerRegistry {@link TrainerRegistry} used by the service.
     * @param battleManager {@link BattleManager} used by the service.
     * @param eventContext {@link EventContext} used by the {@link TrainerRegistry} and {@link BattleManager}.
     * @return Registered {@link RCTApi} instance.
     */
    static RCTApi initInstance(String id, TrainerRegistry trainerRegistry, BattleManager battleManager, EventContext eventContext) {
        return RCTApi.instances.computeIfAbsent(id, s -> new RCTApi(trainerRegistry, battleManager, eventContext));
    }
}
