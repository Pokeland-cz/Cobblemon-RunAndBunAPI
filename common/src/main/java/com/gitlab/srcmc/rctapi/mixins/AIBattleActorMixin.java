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

import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.api.battle.BattleState;

/**
 * Fixes bug (?) in {@link AIBattleActor#onChoiceRequested()}. Potential NPE if no
 * request is present. (TODO open ticket: 'request?' instead of 'request!!')
 * 
 * @see https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/kotlin/com/cobblemon/mod/common/api/battles/model/actor/AIBattleActor.kt#L38
 * 
 * Note: Appears to be fixed in latest Cobblemon.
 */
@Mixin(AIBattleActor.class)
public abstract class AIBattleActorMixin extends BattleActor {
    public AIBattleActorMixin(UUID arg0, List<BattlePokemon> arg1) {
        super(arg0, arg1);
    }

    @Inject(method = "onChoiceRequested", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectOnChoiceRequested(CallbackInfo ci) {
        if(BattleState.findFirst(this.getBattle()) != null) {
            if(this.getRequest() == null) {
                ci.cancel();
            }
        }
    }
}
