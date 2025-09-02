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
package com.gitlab.srcmc.rctapi.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.net.battle.BattleQueueRequestHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.client.ClientTasks;
import com.gitlab.srcmc.rctapi.client.ModClient;
import com.google.common.collect.Streams;

import net.minecraft.client.Minecraft;

@Mixin(BattleQueueRequestHandler.class)
public abstract class BattleQueueRequestPacketMixin {
    @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectHandle(BattleQueueRequestPacket packet, Minecraft client, CallbackInfo ci) {
        ModCommon.LOG.info("++ RECEIVED BattleQueueRequestPacket (QUEUED)");
        ClientTasks.BATTLE_SELECTIONS.run(() -> {
            ModCommon.LOG.info("++ RECEIVED BattleQueueRequestPacket (HANDLED)");
            var battle = CobblemonClient.INSTANCE.getBattle();
            var player = Minecraft.getInstance().player;

            if(battle != null && player != null) {
                var actor = battle.getSide1().getActors().stream().filter(a -> a.getUuid().equals(player.getUUID())).findFirst().orElse(null);
                
                if(actor != null) {
                    battle.setPendingActionRequests(SingleActionRequest.Companion.composeFrom(actor, packet.getRequest()));
                }
            }
        });

        ModClient.BATTLE_STATE.unlock();
        ci.cancel();
    }

    // @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectHandle(BattleQueueRequestPacket packet, Minecraft client, CallbackInfo ci) {
    //     ModCommon.LOG.info("++ RECEIVED BattleQueueRequestPacket");
    //     ModClient.BATTLE_STATE.unlock();

    //     // if(packet.getRequest().getForceSwitch().contains(true)) {
    //     //     ModCommon.LOG.info("++++ LOCKED BATTLE_STATE (force switch)");
    //     //     ModClient.BATTLE_STATE.lock(); // unlocked in BattleMakeChoiceHandler
    //     // }
    // }

    // @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectHandle(BattleQueueRequestPacket packet, Minecraft client, CallbackInfo ci) {
    //     var battle = CobblemonClient.INSTANCE.getBattle();
    //     var player = Minecraft.getInstance().player;

    //     if(battle != null && player != null) {
    //         if(battle.getFirstUnansweredRequest() == null || (battle.getMustChoose() && battle.getFirstUnansweredRequest().getForceSwitch())) {
    //             ModClient.BATTLE_STATE.unlock();
    //         }

    //         var actor = battle.getSide1().getActors().stream().filter(a -> a.getUuid().equals(player.getUUID())).findFirst().orElse(null);
    //         var request = new Object[]{battle.getFirstUnansweredRequest()};

    //         // delayed until all requests have been either answered or canceled
    //         ModCommon.LOG.info("++++ QUEUED REQUEST: first=" + battle.getFirstUnansweredRequest() + ", must=" + battle.getMustChoose());

    //         new Thread(() -> {
    //             int[] delayedCount = {0}; // for debugging

    //             try {
    //                 while(request[0] != null) {
    //                     Minecraft.getInstance().execute(() -> request[0] = battle.getFirstUnansweredRequest());
    //                     Thread.sleep(750);
    //                     delayedCount[0]++;
    //                 }
    //             } catch (InterruptedException e) {
    //                 ModCommon.LOG.error("interrupted delay of BattleQueueRequestHandler", e);
    //             }

    //             Minecraft.getInstance().execute(() -> {
    //                 if(delayedCount[0] > 0) {
    //                     ModCommon.LOG.info("++++ DELAYED BattleQueueRequestHandler by " + (delayedCount[0]*500) + "ms");
    //                 }

    //                 battle.setPendingActionRequests(SingleActionRequest.Companion.composeFrom(actor, packet.getRequest()));
    //             });
    //         }).start();
    //     }

    //     ci.cancel();
    // }
}
