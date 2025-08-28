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
import com.cobblemon.mod.common.client.net.battle.BattleMakeChoiceHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.gitlab.srcmc.rctapi.client.ModClient;

import net.minecraft.client.Minecraft;

@Mixin(BattleMakeChoiceHandler.class)
public abstract class BattleMakeChoiceHandlerMixin {
    /**
     * End of turn faint softlock 'fix'.
     * 
     * @see {@link BattleFaintHandlerMixin#injectHandle}
     * @see {@link BattleGUIMixin#injectSelectAction}
     */
    @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectHandle(BattleMakeChoicePacket packet, Minecraft client, CallbackInfo ci) {
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(battle != null) {
            // requests are ignored in certain circumstances
            if(battle.getFirstUnansweredRequest() == null || (battle.getMustChoose() && battle.getFirstUnansweredRequest().getForceSwitch())) {
                ModClient.BATTLE_STATE.unlock();
            }
        }
    }
}
