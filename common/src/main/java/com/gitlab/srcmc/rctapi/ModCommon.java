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
package com.gitlab.srcmc.rctapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.entity.PokemonEntitySaveToWorldEvent;
import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.ai.config.RCTBattleAIConfig;
import com.gitlab.srcmc.rctapi.api.ai.config.SelfdotGen5AIConfig;
import com.gitlab.srcmc.rctapi.api.ai.config.StrongBattleAIConfig;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;

import dev.architectury.event.events.common.TickEvent;
import kotlin.Unit;
import net.minecraft.server.MinecraftServer;

/**
 * Mod initialization logic.
 */
public class ModCommon {
    public static final String MOD_ID = "rctapi";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        RCTBattleAIConfig.register();
        StrongBattleAIConfig.register();
        SelfdotGen5AIConfig.register();

        TickEvent.SERVER_POST.register(ModCommon::handleServerTick);
        CobblemonEvents.POKEMON_ENTITY_SAVE_TO_WORLD.subscribe(Priority.HIGH, ModCommon::handlePokemonEntitySaveToWorld);
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.HIGH, ModCommon::handleBattleFainted);
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, ModCommon::handleBattleVictory);
        CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, ModCommon::handleBattleFled);
    }

    static void handleServerTick(MinecraftServer server) {
        BattleManager.tick();
    }

    static Unit handlePokemonEntitySaveToWorld(PokemonEntitySaveToWorldEvent event) {
        var pkmn = event.getPokemonEntity().getPokemon();

        if(pkmn.getOriginalTrainer() != null && RCTApi.getInstances().map(e -> e.getValue()).anyMatch(api -> api.getTrainerRegistry().getByOT(pkmn) instanceof TrainerNPC)) {
            event.cancel();
        }
        
        return Unit.INSTANCE;
    }

    static Unit handleBattleFainted(BattleFaintedEvent event) {
        BattleStates.notifyPokemonFainted(event.getBattle(), event.getKilled());
        return Unit.INSTANCE;
    }

    static Unit handleBattleVictory(BattleVictoryEvent event) {
        // TODO: why does Cobblemon not do this?
        var battle = event.getBattle();
        battle.setWinners(event.getWinners());
        battle.setLosers(event.getLosers());
        BattleManager.queryToEnd(event.getBattle());
        return Unit.INSTANCE;
    }

    static Unit handleBattleFled(BattleFledEvent event) {
        BattleStates.notifyBattleEnded(event.getBattle());
        return Unit.INSTANCE;
    }
}
