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
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.ShowdownActionRequest;
import com.cobblemon.mod.common.battles.interpreter.instructions.RequestInstruction;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.battle.BattleState;

import kotlin.Unit;

@Mixin(RequestInstruction.class)
public abstract class RequestInstructionMixin {
    @Shadow(remap = false)
    abstract BattleActor getBattleActor();

    @Shadow(remap = false)
    abstract BattleMessage getMessage();

    /**
     * Sets 'actor.mustChoose' to false in afterDispatches (after forceSwitch) if 
     * actor request is null (also runs checkForInputDispatch in that case).
     */
    @Inject(method = "invoke", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
        if(BattleState.findFirst(battle) != null) {
            battle.log("Request Instruction");
            var message = this.getMessage();
            var actor = this.getBattleActor();

            if(message.getRawMessage().contains("teamPreview")) {
                ci.cancel();
                return;
            }

            if(this.getBattleActor() instanceof AIBattleActor) {
                BattleStates.get(battle).getActorState(actor).nextRequest();
            }

            // Parse Json message and update state info for actor
            var request = BattleRegistry.INSTANCE.getGson().fromJson(message.getRawMessage().split("\\|request\\|")[1], ShowdownActionRequest.class);
            request.sanitize(battle, actor);

            battle.dispatchGo(() -> {
                // This request won't be acted on until the start of next turn
                actor.sendUpdate(new BattleQueueRequestPacket(request));
                actor.setRequest(request);
                actor.getResponses().clear();

                // We need to send this out because 'upkeep' isn't received until the request is handled since the turn won't swap
                if(request.getForceSwitch().contains(true)) {
                    battle.doWhenClear(() -> {
                        actor.setMustChoose(actor.getRequest() != null);

                        if(actor.getMustChoose()) {
                            actor.sendUpdate(new BattleMakeChoicePacket());
                        } else {
                            battle.checkForInputDispatch();
                        }

                        return Unit.INSTANCE;
                    });
                }

                return Unit.INSTANCE;
            });

            ci.cancel();
        }
    }
}
