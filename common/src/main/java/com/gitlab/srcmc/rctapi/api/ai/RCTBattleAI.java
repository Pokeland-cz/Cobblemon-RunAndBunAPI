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
package com.gitlab.srcmc.rctapi.api.ai;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.interactive.PotionType;
import com.gitlab.srcmc.rctapi.api.ai.config.RCTBattleAIConfig;
import com.gitlab.srcmc.rctapi.api.ai.utils.PokeMath;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder.Choice;
import java.util.Random;

import org.jetbrains.annotations.NotNull;

public class RCTBattleAI implements BattleAI {
    private double moveBias;
    private double statusMoveBias;
    private double switchBias;
    private double itemBias;
    private double maxSelectMargin;
    private Random rng = new Random();

    public RCTBattleAI() {
        this(new RCTBattleAIConfig.Builder().build());
    }

    public RCTBattleAI(@NotNull RCTBattleAIConfig config) {
        this.moveBias = config.moveBias();
        this.statusMoveBias = config.statusMoveBias();
        this.switchBias = config.switchBias();
        this.itemBias = config.itemBias();
        this.maxSelectMargin = config.maxSelectMargin();
    }

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

        return builder.response();
    }

    private double evalMove(BattlePokemon from, BattlePokemon to, InBattleMove move) {        
        if(to == null) {
            return from.getActor().getSide().getOppositeSide()
                .getActivePokemon().stream()
                .filter(ActiveBattlePokemon::hasPokemon)
                .map(pkmn -> evalMove(from, pkmn.getBattlePokemon(), move))
                .max(Double::compare).orElse(0.0);
        }

        var hasStatus = to.getContextManager().get(BattleContext.Type.STATUS) != null;
        var estDamage = PokeMath.isStatus(move)
            ? !hasStatus ? to.getHealth() * this.statusMoveBias * (PokeMath.typeEffectiveness(move, to.getOriginalPokemon()) > 0 ? 1 : 0) : 0
            : Math.min(to.getHealth(), PokeMath.damage(from, to, move));
        var d = 1.0 - (to.getHealth() - estDamage) / to.getHealth();
        
        return (d < 1 ? d * to.getHealth() / (double)to.getMaxHealth() : d) * this.moveBias;
    }

    private double evalItem(BagItem item, BattlePokemon to) {
        if(item instanceof PotionType potion) {
            var amount = potion == PotionType.POTION ? Math.min(20, to.getMaxHealth())
                : potion == PotionType.SUPER_POTION ? Math.min(50, to.getMaxHealth())
                : potion == PotionType.HYPER_POTION ? Math.min(200, to.getMaxHealth())
                : potion == PotionType.MAX_POTION ? to.getMaxHealth()
                : potion == PotionType.FULL_RESTORE ? to.getMaxHealth() : 0;

            var hasStatus = to.getContextManager().get(BattleContext.Type.STATUS) != null;
            var estHeal = (Math.min(to.getMaxHealth(), to.getHealth() + amount) - to.getHealth()) / (double)amount;

            return (amount / (double)to.getMaxHealth()) * (1.0 - to.getHealth()/(double)to.getMaxHealth())*Math.min(1.0, estHeal * (to.isSentOut() ? 1 : 0.75) * (hasStatus && potion.getCuresStatus() ? 1.25 : 1)) * this.itemBias;
        }

        return 0;
    }

    private double evalSwitch(ActiveBattlePokemon from, BattlePokemon to) {
        // TODO: consider stat boosts/mali, status effects, etc.
        var fromHealthBias = 0.5 + 0.5*(1.0 - (from.hasPokemon() ? from.getBattlePokemon().getHealth()/from.getBattlePokemon().getMaxHealth() : 0));
        double[] d = {fromHealthBias * to.getHealth() / (double)to.getMaxHealth()};

        from.getSide().getOppositeSide().getActivePokemon().stream().filter(ActiveBattlePokemon::hasPokemon).forEach(pkmn -> {
            var atkTo = pkmn.getBattlePokemon().getOriginalPokemon().getAttack();
            var spaTo = pkmn.getBattlePokemon().getOriginalPokemon().getSpecialAttack();
            d[0] *= 1.0 - PokeMath.typeEffectiveness(pkmn.getBattlePokemon().getOriginalPokemon(), to.getOriginalPokemon()) / 4.0; // max type effectiveness is actually 8 but its pretty uncommon
            d[0] *= (atkTo > spaTo ? to.getOriginalPokemon().getDefence() / (double)atkTo : to.getOriginalPokemon().getSpecialDefence() / (double) spaTo) / 2;

            // TODO: opposite for current pokemon
            // var atkFrom = pkmn.getBattlePokemon().getOriginalPokemon().getAttack();
            // var spaFrom = pkmn.getBattlePokemon().getOriginalPokemon().getSpecialAttack();
            // d[0] *= PokeMath.typeEffectiveness(pkmn.getBattlePokemon().getOriginalPokemon(), to.getOriginalPokemon()) / 4.0;
            // d[0] *= Math.max(0, 1.0 - (atkFrom > spaFrom ? to.getOriginalPokemon().getDefence() / (double)atkFrom : to.getOriginalPokemon().getSpecialDefence() / (double) spaFrom) / 2);
        });

        return Math.min(1, d[0]) * this.switchBias;
    }
}
