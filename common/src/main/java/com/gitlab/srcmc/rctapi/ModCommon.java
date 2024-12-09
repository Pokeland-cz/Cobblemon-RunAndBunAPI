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
package com.gitlab.srcmc.rctapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import kotlin.Unit;

/**
 * Mod initialization logic.
 */
public class ModCommon {
    public static final String MOD_ID = "rctapi";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.HIGH, ModCommon::handleBattleFainted);
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, ModCommon::handleBattleVictory);
        CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, ModCommon::handleBattleFled);
        // CobblemonEvents.COBBLEMON_INITIALISED.subscribe(Priority.NORMAL, ModCommon::handleCobblemonInitialized);
    }

    // static Unit handleCobblemonInitialized(Unit unit) {
    //     return unit;
    // }

    static Unit handleBattleFainted(BattleFaintedEvent event) {
        BattleStates.notifyPokemonFainted(event.getBattle(), event.getKilled());
        return Unit.INSTANCE;
    }

    static Unit handleBattleVictory(BattleVictoryEvent event) {
        BattleStates.notifyBattleEnded(event.getBattle());
        return Unit.INSTANCE;
    }

    static Unit handleBattleFled(BattleFledEvent event) {
        BattleStates.notifyBattleEnded(event.getBattle());
        return Unit.INSTANCE;
    }
}
