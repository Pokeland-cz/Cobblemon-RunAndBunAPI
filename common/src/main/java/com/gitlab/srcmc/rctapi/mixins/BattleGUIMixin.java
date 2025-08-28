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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import com.gitlab.srcmc.rctapi.client.ClientTasks;
import com.gitlab.srcmc.rctapi.client.ModClient;

@Mixin(BattleGUI.class)
public abstract class BattleGUIMixin {
    private static final long SELECT_DELAY = 5000;

    @Shadow
    public abstract void changeActionSelection(@Nullable BattleActionSelection arg0);

    /**
     * End of turn faint softlock 'fix'.
     * 
     * @see {@link BattleFaintHandlerMixin#injectHandle}
     * @see {@link BattleMakeChoiceHandlerMixin#injectHandle}
     */
    @Inject(method = "selectAction", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectSelectAction(@NotNull SingleActionRequest request, @Nullable ShowdownActionResponse response, CallbackInfo ci) {
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(battle != null && request.getResponse() == null) {
            request.setResponse(response);
            this.changeActionSelection(null);

            // if(request.getForceSwitch() && (response instanceof SwitchActionResponse) && ModClient.BATTLE_STATE.removeFainted(request.getActivePokemon())) {
            if(request.getForceSwitch()) {
                ClientTasks.BATTLE_SELECTIONS.runIf(
                    battle::checkForFinishedChoosing,
                    ModClient.BATTLE_STATE::isReady,
                    SELECT_DELAY + SELECT_DELAY * battle.getBattleFormat().getBattleType().getPokemonPerSide());
            } else {
                ClientTasks.BATTLE_SELECTIONS.run(battle::checkForFinishedChoosing);
            }
        }

        ci.cancel();
    }

    // @Inject(method = "deriveRootActionSelection", at = @At("HEAD"), remap = false, cancellable = true)
    // private void injectDeriveRootActionSelection(ClientBattleActor actor, SingleActionRequest request, CallbackInfoReturnable<BattleActionSelection> cir) {
    //     // if(request.getForceSwitch()) { // TODO: this is only required if BOTH sides have to force switch
    //     //     ModClient.setDelay(DELAY);
    //     // }
    //     ModCommon.LOG.info("deriveRootActionSelection");
    // }

    // private long ticks;

    // // debugging/logging
    // @Inject(method = "render", at = @At("TAIL"), remap = false)
    // private void injectRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    //     var self = (BattleGUI)(Object)this;
    //     var battle = CobblemonClient.INSTANCE.getBattle();

    //     if(battle != null && self.getActor() != null) {
    //         if((ticks++) % 240 == 0) {
    //             ModCommon.LOG.info("GUI RENDER: " + battle.getMustChoose() + ", " + self.getCurrentActionSelection() + ", " + battle.getFirstUnansweredRequest() + ", " + self.getActor().getDisplayName().getString());
    //         }

    //         if(battle.getMustChoose()) {
    //             if(self.getCurrentActionSelection() == null) {
    //                 var unanswered = battle.getFirstUnansweredRequest();

    //                 if((ticks-1) % 240 == 0) {
    //                     ModCommon.LOG.info("UNANSWERED: " + unanswered);
    //                 }

    //                 if(unanswered != null) {
    //                     // self.changeActionSelection(self.deriveRootActionSelection(self.getActor(), unanswered));
    //                 }
    //             }
    //         } else if (self.getCurrentActionSelection() != null) {
    //             if((ticks-1) % 240 == 0) {
    //                 ModCommon.LOG.info("NO CHOOSE AND SELECTION");
    //             }

    //             // self.changeActionSelection(null);
    //         }
    //     }
    // }
}
