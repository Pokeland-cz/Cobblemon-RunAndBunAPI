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

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.client.ModClient;

/**
 * This is the best I could come up with to circumvent issues (softlocks) related to pokemon
 * dying at the end of turn, after requests for that turn have already been resolved.
 * 
 * {@snippet :
 * Example:
 * - Have a team of at least 2 pokemon and fight a trainer with at least 2 pokemon
 * - Use "Perish Song"
 * - Once pokemon faint on the user side at the end of turn pick the next pokemon immediately
 *   -> The battle will softlock
 *   -> Observation: waiting a few seconds before making a choice prevents this from happening
 * }
 * 
 * @see {@link BattleGUIMixin}
 */
@Mixin(ClientBattle.class)
public abstract class ClientBattleMixin {
    // @Inject(method = "checkForFinishedChoosing", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectCheckForFinishedChoosing(CallbackInfo ci) {
    //     var self = (ClientBattle)(Object)this;
    //     ModCommon.LOG.info("FINISH CHOOSING: " + self.getFirstUnansweredRequest() + ", must: " + self.getMustChoose() + ", last: " + (self.getLastAnsweredRequest() != null ? self.getLastAnsweredRequest().getResponse().getType() : "none") + ", pending: " + self.getPendingActionRequests().size());

    //     if(self.getFirstUnansweredRequest() == null) {
    //         ModClient.execAfterDelay(() -> {
    //             ModCommon.LOG.info("SENDING RESPONSE");
    //             CobblemonNetwork.INSTANCE.sendToServer(
    //                 new BattleSelectActionsPacket(
    //                     self.getBattleId(),
    //                     self.getPendingActionRequests().stream().map(r -> r.getResponse()).toList()
    //                 )
    //             );

    //             self.setMustChoose(false);
    //         });
    //     }

    //     ci.cancel();
    // }
}
