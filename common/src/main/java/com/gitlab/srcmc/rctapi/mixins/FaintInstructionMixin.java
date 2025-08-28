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
import com.cobblemon.mod.common.battles.interpreter.instructions.FaintInstruction;
import com.gitlab.srcmc.rctapi.ModCommon;

/**
 * For debugging/testing.
 */
@Mixin(FaintInstruction.class)
public abstract class FaintInstructionMixin {
    // @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    // private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
    //     var self = (FaintInstruction)(Object)this;
    //     var pkmn = self.getFaintingPokemon();
    //     ModCommon.LOG.info("FAINT INSTR: " + pkmn.getName().getString() + ": " + self.getMessage());
    // }
}