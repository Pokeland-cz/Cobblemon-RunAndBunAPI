/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2024, HDainester, All rights reserved.
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

import java.util.function.Supplier;

import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerRegistry;

/**
 * API entrypoint (see {@link RCTApi#init(TrainerRegistry, BattleManager)} and
 * {@link RCTApi#getInstance()})).
 */
public class RCTApi {
    private static Supplier<RCTApi> instanceSupplier = () -> {
        var instance = new RCTApi(new TrainerRegistry(), new BattleManager());
        instanceSupplier = () -> instance;
        return instance;
    };

    private TrainerRegistry trainerRegistry;
    private BattleManager battleManager;

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

    /**
     * Initializes a new singleton instance that can be accessed with {@link
     * RCTApi#getInstance()}.
     * 
     * @param trainerRegistry {@link TrainerRegistry} to use.
     * @param battleManager {@link BattleManager} to use.
     */
    public static void init(TrainerRegistry trainerRegistry, BattleManager battleManager) {
        var instance = new RCTApi(trainerRegistry, battleManager);
        instanceSupplier = () -> instance;
    }

    /**
     * Retrieves the singleton instance of the {@link RCTApi} service.
     * 
     * @return {@link RCTApi} singleton.
     */
    public static RCTApi getInstance() {
        return instanceSupplier.get();
    }
}
