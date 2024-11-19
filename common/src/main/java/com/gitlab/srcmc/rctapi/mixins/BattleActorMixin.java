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

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;

import com.gitlab.srcmc.rctapi.api.RCTApi;

/**
 * Restricts usage of bag items based on configurable limits per battle.
 * 
 * @see BagItemInstructionMixin
 */
@Mixin(BattleActor.class)
public class BattleActorMixin {
    // This is the only place I could figure to prevent the usage of items for any
    // battle actors. By the looks of it it shouldn't have any other than the desired
    // effect (especially since this injection only is effective in trainer battles
    // started by this api due to the check for a known battle state).
    @Inject(method = "canFitForcedAction", at = @At("RETURN"), cancellable = true, remap = false)
    private void injectCanFitForcedAction(CallbackInfoReturnable<Boolean> cir) {
        var self = (BattleActor)(Object)this;
        var battleState = RCTApi.getInstance().getBattleManager().getState(self.battle.getBattleId());
        
        if(cir.getReturnValue() && battleState != null) {
            var maxItems = battleState.getRules().getMaxItemUses();
            
            if(maxItems >= 0) {
                var actorState = battleState.getState(self.getUuid());
                cir.setReturnValue(actorState.getItemsUsed() < maxItems);
            }
        }
    }
}
