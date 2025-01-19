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

import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.battle.BattleState;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;

/**
 * Provides access to all {@link EventType}s defined by this api.
 */
public final class Events {
    /**
     * Fired whenever a {@link Trainer} is registred to a {@link TrainerRegistry}.
     */
    public static final EventType<Trainer> TRAINER_REGISTRED = new EventType<>();

    /**
     * Fired whenever a {@link Trainer} is unregistered from a {@link TrainerRegistry}.
     */
    public static final EventType<Trainer> TRAINER_UNREGISTRED = new EventType<>();

    /**
     * Fired whenever a battle was successfully started with a {@link BattleManager}.
     */
    public static final EventType<BattleState> BATTLE_STARTED = new EventType<>();

    /**
     * Fired whenever a battle, that previously has been started with a {@link
     * BattleManager}, has ended.
     */
    public static final EventType<BattleState> BATTLE_ENDED = new EventType<>();

    // no instances of this class
    private Events() {}
}
