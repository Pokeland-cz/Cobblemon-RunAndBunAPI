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

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.battles.interpreter.instructions.TurnInstruction;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.net.battle.BattleApplyPassResponseHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleApplyPassResponsePacket;
import com.gitlab.srcmc.rctapi.ModCommon;

import net.minecraft.client.Minecraft;

/**
 * For debugging/testing.
 */
@Mixin(BattleApplyPassResponseHandler.class)
public abstract class BattleApplyPassResponseHandlerMixin {
    @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectHandle(BattleApplyPassResponsePacket packet, Minecraft client, CallbackInfo ci) {
        ModCommon.LOG.info("HANDLE APPLY PASS RESPONSE: " + packet.type().id().toString() + ", " + packet.type().toString());
    }
    
    // @Inject(method = "handle", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectHandle(BattleApplyPassResponsePacket packet, Minecraft client, CallbackInfo ci) {
    //     ModCommon.LOG.info("HANDLE APPLY PASS RESPONSE: " + packet.type().id().toString() + ", " + packet.type().toString());

    //     var battle = CobblemonClient.INSTANCE.getBattle();

    //     if(battle == null) {
    //         ci.cancel();
    //         return;
    //     }

    //     SingleActionRequest req = battle.getPendingActionRequests().stream().filter(it -> it.getResponse() == null).findFirst().orElse(null);

    //     if(req == null) {
    //         ci.cancel();
    //         return;
    //     }

    //     var res = new ForcePassActionResponse();
    //     var gui = Minecraft.getInstance().screen;

    //     if (gui instanceof BattleGUI bgui) {
    //         ModCommon.LOG.info("SELECT ACTION");
    //         bgui.selectAction(req, res);
    //     } else {
    //         ModCommon.LOG.info("SET RESPONSE (CHECK FINISH)");
    //         req.setResponse(res);
    //         battle.checkForFinishedChoosing();
    //     }

    //     ci.cancel();
    // }
}
