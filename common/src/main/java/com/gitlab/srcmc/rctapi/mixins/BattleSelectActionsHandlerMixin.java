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

import java.awt.Color;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket;
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket;
import com.cobblemon.mod.common.net.serverhandling.battle.BattleSelectActionsHandler;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.google.common.collect.Streams;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

@Mixin(BattleSelectActionsHandler.class)
public abstract class BattleSelectActionsHandlerMixin {
    /**
     * If a user makes a (forced switch) choice very quickly at the of a turn it is
     * possible that it arrives before the upkeep of the next turn, in which case
     * mustChoose would still be false and the selection ignored => battle gets stuck.
     * 
     * For the sake of my sanity I simply chose to ignore the state of mustChoose here
     * (under the premise that an unexpected selection response would indicate a different
     * issue somewhere else anyway).
     */
    // @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectHandle(BattleSelectActionsPacket packet, MinecraftServer server, ServerPlayer player, CallbackInfo ci) {
    //     var battle = BattleRegistry.INSTANCE.getBattle(packet.getBattleId());

    //     if(battle == null) {
    //         ci.cancel();
    //         return;
    //     }
        
    //     BattleActor actor = Streams.stream(battle.getActors())
    //         .filter(a -> Streams.stream(a.getPlayerUUIDs()).anyMatch(uuid -> uuid.equals(player.getUUID())))
    //         .findFirst().orElse(null);

    //     if(actor == null) {
    //         ci.cancel();
    //         return;
    //     }

    //     if(!actor.getMustChoose()) {
    //         ci.cancel();
    //         return;
    //     }

    //     var request = actor.getRequest();
    //     Runnable handler = () -> {
    //         try {
    //             actor.setActionResponses(packet.getShowdownActionResponses());
    //         } catch(IllegalActionChoiceException e) {
    //             var message = e.getLocalizedMessage();
    //             player.sendSystemMessage(Component.literal(message != null ? message : "Illegal choice error: reason unknown").withStyle(ChatFormatting.RED));

    //             if(request == null) {
    //                 ModCommon.LOG.error(String.format("Missing request for BattleActor %s", actor.getName().getString()), e);
    //             } else {
    //                 actor.sendUpdate(new BattleQueueRequestPacket(request));
    //                 actor.sendUpdate(new BattleMakeChoicePacket());
    //             }
    //         }
    //     };

    //     if(request.getForceSwitch().contains(true) && actor.getActivePokemon().stream().noneMatch(p -> p.isAlive())) {
    //         ModCommon.LOG.info("::: QUEUED ACTOR SELECTION: " + actor.getName().getString() + ", responses: " + packet.getShowdownActionResponses().size() + ", active: " + actor.getActivePokemon().stream().filter(p -> p.isAlive()).count() + ", dispatches: " + battle.getDispatches().size() + ", " + battle.getAfterDispatches().size());
    //         BattleStates.get(battle).getActorState(actor).getPostUpkeepHandlers().offer(handler);
    //         // actor.setMustChoose(false);
    //     } else {
    //         handler.run();
    //     }

    //     ci.cancel();
    // }

    // reimplementation (for debugging)
    @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectHandle(BattleSelectActionsPacket packet, MinecraftServer server, ServerPlayer player, CallbackInfo ci) {
        var battle = BattleRegistry.INSTANCE.getBattle(packet.getBattleId());

        if(battle == null) {
            ci.cancel();
            return;
        }
        
        BattleActor actor = Streams.stream(battle.getActors())
            .filter(a -> Streams.stream(a.getPlayerUUIDs()).anyMatch(uuid -> uuid.equals(player.getUUID())))
            .findFirst().orElse(null);

        if(actor == null) {
            ci.cancel();
            return;
        }

        if(!actor.getMustChoose()) {
            ci.cancel();
            return;
        }

        var request = actor.getRequest();
        
        try {
            actor.setActionResponses(packet.getShowdownActionResponses());
        } catch(IllegalActionChoiceException e) {
            var message = e.getLocalizedMessage();
            player.sendSystemMessage(Component.literal(message != null ? message : "Illegal choice error: reason unknown").withStyle(ChatFormatting.RED));

            if(request == null) {
                ModCommon.LOG.error(String.format("Missing request for BattleActor %s", actor.getName().getString()), e);
            } else {
                actor.sendUpdate(new BattleQueueRequestPacket(request));
                actor.sendUpdate(new BattleMakeChoicePacket());
            }
        }

        ci.cancel();
    }
}
