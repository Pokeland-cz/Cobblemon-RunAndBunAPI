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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.dispatch.DispatchResult;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket;
import com.cobblemon.mod.common.net.serverhandling.battle.BattleSelectActionsHandler;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket;

/**
 * For debugging/testing.
 */
@Mixin(BattleSelectActionsHandler.class)
public abstract class BattleSelectActionsHandlerMixin {
    // private Queue<Thread> threadQueue = new LinkedList<>();
    
    // @Overwrite
    // public void handle(BattleSelectActionsPacket packet, MinecraftServer server, ServerPlayer player) {
    //     ModCommon.LOG.info("HANDLE SELECTION: " + player.getName().getString() + ", " + packet.getShowdownActionResponses().size());
    //     var battle = BattleRegistry.INSTANCE.getBattle(packet.getBattleId());

    //     if(battle == null) {
    //         return;
    //     }
        
    //     var actors = battle.getActors().iterator();
    //     BattleActor actor = null;

    //     while(actors.hasNext() && actor == null) {
    //         var nextActor = actors.next();
    //         var players = nextActor.getPlayerUUIDs().iterator();

    //         while(players.hasNext()) {
    //             if(players.next().equals(player.getUUID())) {
    //                 actor = nextActor;
    //                 break;
    //             }
    //         }
    //     }

    //     if(actor == null) {
    //         ModCommon.LOG.info(" -> SELECTION CANCELED: no actor");
    //         return;
    //     }

    //     if(!actor.getMustChoose()) {
    //         ModCommon.LOG.info(" -> SELECTION CANCELED: mustChoose == false");
    //         return;
    //     }
        
    //     var responses = packet.getShowdownActionResponses();
    //     var actorF = actor;
            
    //     var t = new Thread(() -> {
    //         try {
    //             while(/*battle.getDispatches().size() > 0 || */battle.getAfterDispatches().size() > 0) {
    //                 Thread.sleep(200);
    //             }
    //         } catch(InterruptedException e) {}


    //         server.execute(() -> {
    //             try {
    //                 ModCommon.LOG.info(" -> SELECTION RESPONSE... " + packet.type().id().toString() + responses.stream().map(r -> r.getType().name()).reduce("", (a, b) -> a + " " + b));
    //                 actorF.setActionResponses(responses);
    //                 ModCommon.LOG.info(" -> SELECTION SUCCESS!");
    //             } catch (IllegalActionChoiceException e) {
    //                 ModCommon.LOG.info(" -> SELECTION FAILURE: " + e.getMessage());
    //                 player.sendSystemMessage(Component.literal(e.getMessage()));
    //                 actorF.sendUpdate(new BattleQueueRequestPacket(actorF.getRequest()));
    //                 actorF.sendUpdate(new BattleMakeChoicePacket());
    //             }
    //         });

    //         synchronized(this.threadQueue) {
    //             this.threadQueue.poll();

    //             if(!this.threadQueue.isEmpty()) {
    //                 this.threadQueue.peek().start();
    //             }
    //         }
    //     });

    //     synchronized(this.threadQueue) {
    //         this.threadQueue.offer(t);

    //         if(this.threadQueue.size() == 1) {
    //             t.start();
    //         }
    //     }
    // }

    // @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectHandle(BattleSelectActionsPacket packet, MinecraftServer server, ServerPlayer player, CallbackInfo ci) {
    //     ModCommon.LOG.info("HANDLE SELECTION: " + player.getName().getString() + ", " + packet.getShowdownActionResponses().size());
    //     var battle = BattleRegistry.INSTANCE.getBattle(packet.getBattleId());

    //     if(battle == null) {
    //         ci.cancel();
    //         return;
    //     }

    //     new Thread(() -> {
    //         try {
    //             while(battle.getDispatches().size() > 0 || battle.getAfterDispatches().size() > 0) {
    //                 Thread.sleep(200);
    //             }
    //         } catch(InterruptedException e) {
    //         }

    //         server.execute(() -> {
    //             var actors = battle.getActors().iterator();
    //             BattleActor actor = null;

    //             while(actors.hasNext() && actor == null) {
    //                 var nextActor = actors.next();
    //                 var players = nextActor.getPlayerUUIDs().iterator();

    //                 while(players.hasNext()) {
    //                     if(players.next().equals(player.getUUID())) {
    //                         actor = nextActor;
    //                         break;
    //                     }
    //                 }
    //             }

    //             if(actor == null) {
    //                 ModCommon.LOG.info(" -> SELECTION CANCELED: no actor");
    //                 return;
    //             }

    //             if(!actor.getMustChoose()) {
    //                 ModCommon.LOG.info(" -> SELECTION CANCELED: mustChoose == false");
    //                 return;
    //             }

    //             var responses = packet.getShowdownActionResponses();
                
    //             try {
    //                 ModCommon.LOG.info(" -> SELECTION RESPONSE... " + packet.type().id().toString() + responses.stream().map(r -> r.getType().name()).reduce("", (a, b) -> a + " " + b));
    //                 actor.setActionResponses(responses);
    //                 ModCommon.LOG.info(" -> SELECTION SUCCESS!");
    //             } catch (IllegalActionChoiceException e) {
    //                 ModCommon.LOG.info(" -> SELECTION FAILURE: " + e.getMessage());
    //                 player.sendSystemMessage(Component.literal(e.getMessage()));
    //                 actor.sendUpdate(new BattleQueueRequestPacket(actor.getRequest()));
    //                 actor.sendUpdate(new BattleMakeChoicePacket());
    //             }
    //         });
    //     }).start();

    //     ci.cancel();
    // }

    // @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectHandle(BattleSelectActionsPacket packet, MinecraftServer server, ServerPlayer player, CallbackInfo ci) {
    //     ModCommon.LOG.info("HANDLE SELECTION: " + player.getName().getString() + ", " + packet.getShowdownActionResponses().size());
    //     var battle = BattleRegistry.INSTANCE.getBattle(packet.getBattleId());

    //     if(battle == null) {
    //         ci.cancel();
    //         return;
    //     }

    //     var actors = battle.getActors().iterator();
    //     BattleActor actor = null;

    //     while(actors.hasNext() && actor == null) {
    //         var nextActor = actors.next();
    //         var players = nextActor.getPlayerUUIDs().iterator();

    //         while(players.hasNext()) {
    //             if(players.next().equals(player.getUUID())) {
    //                 actor = nextActor;
    //                 break;
    //             }
    //         }
    //     }

    //     if(actor == null) {
    //         ModCommon.LOG.info(" -> SELECTION CANCELED: no actor");
    //         ci.cancel();
    //         return;
    //     }

    //     if(!actor.getMustChoose()) {
    //         ModCommon.LOG.info(" -> SELECTION CANCELED: mustChoose == false");

    //         // TEST (seems to help bypass the perish song issue but is just a hack)
    //         // actor.sendUpdate(new BattleQueueRequestPacket(actor.getRequest()));
    //         // actor.sendUpdate(new BattleMakeChoicePacket());
    //         ///////
            
    //         ci.cancel();
    //         return;
    //     }

    //     var actorFin = actor;

    //     // The original code executes this immediately. This works in most cases but I ran into
    //     // issues with very specific scenarios (like both sides pokemon fanting from perish song).
    //     // This seems to do the trick (along other adjustments in {@link PokemonBattleMixin}).

    //     // Kinda works (actor sends out pokemon, but gets stuck on next choice request instead)
    //     // battle.doWhenClear(() -> {
    //     //     try {
    //     //         ModCommon.LOG.info(" -> SELECTION RESPONSE... " + packet.type().id().toString() + responses.stream().map(r -> r.getType().name()).reduce("", (a, b) -> a + " " + b));
    //     //         actorFin.setActionResponses(responses);
    //     //         ModCommon.LOG.info(" -> SELECTION SUCCESS!");
    //     //     } catch (IllegalActionChoiceException e) {
    //     //         ModCommon.LOG.info(" -> SELECTION FAILURE: " + e.getMessage());
    //     //         player.sendSystemMessage(Component.literal(e.getMessage()));
    //     //         actorFin.sendUpdate(new BattleQueueRequestPacket(request));
    //     //         actorFin.sendUpdate(new BattleMakeChoicePacket());
    //     //     }

    //     //     return Unit.INSTANCE;
    //     // });

    //     // workaround
    //     new Thread(() -> {
    //         var request = actorFin.getRequest();
    //         var switchOpponents = Stream.of(actorFin.getSide().getOppositeSide().getActors())
    //             .filter(a -> a instanceof AIBattleActor)
    //             .filter(a -> a.getRequest().getForceSwitch().contains(true))
    //             .toList();

    //         if(request.getForceSwitch().contains(true) && switchOpponents.size() > 0) {
    //             try {
    //                 while(battle.getDispatches().size() > 0 || battle.getAfterDispatches().size() > 0) {
    //                     Thread.sleep(500);
    //                 }
    //             } catch(InterruptedException e) {
    //             }
    //         }

    //         server.execute(() -> {
    //             var responses = packet.getShowdownActionResponses();
                
    //             try {
    //                 ModCommon.LOG.info(" -> SELECTION RESPONSE... " + packet.type().id().toString() + responses.stream().map(r -> r.getType().name()).reduce("", (a, b) -> a + " " + b));
    //                 actorFin.setActionResponses(responses);
    //                 ModCommon.LOG.info(" -> SELECTION SUCCESS!");
    //             } catch (IllegalActionChoiceException e) {
    //                 ModCommon.LOG.info(" -> SELECTION FAILURE: " + e.getMessage());
    //                 player.sendSystemMessage(Component.literal(e.getMessage()));
    //                 actorFin.sendUpdate(new BattleQueueRequestPacket(request));
    //                 actorFin.sendUpdate(new BattleMakeChoicePacket());
    //             }
    //         });
    //     }).start();

    //     // older approach
    //     // var actorFin = actor;
    //     // var delay = 0;

    //     // BattleStates.get(battle).execAfterForceSwitch(server, () -> {
    //     //     try {
    //     //         ModCommon.LOG.info(" -> RESPONDING..." + packet.type().id().toString() + packet.getShowdownActionResponses().stream().map(r -> r.getType().name()).reduce("", (a, b) -> a + " " + b));
    //     //         actorFin.setActionResponses(packet.getShowdownActionResponses());
    //     //         ModCommon.LOG.info(" -> SUCCESS!");
    //     //     } catch (IllegalActionChoiceException e) {
    //     //         ModCommon.LOG.info(" -> FAILURE: " + e.getMessage());
    //     //         player.sendSystemMessage(Component.literal(e.getMessage()));
    //     //         actorFin.sendUpdate(new BattleQueueRequestPacket(actorFin.getRequest()));
    //     //         actorFin.sendUpdate(new BattleMakeChoicePacket());
    //     //     }
    //     // }, delay);

    //     ci.cancel();
    // }
}