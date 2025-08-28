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

import java.util.Collections;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.net.NetworkPacket;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwapPokemonPacket;
import com.gitlab.srcmc.rctapi.ModCommon;

/**
 * @see also {@link BattleSwapPokemonHandleMixin}
 */
@Mixin(PokemonBattle.class)
public abstract class PokemonBattleMixin {
    // private int dispatchesCount, afterDispatchesCount;

    @Inject(method = "turn", at = @At("HEAD"), remap = false)
    private void injectTurn(int n, CallbackInfo ci) {
        ModCommon.LOG.info("================ TURN: " + n);
    }

    @Inject(method = "end", at = @At("HEAD"), remap = false)
    private void injectEnd(CallbackInfo ci) {
        ModCommon.LOG.info("================ BATTLE END ================");
        var self = (PokemonBattle)(Object)this;
        self.saveBattleLog();
    }

    // @Inject(method = "tick", at = @At("HEAD"), remap = false)
    // private void injectTick(CallbackInfo ci) {
    //     var self = (PokemonBattle)(Object)this;
        
    //     if(this.dispatchesCount != self.getDispatches().size() || this.afterDispatchesCount != self.getAfterDispatches().size()) {
    //         this.dispatchesCount = self.getDispatches().size();
    //         this.afterDispatchesCount = self.getAfterDispatches().size();
    //         ModCommon.LOG.info("BATTLE: Dispatches: " + this.dispatchesCount + ", afterDispatches: " + this.afterDispatchesCount);
    //     }
    // }

    /**
     * Swaps pokemon in pokemonList of actors whenever they swap positions on
     * field (e.g. by 'Ally Switch'). Fixes issues with follow up switch responses.
     */
    @Inject(method = "sendUpdate", at = @At("HEAD"), remap = false)
    private void injectSendUpdate(NetworkPacket<?> packet, CallbackInfo ci) {
        var self = (PokemonBattle)(Object)this;

        if(packet instanceof BattleSwapPokemonPacket swPacket) {
            if(self.getFormat().component2().getPokemonPerSide() < 3) {
                var pnxB = swPacket.getPnx(); // positions have already been swapped (so this actually refers to the target)
                var actorAndSlotB = self.getActorAndActiveSlotFromPNX(pnxB);
                
                var actor = actorAndSlotB.component1();
                var pkmnB = actorAndSlotB.component2();
                var pkmnA = (ActiveBattlePokemon)pkmnB.getAdjacentAllies().stream().findFirst().get();

                ModCommon.LOG.info(String.format("==> SWAP FIX: pnx: %s, %s <=> %s", pnxB, pkmnA.getBattlePokemon().getName().getString(), pkmnB.getBattlePokemon().getName().getString()));

                var actorPkmn = actor.getPokemonList();
                Collections.swap(actorPkmn, actorPkmn.indexOf(pkmnA.getBattlePokemon()), actorPkmn.indexOf(pkmnB.getBattlePokemon()));
            } else {
                // Ally switch in triple battles kinda messes things up.
                // Applying this 'fix' here does not help the cause.
                ModCommon.LOG.info("==> SWAP FIX SKIPPED (too many pokemon per side)");
            }
        }
    }
}
