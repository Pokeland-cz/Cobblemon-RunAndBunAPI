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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.SingleActionRequest;
import com.cobblemon.mod.common.client.gui.CobblemonRenderable;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import com.gitlab.srcmc.rctapi.client.ClientTasks;
import com.gitlab.srcmc.rctapi.client.ModClient;

@Mixin(BattleGUI.class)
public abstract class BattleGUIMixin implements CobblemonRenderable {
    private static final long MAX_SELECT_DELAY = 32000;

    @Shadow(remap = false)
    abstract void changeActionSelection(@Nullable BattleActionSelection arg0);

    /**
     * End of turn faint softlock 'fix'.
     * 
     * Triggered by pokemon fainting at the end of turn on both sides and the player
     * selecting a pokemon to switch in very quickly (tested with 'Perish Song').
     * 
     * @see {@link BattleQueueRequestPacketMixin#injectHandle}
     */
    @Inject(method = "selectAction", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectSelectAction(@NotNull SingleActionRequest request, @Nullable ShowdownActionResponse response, CallbackInfo ci) {
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(Stream.of(battle.getSides()).anyMatch(s -> s.getActors().stream().anyMatch(a -> a.getType().equals(ActorType.NPC)))) {
            if(battle != null && request.getResponse() == null) {
                request.setResponse(response);
                this.changeActionSelection(null);

                if(request.getForceSwitch()) {
                    // delayed (waits for 'DispatchesCompleted')
                    ClientTasks.BATTLE_SELECTIONS.runIf(
                        () -> battle.checkForFinishedChoosing(),
                        () -> !ModClient.BATTLE_STATE.getForceSwitch() || ModClient.BATTLE_STATE.getDispatchesComplete(),
                        // max delay (should not be reached but just in case try to continue anyways)
                        MAX_SELECT_DELAY);
                } else {
                    // immediately (normal)
                    battle.checkForFinishedChoosing();
                }
            }

            ci.cancel();
        }
    }
}
