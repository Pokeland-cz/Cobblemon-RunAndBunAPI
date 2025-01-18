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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cobblemon.mod.common.pokemon.Pokemon;

import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Serves as hook between Cobblemons pokemon/trainer relation and trainers
 * registered by this api.
 */
@Mixin(Pokemon.class)
public abstract class PokemonMixin {
    @Inject(method = "getOwnerEntity", at = @At("RETURN"), cancellable = true, remap = false)
    private void injectGetOwnerEntity(CallbackInfoReturnable<LivingEntity> cir) {
        if(cir.getReturnValue() == null) {
            var self = (Pokemon)(Object)this;
            Trainer npc;

            if((npc = RCTApi.getInstances().map(e -> e.getValue().getTrainerRegistry().getByOT(self)).dropWhile(n -> n == null).findFirst().orElse(null)) != null) {
                cir.setReturnValue(npc.getEntity());
            }
        }
    }
}
