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

import com.cobblemon.mod.common.battles.ShowdownActionRequest;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleGeneralActionSelection;
import com.cobblemon.mod.common.client.net.battle.BattleMakeChoiceHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.gitlab.srcmc.rctapi.ModCommon;
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
    @Inject(method = "handle", at = @At("HEAD"), remap = false)
    private void injectHandle(BattleMakeChoicePacket packet, Minecraft client, CallbackInfo ci) {
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(battle != null) {
            ModCommon.LOG.info("++ RECEIVED BattleMakeChoicePacket: mustChoose=" + battle.getMustChoose()
                + ", first(has)=" + (battle.getFirstUnansweredRequest() != null)
                + ", first(forced)=" + (battle.getFirstUnansweredRequest() != null && battle.getFirstUnansweredRequest().getForceSwitch())
                + ", last(has)=" + (battle.getLastAnsweredRequest() != null)
                + ", last(forced)=" + (battle.getLastAnsweredRequest() != null && battle.getLastAnsweredRequest().getForceSwitch())
            );

            var request = battle.getFirstUnansweredRequest();

            if((battle.getMustChoose() && (request == null || request.getForceSwitch()))
            // TEST --+
            //        v
            || (!battle.getMustChoose() && (request == null || !request.getForceSwitch()))) {
                if(!ModClient.BATTLE_STATE.isOpen()) {
                    ModClient.BATTLE_STATE.unlock();
                    ModCommon.LOG.info("++++ UNLOCKED BATTLE_STATE");
                }
            }
        }

        // if(Minecraft.getInstance().screen instanceof BattleGUI battleGUI) {
        //     var current = battleGUI.getCurrentActionSelection();

        //     if(current == null && battle.getFirstUnansweredRequest() == null) {
        //         if(battle.getLastAnsweredRequest() != null) {
        //             var request = new SingleActionRequest[1];
        //             var count = new int[1];

        //             new Thread(() -> {
        //                 try {
        //                     do {
        //                         Thread.sleep(750);
        //                         Minecraft.getInstance().execute(() -> request[0] = battle.getFirstUnansweredRequest());
        //                         count[0]++;
        //                     } while(request[0] == null);
        //                 } catch(InterruptedException e) {
        //                     ModCommon.LOG.error(e.getLocalizedMessage(), e);
        //                 }

        //                 Minecraft.getInstance().execute(() -> {
        //                     ModCommon.LOG.info("++++ GENERAL SELECTION (delayed) after" + (count[0] * 750) + "ms");
        //                     battleGUI.changeActionSelection(new BattleGeneralActionSelection(battleGUI, request[0]));
        //                 });
        //             }).start();
        //         }
        //     }
        // }
    }
}
