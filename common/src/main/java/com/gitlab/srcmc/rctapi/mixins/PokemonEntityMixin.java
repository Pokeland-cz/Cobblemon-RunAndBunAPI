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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import com.gitlab.srcmc.rctapi.api.RCTApi;

/**
 * Ensures pokemon entites from {@link Trainer}s are never saved to the world.
 */
@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin {
    @Inject(method = "shouldBeSaved", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectShouldBeSaved(CallbackInfoReturnable<Boolean> cir) {
        var self = (PokemonEntity)(Object)this;
        var ot = self.getPokemon().getOriginalTrainer();

        if(ot != null && RCTApi.getInstance().getTrainerRegistry().getById(ot) != null) {
            cir.setReturnValue(false);
        }
    }
}
