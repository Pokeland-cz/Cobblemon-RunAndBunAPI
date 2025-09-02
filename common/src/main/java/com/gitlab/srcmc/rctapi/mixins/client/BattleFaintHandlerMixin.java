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
import com.cobblemon.mod.common.client.net.battle.BattleFaintHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleFaintPacket;
import com.gitlab.srcmc.rctapi.client.ModClient;

import net.minecraft.client.Minecraft;

@Mixin(BattleFaintHandler.class)
public abstract class BattleFaintHandlerMixin {
    /**
     * End of turn faint softlock 'fix'.
     * 
     * Triggered by pokemon fainting at the end of turn on both sides and the player
     * selecting a pokemon to switch in very quickly (tested with 'Perish Song').
     * 
     * @see {@link BattleGUIMixin#injectSelectAction}
     * @see {@link BattleMakeChoiceHandlerMixin#injectHandle}
     * @see {@link BattleQueueRequestPacketMixin#injectHandle}
     */
    @Inject(method = "handle", at = @At("HEAD"), remap = false)
    private void injectHandle(BattleFaintPacket packet, Minecraft client, CallbackInfo ci) {
        // Observation/Idea: After a pokemon faints the player will receive a BattleMakeChoiceRequests.
        // Delay the SwitchResponse if pokemon fainted on both sides until it arrives.
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(battle != null && Stream.of(battle.getSides()).anyMatch(s -> s.getActors().stream().anyMatch(a -> a.getType().equals(ActorType.NPC)))) {
            ModClient.BATTLE_STATE.lock(battle.getPokemonFromPNX(packet.getPnx()).component1().getSide());
        }
    }
}
