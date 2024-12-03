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
package com.gitlab.srcmc.rctapi.mixins;

import com.cobblemon.mod.common.battles.interpreter.instructions.SwitchInstruction;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder;

import org.spongepowered.asm.mixin.Mixin;

/**
 * "Borrowed" from selfdot. This mixin fixes a bug in cobblemon. If a Pokemon has
 * been switched in before by a trainer then their willBeSwitchedIn attribute is
 * never reset, so they cannot be switched in a second time. This will cause a
 * softlock if not fixed.
 * 
 * Note: Not really required, simply call setWillBeSwitched(false) for a current
 * active pokemon whenever it is switched out (e.g. {@link ResponseBuilder#suggestSwitches(java.util.function.Function)}).
 */
@Mixin(SwitchInstruction.class)
public abstract class SwitchInstructionMixin {
    // @Shadow(remap = false)
    // public abstract BattleMessage getPublicMessage();

    // @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    // private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
    //     var pnxAndPokemonID = getPublicMessage().pnxAndUuid(0);

    //     if(pnxAndPokemonID != null) {
    //         battle
    //             .getBattlePokemon(pnxAndPokemonID.component1(), pnxAndPokemonID.component2())
    //             .setWillBeSwitchedIn(false);
    //     }
    // }
}
