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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;

/**
 * Restricts usage of bag items based on configurable limits per battle.
 * 
 * @see BagItemInstructionMixin
 */
@Mixin(BattleActor.class)
public class BattleActorMixin {
    // Updates battle states.
    @Inject(method = "turn", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectTurn(CallbackInfo ci) {
        var self = (BattleActor)(Object)this;
        ModCommon.LOG.info("TURN: " + self.getName().getString() + ", pkmn: " + self.getActivePokemon().stream().filter(p -> p.isAlive()).count() + ", " + self.getRequest());

        var battleState = RCTApi.getInstances()
            .map(e -> e.getValue().getBattleManager()
            .getState(self.battle.getBattleId()))
            .filter(bs -> bs != null)
            .findFirst().orElse(null);
        
        if(battleState != null) {
            var bs = BattleStates.get(battleState.getBattle());
            // bs.getActorState(self).nextTurn();
            self.getActivePokemon().stream()
                .filter(ActiveBattlePokemon::hasPokemon)
                .forEach(pkmn  -> bs.getPokemonState(pkmn.getBattlePokemon()).nextTurn());
        }

        // // reimplementation in java
        // var request = self.getRequest();

        // if(request == null) {
        //     ci.cancel();
        //     return;
        // }

        // self.getResponses().clear();

        // if(self.getActivePokemon().stream().anyMatch(p -> p.isAlive())) {
        //     ModCommon.LOG.info("SENDING CHOICE PACKET");
        //     self.setMustChoose(true);
        //     self.sendUpdate(new BattleMakeChoicePacket());
        // }

        // var requestActive = request.getActive();

        // if(requestActive == null || requestActive.isEmpty() || request.getWait()) {
        //     ModCommon.LOG.info("REQUEST NOT ACTIVE: " + (requestActive == null) + ", " + requestActive.isEmpty() + ", " + request.getWait());
        //     self.setRequest(null);
        //     self.getExpectingPassActions().clear();
        // }

        // ci.cancel();
    }

    // debugging/testing stuff
    // @Inject(method = "upkeep", at = @At("HEAD"), remap = false)
    // private void injectUpkeep(CallbackInfo ci) {
    //     var self = (BattleActor)(Object)this;
    //     ModCommon.LOG.info("UPKEEP: " + self.getName().getString() + ", request: " + self.getRequest());

    //     var battleState = RCTApi.getInstances()
    //         .map(e -> e.getValue().getBattleManager()
    //         .getState(self.battle.getBattleId()))
    //         .filter(bs -> bs != null)
    //         .findFirst().orElse(null);
        
    //     if(battleState != null) {
    //         var bs = BattleStates.get(battleState.getBattle());
    //         bs.getActorState(self).nextRequest();
    //     }
    // }

    // // debugging/testing stuff
    // @Inject(method = "writeShowdownResponse", at = @At("HEAD"), remap = false)
    // private void injectWriteShowdownResponse(CallbackInfo ci) {
    //     var self = (BattleActor)(Object)this;
    //     ModCommon.LOG.info("RESPONSE: " + self.getName().getString());
    // }

    // This is the only place I could figure to prevent the usage of items for any
    // battle actors. By the looks of it it shouldn't have any other than the desired
    // effect (especially since this injection only is effective in trainer battles
    // started by this api due to the check for a known battle state).
    @Inject(method = "canFitForcedAction", at = @At("RETURN"), cancellable = true, remap = false)
    private void injectCanFitForcedAction(CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            var self = (BattleActor)(Object)this;
            var battleState = RCTApi.getInstances()
                .map(e -> e.getValue().getBattleManager()
                .getState(self.battle.getBattleId()))
                .filter(bs -> bs != null)
                .findFirst().orElse(null);
            
            if(battleState != null) {
                var maxItems = battleState.getRules().getMaxItemUses();
                
                if(maxItems >= 0) {
                    var actorState = battleState.getState(self.getUuid());
                    cir.setReturnValue(actorState.getItemsUsed() < maxItems);
                }
            }
        }
    }
}
