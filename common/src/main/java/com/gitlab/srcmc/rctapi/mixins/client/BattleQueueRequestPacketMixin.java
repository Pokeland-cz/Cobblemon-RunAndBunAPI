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
import com.cobblemon.mod.common.client.net.battle.BattleQueueRequestHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket;
import com.gitlab.srcmc.rctapi.client.ModClient;
import net.minecraft.client.Minecraft;

@Mixin(BattleQueueRequestHandler.class)
public abstract class BattleQueueRequestPacketMixin {
    /**
     * End of turn faint softlock 'fix'.
     * 
     * Triggered by pokemon fainting at the end of turn on both sides and the player
     * selecting a pokemon to switch in very quickly (tested with 'Perish Song').
     * 
     * @see {@link BattleGUIMixin#injectSelectAction}
     */
    @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectHandle(BattleQueueRequestPacket packet, Minecraft client, CallbackInfo ci) {
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(battle != null && Stream.of(battle.getSides()).anyMatch(s -> s.getActors().stream().anyMatch(a -> a.getType().equals(ActorType.NPC)))) {
            var player = Minecraft.getInstance().player;
            
            if(player != null) {
                var actor = battle.getSide1().getActors().stream().filter(a -> a.getUuid().equals(player.getUUID())).findFirst().orElse(null);
                
                if(actor != null) {
                    ModClient.BATTLE_STATE.setDispatchesComplete(false);

                    if(packet.getRequest().getForceSwitch().contains(true)) {
                        ModClient.BATTLE_STATE.setForceSwitch();
                    }
                }
            }
        }
    }
}
