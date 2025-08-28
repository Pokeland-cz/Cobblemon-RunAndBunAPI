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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.interpreter.instructions.BagItemInstruction;

import com.gitlab.srcmc.rctapi.api.battle.BattleState;

/**
 * Required to keep track of the number of items used by actors per battle.
 * 
 * @see BattleActorMixin
 */
@Mixin(BagItemInstruction.class)
public abstract class BagItemInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
        var battleState = BattleState.findFirst(battle);

        if(battleState != null) {
            var message = this.getMessage();
            var pkmn = message.pokemonByUuid(0, battle);

            if(pkmn != null) {
                var actor = pkmn.getActor();
                var actorState = battleState.getState(actor.getUuid());
                actorState.setItemsUsed(actorState.getItemsUsed() + 1);
            }
        }
    }
}
