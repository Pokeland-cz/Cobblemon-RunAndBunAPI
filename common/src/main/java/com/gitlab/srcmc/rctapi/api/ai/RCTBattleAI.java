/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2024, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctapi.api.ai;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.interactive.PotionType;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.utils.PokeMath;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder.Choice;
import java.util.Random;

public class RCTBattleAI implements BattleAI {
    private static final double STATUS_MOVE_BIAS = 0.1;
    private static final double MOVE_BIAS = 1.0;
    private static final double SWITCH_BIAS = 1.0;
    private static final double ITEM_BIAS = 1.0;

    private double maxSelectMargin = 0.15;
    private Random rng = new Random();

    @Override
    public ShowdownActionResponse choose(ActiveBattlePokemon pkmn, ShowdownMoveset moveset, boolean forceSwitch) {
        var builder = ResponseBuilder
            .create(pkmn, moveset, forceSwitch)
            .margin(this.rng.nextDouble(this.maxSelectMargin))
            .random(this.rng);

        builder.suggestMoves(candidates -> candidates
            .map(pair -> {
                if(pair.second instanceof ActiveBattlePokemon targetPkmn) {
                    return new Choice<>(pair, pair.second.isAllied(pkmn)
                        ? 1.0 + evalMove(pkmn.getBattlePokemon(), targetPkmn.getBattlePokemon(), pair.first)
                        : 1.0 - evalMove(pkmn.getBattlePokemon(), targetPkmn.getBattlePokemon(), pair.first));
                }

                // non or multi-target move
                return new Choice<>(pair, 1.0 - evalMove(pkmn.getBattlePokemon(), null, pair.first));
            }));
        
        builder.suggestItems(candidates -> candidates
            .map(pair -> new Choice<>(pair, 1.0 - evalItem(pair.first, pair.second))));

        builder.suggestSwitches(candidates -> candidates
            .map(bp -> new Choice<>(bp, 1.0 - evalSwitch(pkmn, bp))));

        return builder.response(r -> ModCommon.LOG.info("RESPONSE: " + r.toShowdownString(pkmn, moveset)));
    }

    private static double evalMove(BattlePokemon from, BattlePokemon to, InBattleMove move) {        
        if(to == null) {
            return from.getActor().getSide().getOppositeSide()
                .getActivePokemon().stream()
                .filter(ActiveBattlePokemon::hasPokemon)
                .map(pkmn -> evalMove(from, pkmn.getBattlePokemon(), move))
                .max(Double::compare).orElse(0.0);
        }

        var hasStatus = to.getContextManager().get(BattleContext.Type.STATUS) != null;
        var estDamage = PokeMath.isStatus(move)
            ? !hasStatus ? to.getHealth() * STATUS_MOVE_BIAS * (PokeMath.typeEffectiveness(move, to.getOriginalPokemon()) > 0 ? 1 : 0) : 0
            : Math.min(to.getHealth(), PokeMath.damage(from, to, move));
        var d = 1.0 - (to.getHealth() - estDamage) / to.getHealth();
        ModCommon.LOG.info(String.format("EVAL MOVE: from=%s, to=%s, move=%s/%s, estd: %.2f, d: %.2f", from.getName().getString(), to.getName().getString(), move.id, move.move, estDamage, (d < 1 ? d * to.getHealth() / (double)to.getMaxHealth() : d) * MOVE_BIAS));
        return (d < 1 ? d * to.getHealth() / (double)to.getMaxHealth() : d) * MOVE_BIAS;
    }

    private static double evalItem(BagItem item, BattlePokemon to) {
        if(item instanceof PotionType potion) {
            var amount = potion == PotionType.POTION ? Math.min(20, to.getMaxHealth())
                : potion == PotionType.SUPER_POTION ? Math.min(50, to.getMaxHealth())
                : potion == PotionType.HYPER_POTION ? Math.min(200, to.getMaxHealth())
                : potion == PotionType.MAX_POTION ? to.getMaxHealth()
                : potion == PotionType.FULL_RESTORE ? to.getMaxHealth() : 0;

            var hasStatus = to.getContextManager().get(BattleContext.Type.STATUS) != null;
            var estHeal = (Math.min(to.getMaxHealth(), to.getHealth() + amount) - to.getHealth()) / (double)amount;
            ModCommon.LOG.info(String.format("EVAL ITEM: item=%s, to=%s, esth: %.2f, d: %.2f", item.getItemName(), to.getName().getString(), estHeal, Math.min(1.0, estHeal * (to.isSentOut() ? 1 : 0.75) * (hasStatus && potion.getCuresStatus() ? 1.25 : 1)) * ITEM_BIAS));
            return Math.min(1.0, estHeal * (to.isSentOut() ? 1 : 0.75) * (hasStatus && potion.getCuresStatus() ? 1.25 : 1)) * ITEM_BIAS;
        }

        return 0;
    }

    private static double evalSwitch(ActiveBattlePokemon from, BattlePokemon to) {
        double[] d = {to.getHealth() / (double)to.getMaxHealth()};

        from.getSide().getOppositeSide().getActivePokemon().stream().filter(ActiveBattlePokemon::hasPokemon).forEach(pkmn -> {
            d[0] *= 1.0 - PokeMath.typeEffectiveness(pkmn.getBattlePokemon().getOriginalPokemon(), to.getOriginalPokemon()) / 4.0; // max type effectiveness is actually 8 but its pretty uncommon
            var atk = pkmn.getBattlePokemon().getOriginalPokemon().getAttack();
            var spa = pkmn.getBattlePokemon().getOriginalPokemon().getSpecialAttack();
            d[0] *= atk > spa ? to.getOriginalPokemon().getDefence() / (double)atk : to.getOriginalPokemon().getSpecialDefence() / (double) spa;
        });

        ModCommon.LOG.info(String.format("EVAL SWITCH: from=%s, to=%s, d: %.2f", from.getBattlePokemon() != null ? from.getBattlePokemon().getName().getString() : "dead", to.getName().getString(), Math.min(1, d[0]) * SWITCH_BIAS));
        return Math.min(1, d[0]) * SWITCH_BIAS;
    }
}
