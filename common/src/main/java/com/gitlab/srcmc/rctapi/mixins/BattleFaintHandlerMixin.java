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

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.net.battle.BattleFaintHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleFaintPacket;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.client.ModClient;

import net.minecraft.client.Minecraft;

@Mixin(BattleFaintHandler.class)
public abstract class BattleFaintHandlerMixin {
    /**
     * End of turn faint softlock 'fix'.
     * 
     * @see {@link BattleGUIMixin#injectSelectAction}
     * @see {@link BattleMakeChoiceHandlerMixin#injectHandle}
     */
    @Inject(method = "handle", at = @At("HEAD"), remap = false)
    private void injectHandle(BattleFaintPacket packet, Minecraft client, CallbackInfo ci) {
        // Observation/Idea: After a pokemon faints the player will receive a
        // BattleMakeChoiceRequests. Delay the SwitchResponse until it arrived.
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(battle != null) {
            var actorAndPkmn = battle.getPokemonFromPNX(packet.getPnx());
            var player = Minecraft.getInstance().player;

            if(battle.getParticipatingActor(player.getUUID()) == actorAndPkmn.component1()) {
                // ModClient.BATTLE_STATE.addFainted(actorAndPkmn.component2());
                // ModClient.BATTLE_STATE.resetChoiceRequestCount();
                // ModClient.BATTLE_STATE.addExpectedRequests(battle.getBattleFormat().getBattleType().getPokemonPerSide());
                // ModCommon.LOG.info(String.format("FAINTED: %s, adding to expected: %d", actorAndPkmn.component2().getBattlePokemon().getDisplayName().getString(), ModClient.BATTLE_STATE.getExpectedRequests()));
                ModCommon.LOG.info(String.format("FAINTED: %s", actorAndPkmn.component2().getBattlePokemon().getDisplayName().getString()));
                ModClient.BATTLE_STATE.lock();
            }
        }
    }
}