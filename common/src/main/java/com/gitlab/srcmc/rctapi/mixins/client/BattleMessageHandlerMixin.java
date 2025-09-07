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

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.net.battle.BattleMessageHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket;
import com.gitlab.srcmc.rctapi.client.ModClient;

import net.minecraft.client.Minecraft;

@Mixin(BattleMessageHandler.class)
public abstract class BattleMessageHandlerMixin {
    /**
     * End of turn faint softlock 'fix'.
     * 
     * Triggered by pokemon fainting at the end of turn on both sides and the player
     * selecting a pokemon to switch in very quickly (tested with 'Perish Song').
     * 
     * @see {@link BattleGUIMixin#injectSelectAction}
     * @see {@link BattleFaintHandlerMixin#injectHandle}
     * @see {@link BattleQueueRequestHandlerMixin#injectHandle}
     */
    @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectHandle(BattleMessagePacket packet, Minecraft client, CallbackInfo ci) {
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(battle != null && Stream.of(battle.getSides()).anyMatch(s -> s.getActors().stream().anyMatch(a -> a.getType().equals(ActorType.NPC)))) {
            if(packet.getMessages().stream().anyMatch(m -> m.toString().contains("cobblemon.battle.turn"))) {
                ModClient.BATTLE_STATE.reset();
            }
        }
    }
}
