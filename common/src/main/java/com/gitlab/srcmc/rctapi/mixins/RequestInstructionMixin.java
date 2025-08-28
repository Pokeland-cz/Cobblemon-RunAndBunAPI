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
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.ShowdownActionRequest;
import com.cobblemon.mod.common.battles.interpreter.instructions.RequestInstruction;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * For debugging/testing.
 */
@Mixin(RequestInstruction.class)
public abstract class RequestInstructionMixin {
    @Inject(method = "invoke", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
        var self = (RequestInstruction)(Object)this;
        ModCommon.LOG.info("REQU. INSTR: " + battle.getTurn() + ", " + self.getBattleActor().getName().getString());
        BattleStates.get(battle).getActorState(self.getBattleActor()).nextRequest();
    }

    // @Inject(method = "invoke", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
    //     var self = (RequestInstruction)(Object)this;
    //     ModCommon.LOG.info("REQU. INSTR: " + battle.getTurn() + ", " + self.getBattleActor().getName().getString());

    //     battle.log("Request Instruction");

    //     if(self.getMessage().getRawMessage().contains("teamPreview")) {
    //         ci.cancel();
    //         return;
    //     }

    //     // Parse Json message and update state info for actor
    //     var request = BattleRegistry.INSTANCE.getGson().fromJson(self.getMessage().getRawMessage().split("\\|request\\|")[1], ShowdownActionRequest.class);
    //     var delay = self.getBattleActor() instanceof AIBattleActor ? 0 : 3;
    //     request.sanitize(battle, self.getBattleActor());

    //     battle.dispatchGo(() -> {
    //         // This request won't be acted on until the start of next turn
    //         self.getBattleActor().sendUpdate(new BattleQueueRequestPacket(request));
    //         self.getBattleActor().setRequest(request);
    //         self.getBattleActor().getResponses().clear();
    //         var battleState = BattleStates.get(battle);

    //         // We need to send this out because 'upkeep' isn't received until the request is handled since the turn won't swap
    //         if(request.getForceSwitch().contains(true)) {
    //             ModCommon.LOG.info("FORCE SWITCH REQUEST: " + self.getBattleActor().getName().getString() + ", " + battleState.getForcedSwitches());
    //             battleState.incForcedSwitches();

    //             battle.doWhenClear(() -> {
    //                 self.getBattleActor().setMustChoose(true);
    //                 self.getBattleActor().sendUpdate(new BattleMakeChoicePacket());
    //                 ModCommon.LOG.info("CLEARED (FORCE SWITCH): " + self.getBattleActor().getName().getString() + ", " + battleState.getForcedSwitches());
    //                 battleState.decForcedSwitches();
    //                 return Unit.INSTANCE;
    //             });
    //         } else {
    //             ModCommon.LOG.info("CLEARED: " + self.getBattleActor().getName().getString());
    //         }

    //         return Unit.INSTANCE;
    //     });

    //     ci.cancel();
    // }
}
