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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerRegistry;

/**
 * API entrypoint (see {@link RCTApi#initInstance(String)} and {@link
 * RCTApi#getInstance(String)})).
 */
public class RCTApi {
    private TrainerRegistry trainerRegistry;
    private BattleManager battleManager;

    private RCTApi(TrainerRegistry trainerRegistry) {
        this(trainerRegistry, RCTApi.DEFAULT_BATTLE_MANAGER);
    }

    private RCTApi(TrainerRegistry trainerRegistry, BattleManager battleManager) {
        this.trainerRegistry = trainerRegistry;
        this.battleManager = battleManager;
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

    //////////////////////////////////////////////////////////////////////////////////
    //                                    STATIC                                    //
    //////////////////////////////////////////////////////////////////////////////////

    /**
     * The default {@link BattleManager} used by {@link RCTApi#DEFEAULT_INSTANCE} and
     * any {@link RCTApi} instances that have not explicitly been initialized with a
     * different {@link BattleManager}.
     */
    private static final BattleManager DEFAULT_BATTLE_MANAGER = new BattleManager();

    /**
     * The singleton instance that is returned by {@link RCTApi#getInstance()} and by
     * {@link RCTApi#getInstance(String)} if no registered instance was found.
     */
    private static final RCTApi DEFEAULT_INSTANCE = new RCTApi(new TrainerRegistry());

    // All registered instances (does not include DEFAULT_INSTANCE).
    private static Map<String, RCTApi> instances = new HashMap<String, RCTApi>();

    /**
     * Does nothing.
     * 
     * @deprecated Use {@link RCTApi#initInstance()} instead.
     */
    public static void init(TrainerRegistry trainerRegistry, BattleManager battleManager) {
    }

    /**
     * Retrieves the {@link RCTApi#DEFEAULT_INSTANCE} singleton.
     * 
     * @return Default {@link RCTApi} instance.
     */
    public static RCTApi getInstance() {
        return RCTApi.DEFEAULT_INSTANCE;
    }

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
     * Creates and registers a new instance of the {@link RCTApi} service, which uses a
     * newly created instance of {@link TrainerRegistry} and the {@link RCTApi#DEFAULT_BATTLE_MANAGER}.
     * Does nothing if an instance for the given id is already registered.
     * 
     * @param id Unique id to register the {@link RCTApi} instance for (usually a mod id).
     * @return Registered {@link RCTApi} instance.
     */
    public static RCTApi initInstance(String id) {
        return RCTApi.initInstance(id, new TrainerRegistry(id), RCTApi.DEFAULT_BATTLE_MANAGER);
    }

    /**
     * Creates and registers a new instance of the {@link RCTApi} service, which uses
     * the {@link RCTApi#DEFAULT_BATTLE_MANAGER}. Does nothing if an instance for the
     * given id is already registered.
     * 
     * @param id Unique id to register the {@link RCTApi} instance for (usually a mod id).
     * @param trainerRegistry {@link TrainerRegistry} used by the service.
     * @return Registered {@link RCTApi} instance.
     */
    public static RCTApi initInstance(String id, TrainerRegistry trainerRegistry) {
        return RCTApi.initInstance(id, trainerRegistry, RCTApi.DEFAULT_BATTLE_MANAGER);
    }

    /**
     * Creates and registers a new instance of the {@link RCTApi} service. Does nothing
     * if an instance for the given id is already registered.
     * 
     * @param id Unique id to register the {@link RCTApi} instance for (usually a mod id).
     * @param trainerRegistry {@link TrainerRegistry} used by the service.
     * @param battleManager {@link BattleManager} used by the service.
     * @return Registered {@link RCTApi} instance.
     */
    public static RCTApi initInstance(String id, TrainerRegistry trainerRegistry, BattleManager battleManager) {
        return RCTApi.instances.computeIfAbsent(id, s -> new RCTApi(trainerRegistry, battleManager));
    }
}
