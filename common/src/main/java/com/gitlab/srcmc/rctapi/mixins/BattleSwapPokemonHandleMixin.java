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

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.net.battle.BattleSwapPokemonHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwapPokemonPacket;
import com.gitlab.srcmc.rctapi.ModCommon;

import net.minecraft.client.Minecraft;

/**
 * @see also {@link PokemonBattleMixin}
 */
@Mixin(BattleSwapPokemonHandler.class)
public abstract class BattleSwapPokemonHandleMixin {
    // /**
    //  * Ally switch 'fix' (cient side).
    //  */
    // @Inject(method = "handle", at = @At("HEAD"), remap = false)
    // private void injectHandle(BattleSwapPokemonPacket packet, Minecraft client, CallbackInfo ci) {
    //     var battle = CobblemonClient.INSTANCE.getBattle();

    //     if(battle != null) {
    //         if(battle.getBattleFormat().component2().getPokemonPerSide() < 3) {
    //             var pnxA = packet.getPnx();
    //             var actorAndSlotA = battle.getPokemonFromPNX(pnxA);
                
    //             var actor = actorAndSlotA.component1();
    //             var pkmnA = actorAndSlotA.component2();
    //             var pkmnB = (ActiveClientBattlePokemon)pkmnA.getAdjacentAllies().stream().findFirst().get();

    //             ModCommon.LOG.info(String.format("==> SWAP FIX: pnx: %s, %s <=> %s", pnxA, pkmnA.getBattlePokemon().getDisplayName().getString(), pkmnB.getBattlePokemon().getDisplayName().getString()));

    //             var actorPkmn = actor.getPokemon();
    //             Collections.swap(actorPkmn, actorPkmn.indexOf(pkmnA.getBattlePokemon()), actorPkmn.indexOf(pkmnB.getBattlePokemon()));
    //         } else {
    //             // Ally switch in triple battles kinda messes things up.
    //             // Applying this 'fix' here does not help the cause.
    //             ModCommon.LOG.info("==> SWAP FIX SKIPPED (too many pokemon per side)");
    //         }
    //     }
    // }
}
