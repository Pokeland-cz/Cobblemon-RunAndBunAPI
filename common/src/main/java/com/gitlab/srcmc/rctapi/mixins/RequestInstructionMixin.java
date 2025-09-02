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
import com.gitlab.srcmc.rctapi.ModCommon;
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
     * Update {@link AIBattleActor} state for next request.
     */
    // @Inject(method = "invoke", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
    //     if(BattleState.findFirst(battle) != null) {
    //         battle.log("Request Instruction");
    //         var message = this.getMessage();
    //         var actor = this.getBattleActor();

    //         if(message.getRawMessage().contains("teamPreview")) {
    //             ci.cancel();
    //             return;
    //         }

    //         if(this.getBattleActor() instanceof AIBattleActor) {
    //             BattleStates.get(battle).getActorState(actor).nextRequest();
    //         }

    //         // Parse Json message and update state info for actor
    //         ModCommon.LOG.info(":: REQUEST INTSTRUCTION: " + actor.getName().getString());
    //         var request = BattleRegistry.INSTANCE.getGson().fromJson(message.getRawMessage().split("\\|request\\|")[1], ShowdownActionRequest.class);
    //         request.sanitize(battle, actor);

    //         battle.dispatchGo(() -> {
    //             // This request won't be acted on until the start of next turn
    //             actor.sendUpdate(new BattleQueueRequestPacket(request));
    //             actor.setRequest(request);
    //             actor.getResponses().clear();
    //             ModCommon.LOG.info(":::: QUEUED REQUEST PACKET, " + actor.getName().getString());

    //             // We need to send this out because 'upkeep' isn't received until the request is handled since the turn won't swap
    //             if(request.getForceSwitch().contains(true)) {
    //                 ModCommon.LOG.info(":::: FORCED SWITCH, " + actor.getName().getString() + " wait=" + request.getWait() + ", responses=" + actor.getResponses().size() + ", must=" + actor.getMustChoose());
    //                 var server = battle.getPlayers().getFirst().getServer();
    //                 var hasResponses = new boolean[]{actor.getMustChoose() ? !actor.getResponses().isEmpty() : true};
    //                 var count = new int[]{0};

    //                 // new Thread(() -> {
    //                 //     while(!hasResponses[0]) {
    //                 //         try {
    //                 //             Thread.sleep(500);
    //                 //             server.execute(() -> hasResponses[0] = !actor.getResponses().isEmpty());
    //                 //             count[0]++;
    //                 //         } catch(InterruptedException e) {
    //                 //             ModCommon.LOG.error(e.getLocalizedMessage(), e);
    //                 //         }
    //                 //     }

    //                 //     server.execute(() -> {
    //                         // if(actor instanceof AIBattleActor) {
    //                             actor.setMustChoose(true);
    //                             actor.sendUpdate(new BattleMakeChoicePacket());
    //                             ModCommon.LOG.info(":::: SEND CHOICE PACKET (NOW), " + actor.getName().getString() + ", responses: " + actor.getResponses().size() + (count[0] > 0 ? (", DELAYED: " + (count[0] * 500) + "ms") : ""));                                
    //                         // } else {
    //                         //     battle.doWhenClear(() -> {
    //                         //         actor.setMustChoose(true);
    //                         //         actor.sendUpdate(new BattleMakeChoicePacket());
    //                         //         ModCommon.LOG.info(":::: SEND CHOICE PACKET (DISPATCHED), " + actor.getName().getString() + ", responses: " + actor.getResponses().size() + (count[0] > 0 ? (", DELAYED: " + (count[0] * 500) + "ms") : ""));
    //                         //         return Unit.INSTANCE;
    //                         //     });
    //                         // }
    //                 //     });
    //                 // }).start();
    //             }

    //             return Unit.INSTANCE;
    //         });

    //         ci.cancel();
    //     }
    // }

    // @Inject(method = "invoke", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
    //     if(BattleState.findFirst(battle) != null) {
    //         battle.log("Request Instruction");
    //         var message = this.getMessage();
    //         var actor = this.getBattleActor();

    //         if(message.getRawMessage().contains("teamPreview")) {
    //             ci.cancel();
    //             return;
    //         }

    //         if(this.getBattleActor() instanceof AIBattleActor) {
    //             BattleStates.get(battle).getActorState(actor).nextRequest();
    //         }

    //         // Parse Json message and update state info for actor
    //         ModCommon.LOG.info(":: REQUEST INTSTRUCTION: " + actor.getName().getString());
    //         var request = BattleRegistry.INSTANCE.getGson().fromJson(message.getRawMessage().split("\\|request\\|")[1], ShowdownActionRequest.class);
    //         request.sanitize(battle, actor);

    //         battle.dispatchGo(() -> {
    //             // This request won't be acted on until the start of next turn
    //             actor.sendUpdate(new BattleQueueRequestPacket(request));
    //             actor.setRequest(request);
    //             actor.getResponses().clear();
    //             ModCommon.LOG.info(":::: QUEUED REQUEST PACKET, " + actor.getName().getString());

    //             // We need to send this out because 'upkeep' isn't received until the request is handled since the turn won't swap
    //             if(request.getForceSwitch().contains(true)) {
    //                 ModCommon.LOG.info(":::: FORCED SWITCH, " + actor.getName().getString());
    //                 battle.doWhenClear(() -> {
    //                     actor.setMustChoose(true);

    //                     if(!actor.getResponses().isEmpty()) {
    //                         actor.sendUpdate(new BattleQueueRequestPacket(request));
    //                     }

    //                     actor.sendUpdate(new BattleMakeChoicePacket());
    //                     ModCommon.LOG.info(":::: SEND CHOICE PACKET (DISPATCHED), " + actor.getName().getString() + ", responses: " + actor.getResponses().size());
    //                     return Unit.INSTANCE;
    //                 });
    //             }

    //             return Unit.INSTANCE;
    //         });

    //         ci.cancel();
    //     }
    // }

    // @Inject(method = "invoke", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
    //     if(BattleState.findFirst(battle) != null) {
    //         battle.log("Request Instruction");
    //         var message = this.getMessage();
    //         var actor = this.getBattleActor();

    //         if(message.getRawMessage().contains("teamPreview")) {
    //             ci.cancel();
    //             return;
    //         }

    //         if(this.getBattleActor() instanceof AIBattleActor) {
    //             BattleStates.get(battle).getActorState(actor).nextRequest();
    //         }

    //         // Parse Json message and update state info for actor
    //         ModCommon.LOG.info(":: REQUEST INTSTRUCTION: " + actor.getName().getString());
    //         var request = BattleRegistry.INSTANCE.getGson().fromJson(message.getRawMessage().split("\\|request\\|")[1], ShowdownActionRequest.class);
    //         request.sanitize(battle, actor);

    //         battle.dispatchGo(() -> {
    //             // This request won't be acted on until the start of next turn
    //             actor.sendUpdate(new BattleQueueRequestPacket(request));
    //             actor.setRequest(request);
    //             actor.getResponses().clear();
    //             ModCommon.LOG.info(":::: QUEUED REQUEST PACKET, " + actor.getName().getString());

    //             // We need to send this out because 'upkeep' isn't received until the request is handled since the turn won't swap
    //             if(request.getForceSwitch().contains(true)) {
    //                 ModCommon.LOG.info(":::: FORCED SWITCH, " + actor.getName().getString() + " wait=" + request.getWait() + ", responses=" + actor.getResponses().size() + ", must=" + actor.getMustChoose());
    //                 List<Function0<Unit>> handler = new ArrayList<>();

    //                 handler.add(() -> {
    //                     if(actor.getRequest() == null) {
    //                         ModCommon.LOG.info(":::: REQUEST WAS null (requeue)?!");
    //                         battle.dispatchGo(handler.get(0));
    //                     } else {
    //                         if(!request.equals(actor.getRequest())) {
    //                             ModCommon.LOG.info(":::: DIFFERENT REQUEST?!");
    //                         }

    //                         actor.setMustChoose(true);
    //                         actor.sendUpdate(new BattleMakeChoicePacket());
    //                         ModCommon.LOG.info(":::: SEND CHOICE PACKET (DISPATCHED), " + actor.getName().getString() + ", responses: " + actor.getResponses().size());
    //                     }

    //                     return Unit.INSTANCE;
    //                 });

    //                 battle.doWhenClear(handler.get(0));
    //             }

    //             return Unit.INSTANCE;
    //         });

    //         ci.cancel();
    //     }
    // }

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
            ModCommon.LOG.info(":: REQUEST INTSTRUCTION: " + actor.getName().getString());
            var request = BattleRegistry.INSTANCE.getGson().fromJson(message.getRawMessage().split("\\|request\\|")[1], ShowdownActionRequest.class);
            request.sanitize(battle, actor);

            battle.dispatchGo(() -> {
                // This request won't be acted on until the start of next turn
                actor.sendUpdate(new BattleQueueRequestPacket(request));
                actor.setRequest(request);
                actor.getResponses().clear();
                ModCommon.LOG.info(":::: QUEUED REQUEST PACKET, " + actor.getName().getString());

                // We need to send this out because 'upkeep' isn't received until the request is handled since the turn won't swap
                if(request.getForceSwitch().contains(true)) {
                    ModCommon.LOG.info(":::: FORCED SWITCH, " + actor.getName().getString() + " wait=" + request.getWait() + ", responses=" + actor.getResponses().size() + ", must=" + actor.getMustChoose());
                    battle.doWhenClear(() -> {
                        actor.setMustChoose(actor.getRequest() != null);

                        if(actor.getMustChoose()) {
                            actor.sendUpdate(new BattleMakeChoicePacket());
                            ModCommon.LOG.info(":::: SEND CHOICE PACKET (DISPATCHED), " + actor.getName().getString() + ", responses: " + actor.getResponses().size());
                        } else {
                            ModCommon.LOG.info(":::: REQUEST WAS null (REQUEST IGNORED)?!");
                            battle.checkForInputDispatch();
                        }

                        // actor.setMustChoose(true);
                        // if(actor.getRequest() == null) {
                        //     ModCommon.LOG.info(":::: REQUEST WAS null?!");
                        //     actor.setRequest(request); // gets set to null somewhere in multi battles?
                        // } else if(!request.equals(actor.getRequest())) {
                        //     ModCommon.LOG.info(":::: DIFFERENT REQUEST?!");
                        // }
                        // actor.sendUpdate(new BattleMakeChoicePacket());
                        // ModCommon.LOG.info(":::: SEND CHOICE PACKET (DISPATCHED), " + actor.getName().getString() + ", responses: " + actor.getResponses().size());
                        return Unit.INSTANCE;
                    });
                }

                return Unit.INSTANCE;
            });

            ci.cancel();
        }
    }
}
