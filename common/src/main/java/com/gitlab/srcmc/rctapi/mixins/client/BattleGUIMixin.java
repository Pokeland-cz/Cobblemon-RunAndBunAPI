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
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.client.ClientTasks;
import com.gitlab.srcmc.rctapi.client.ModClient;
import com.google.common.collect.Streams;

@Mixin(BattleGUI.class)
public abstract class BattleGUIMixin implements CobblemonRenderable {
    // max delay (should not be reached but just in case at least try to continue)
    private static final long SELECT_DELAY = 32000;

    @Shadow(remap = false)
    abstract void changeActionSelection(@Nullable BattleActionSelection arg0);

    /**
     * End of turn faint softlock 'fix'.
     * 
     * Triggered by pokemon fainting at the end of turn on both sides and the player
     * selecting a pokemon to switch in very quickly (tested with 'Perish Song').
     * 
     * @see {@link BattleFaintHandlerMixin#injectHandle}
     * @see {@link BattleMakeChoiceHandlerMixin#injectHandle}
     * @see {@link BattleQueueRequestPacketMixin#injectHandle}
     */
    @Inject(method = "selectAction", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectSelectAction(@NotNull SingleActionRequest request, @Nullable ShowdownActionResponse response, CallbackInfo ci) {
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(Stream.of(battle.getSides()).anyMatch(s -> s.getActors().stream().anyMatch(a -> a.getType().equals(ActorType.NPC)))) {
            if(battle != null && request.getResponse() == null) {
                request.setResponse(response);
                this.changeActionSelection(null);

                if(request.getForceSwitch() /*&& ModClient.BATTLE_STATE.getFainted() > 0*/) {
                    // delayed (waits for 'fainted' messages)
                    ClientTasks.BATTLE_SELECTIONS.runIf(
                        () -> battle.checkForFinishedChoosing(),
                        // () -> !ModClient.BATTLE_STATE.getForceSwitch() || Streams.stream(ModClient.BATTLE_STATE.getMessages()).filter(m -> m.contains("cobblemon.battle.fainted")).count() >= ModClient.BATTLE_STATE.getFainted(),
                        () -> {
                            ModCommon.LOG.info(":: CHECK:");
                            Stream.of(battle.getSides()).forEach(s -> s.getActiveClientBattlePokemon().forEach(p -> ModCommon.LOG.info(String.format(
                                ":::: %s, has: %b, pkmn: %s, fainted: %d, messages: %s, first: %s, last: %s, force: %b, open: %b",
                                p.getActor().getDisplayName().getString(),
                                p.hasPokemon(),
                                p.hasPokemon() ? p.getBattlePokemon().getDisplayName().getString() : "<null>",
                                ModClient.BATTLE_STATE.getFainted(),
                                Streams.stream(ModClient.BATTLE_STATE.getMessages()).filter(m -> m.contains("cobblemon.battle.fainted")).count(),
                                battle.getFirstUnansweredRequest(),
                                battle.getLastAnsweredRequest(),
                                ModClient.BATTLE_STATE.getForceSwitch(),
                                ModClient.BATTLE_STATE.getDispatchesComplete()
                                // !ModClient.BATTLE_STATE.getForceSwitch() || Streams.stream(ModClient.BATTLE_STATE.getMessages()).filter(m -> m.contains("cobblemon.battle.fainted")).count() >= ModClient.BATTLE_STATE.getFainted()
                                // !ModClient.BATTLE_STATE.getForceSwitch() || Streams.stream(ModClient.BATTLE_STATE.getMessages()).anyMatch(m -> m.contains("cobblemon.battle.turn"))
                            ))));

                            return !ModClient.BATTLE_STATE.getForceSwitch() || ModClient.BATTLE_STATE.getDispatchesComplete();
                            // return !ModClient.BATTLE_STATE.getForceSwitch() || Streams.stream(ModClient.BATTLE_STATE.getMessages()).anyMatch(m -> m.contains("cobblemon.battle.turn"));
                        },
                        // max delay (shouldn't happen but just in case)
                        // SELECT_DELAY * ModClient.BATTLE_STATE.getFainted());
                        SELECT_DELAY);
                } else {
                    // immediately (normal)
                    battle.checkForFinishedChoosing();
                }
            }

            ci.cancel();
        }
    }
}
