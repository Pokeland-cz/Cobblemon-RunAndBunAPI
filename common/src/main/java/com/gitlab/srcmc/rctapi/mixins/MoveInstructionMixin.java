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
package com.gitlab.srcmc.rctapi.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.interpreter.instructions.MoveInstruction;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.ai.utils.MoveType;

/**
 * Custom event for successful moves.
 * 
 * @see MoveType#handle(String, com.cobblemon.mod.common.battles.pokemon.BattlePokemon, com.cobblemon.mod.common.battles.pokemon.BattlePokemon)
 */
@Mixin(MoveInstruction.class)
public abstract class MoveInstructionMixin {
    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
        var self = (MoveInstruction)(Object)this;
        var battleState = RCTApi.getInstances()
            .map(e -> e.getValue().getBattleManager()
            .getState(battle.getBattleId()))
            .filter(bs -> bs != null)
            .findFirst().orElse(null);

        if(battleState != null) {
            var message = self.getMessage();

            if(!message.hasOptionalArgument("still") && !message.hasOptionalArgument("miss")) {
                var from = message.battlePokemon(0, battle);
                var to = message.battlePokemon(2, battle);
                var mv = message.argumentAt(1);

                if(from != null && to != null && mv != null) {
                    MoveType.handle(mv.toLowerCase().replaceAll("[^a-z0-9]", ""), from, to);
                } else {
                    ModCommon.LOG.warn("unexpected message: " + message.getRawMessage());
                }
            }
        }
    }
}
