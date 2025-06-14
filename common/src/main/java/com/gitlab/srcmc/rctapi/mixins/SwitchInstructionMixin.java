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
import com.cobblemon.mod.common.battles.interpreter.instructions.SwitchInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;

// Fixes issues related to switch instructions (especially if forced by moves).
// Selfdot original: https://github.com/davo899/CobblemonTrainers/blob/main/common/src/main/java/com/selfdot/cobblemontrainers/mixin/SwitchInstructionMixin.java
// License: https://github.com/davo899/CobblemonTrainers/blob/main/LICENSE
@Mixin(SwitchInstruction.class)
public abstract class SwitchInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getPublicMessage();

    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
        var pnxAndPokemonID = getPublicMessage().pnxAndUuid(0);

        if(pnxAndPokemonID == null) return;

        BattlePokemon battlePokemon = battle.getBattlePokemon(
            pnxAndPokemonID.component1(), pnxAndPokemonID.component2()
        );

        battlePokemon.setWillBeSwitchedIn(false);
    }

}