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

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.interactive.PotionType;
import com.gitlab.srcmc.rctapi.api.ai.config.RCTBattleAIConfig;
import com.gitlab.srcmc.rctapi.api.ai.utils.MoveType;
import com.gitlab.srcmc.rctapi.api.ai.utils.PokeContext;
import com.gitlab.srcmc.rctapi.api.ai.utils.PokeMath;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder;
import com.gitlab.srcmc.rctapi.api.ai.utils.TypeChart;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder.Choice;
import java.util.Random;

import org.jetbrains.annotations.NotNull;

public class RCTBattleAI implements BattleAI {
    public static final boolean DEBUG = false;

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
        // TODO: REMOVE DEBUG
        if(RCTBattleAI.DEBUG) {
            if(pkmn.isAlive()) {
                PokeContext.dump(pkmn.getBattlePokemon());
                pkmn.getActor().getSide().getOppositeSide().getActivePokemon()
                    .stream().filter(p -> p.isAlive())
                    .map(p -> p.getBattlePokemon())
                    .forEach(PokeContext::dump);
            }
        }
        // // // // // // //

        var builder = ResponseBuilder
            .create(pkmn, moveset, forceSwitch)
            .margin(this.rng.nextDouble(this.maxSelectMargin))
            .random(this.rng);

        builder.suggestMoves(candidates -> candidates
            .filter(pair -> !(pair.second instanceof ActiveBattlePokemon targetPkmn) || targetPkmn.isAlive())
            .map(pair -> {
                if(pair.second instanceof ActiveBattlePokemon targetPkmn) {
                    return new Choice<>(String.format("MOVE %s -> %s", pair.first.move, targetPkmn.getBattlePokemon().getName().getString()), pair, 1.0 - evalMove(pkmn.getBattlePokemon(), targetPkmn.getBattlePokemon(), pair.first));
                }

                // non or multi-target move
                return new Choice<>(String.format("MOVE %s -> <multi/none>", pair.first.move), pair, 1.0 - evalMove(pkmn.getBattlePokemon(), null, pair.first));
            }));

        builder.suggestItems(candidates -> candidates
            .map(pair -> new Choice<>(String.format("ITEM %s -> %s", pair.first.getItemName(), pair.second.getName().getString()), pair, 1.0 - evalItem(pair.first, pair.second))));

        if(forceSwitch || !pkmn.hasPokemon() || !PokeContext.State.trapped(pkmn.getBattlePokemon())) {
            builder.suggestSwitches(candidates -> candidates
                .map(bp -> new Choice<>(String.format("SWITCH %s -> %s", pkmn.isAlive() ? pkmn.getBattlePokemon().getName().getString() : "<dead>", bp.getName().getString()), bp, 1.0 - evalSwitch(pkmn, bp))));
        }

        return builder.response();
    }

    private double evalMove(BattlePokemon from, BattlePokemon to, InBattleMove move) {        
        var mt = MoveType.of(move);

        if(to == null) {
            var all = (mt == MoveType.HEAL || mt == MoveType.CURE || mt == MoveType.BUFF)
                ? from.getActor().getSide().getActivePokemon()
                : from.getActor().getSide().getOppositeSide().getActivePokemon();

            return all.stream()
                .filter(ActiveBattlePokemon::hasPokemon)
                .map(pkmn -> evalMove(from, pkmn.getBattlePokemon(), move))
                .max(Double::compare).orElse(0.0);
        }

        var ally = from.actor.getSide().equals(to.actor.getSide());
        double e;

        switch(mt) {
            case HEAL:
                // TODO: check healblock
                e = ally ? Math.max(this.rngSin()*this.maxSelectMargin, 1.0 - to.getHealth()/(double)to.getMaxHealth())*this.statusMoveBias : 0;
                break;
            case CURE:
                e = ally ? (PokeContext.Statuses.any(to) ? this.rngSin() : this.rngSin()*this.maxSelectMargin)*this.statusMoveBias : 0;
                break;
            case BUFF:
                e = ally ? this.rngSin()*Math.min(1.0, 1.0 - PokeContext.Boosts.avg(to)*5)*this.statusMoveBias : 0;
                break;
            case MALUS:
                e = !ally ? this.rngSin()*Math.min(1.0, 1.0 + PokeContext.Boosts.avg(to)*5)*this.statusMoveBias : 0;
                break;
            case STATUS:
                e = !ally ? ((!PokeContext.Statuses.any(to) && !PokeContext.Volatiles.any(to)) ? this.rngSin() : this.rngSin()*this.maxSelectMargin)*this.statusMoveBias : 0;
                break;
            case DAMAGE:
                e = !ally ? Math.max(this.rngSin()*this.maxSelectMargin, Math.min(to.getHealth(), PokeMath.damage(from, to, move))/(double)to.getHealth())*this.moveBias : 0;
                break;
            default:
                e = this.rng.nextDouble();
                break;
        }

        return e == 0 ? -1.0 : MoveType.eval(from, to, move) * e;
    }

    private double evalItem(BagItem item, BattlePokemon to) {
        if(item instanceof PotionType potion) {
            var amount = potion == PotionType.POTION ? Math.min(20, to.getMaxHealth())
                : potion == PotionType.SUPER_POTION ? Math.min(50, to.getMaxHealth())
                : potion == PotionType.HYPER_POTION ? Math.min(200, to.getMaxHealth())
                : potion == PotionType.MAX_POTION ? to.getMaxHealth()
                : potion == PotionType.FULL_RESTORE ? to.getMaxHealth() : 0;

            var estHeal = (Math.min(to.getMaxHealth(), to.getHealth() + amount) - to.getHealth())/(double)amount;
            return Math.min(1.0, (amount/(double)to.getMaxHealth()) * (1.0 - to.getHealth()/(double)to.getMaxHealth())*Math.min(1.0, estHeal * (to.isSentOut() ? 1 : 0.75) * (PokeContext.Statuses.any(to) && potion.getCuresStatus() ? 1.25 : 1)) * this.itemBias);
        }

        return 0;
    }

    private double evalSwitch(ActiveBattlePokemon from, BattlePokemon to) {
        // stat boosts
        var fboost = 1.0 - 0.5 * (from.hasPokemon() ? PokeContext.Boosts.avg(from.getBattlePokemon()) : 0);

        // status effects
        var fStat = from.hasPokemon() && (PokeContext.Statuses.any(from.getBattlePokemon()) || PokeContext.Volatiles.any(from.getBattlePokemon())) ? 1.25 : 1.0;
        var tStat = PokeContext.Statuses.any(to) ? 0.75 : 1.0;

        // double[] d = { (thRel > 0 ? thRel < fhRel ? (1.0 + thRel - fhRel)/4.0 : thRel - fhRel : 0.0) * fStat * tStat * fboost};
        double[] d = { fStat * tStat * fboost};

        // (s)atk/(s)def and type effectiveness
        var atkFrom = from.hasPokemon() ? from.getBattlePokemon().getEffectedPokemon().getAttack() : 0;
        var spaFrom = from.hasPokemon() ? from.getBattlePokemon().getEffectedPokemon().getSpecialAttack() : 0;
        var defFrom = from.hasPokemon() ? from.getBattlePokemon().getEffectedPokemon().getAttack() : 0;
        var spdFrom = from.hasPokemon() ? from.getBattlePokemon().getEffectedPokemon().getSpecialAttack() : 0;
        var atkTo = to.getEffectedPokemon().getAttack();
        var spaTo = to.getEffectedPokemon().getSpecialAttack();
        var defTo = to.getEffectedPokemon().getAttack();
        var spdTo = to.getEffectedPokemon().getSpecialAttack();

        to.getActor().getSide().getOppositeSide().getActivePokemon().stream().filter(ActiveBattlePokemon::hasPokemon).forEach(pkmn -> {
            var atkOpp = pkmn.getBattlePokemon().getEffectedPokemon().getAttack();
            var spaOpp = pkmn.getBattlePokemon().getEffectedPokemon().getSpecialAttack();
            var defOpp = pkmn.getBattlePokemon().getEffectedPokemon().getAttack();
            var spdOpp = pkmn.getBattlePokemon().getEffectedPokemon().getSpecialAttack();
            
            var fe1 = from.hasPokemon() ? TypeChart.getEffectiveness(from.getBattlePokemon(), pkmn.getBattlePokemon()) : 0;
            var fe2 = from.hasPokemon() ? TypeChart.getEffectiveness(pkmn.getBattlePokemon(), from.getBattlePokemon()) : 4;
            var f1 = atkFrom > 0 ? defOpp/(double)atkFrom : 1;
            var f2 = spaFrom > 0 ? spdOpp/(double)spaFrom : 1;
            var f3 = defFrom > 0 ? atkOpp/(double)defFrom : 1;
            var f4 = spdFrom > 0 ? spaOpp/(double)spdFrom : 1;
            var f5 = fe1 > 0 ? 1/fe1 : 1;
            var f6 = fe2 > 0 ? fe2/4 : 1/2.0;

            var te1 = TypeChart.getEffectiveness(to, pkmn.getBattlePokemon());
            var te2 = TypeChart.getEffectiveness(pkmn.getBattlePokemon(), to);
            var t1 = defOpp > 0 ? atkTo/(double)defOpp : 2;
            var t2 = spdOpp > 0 ? spaTo/(double)spdOpp : 2;
            var t3 = atkOpp > 0 ? defTo/(double)atkOpp : 2;
            var t4 = spaOpp > 0 ? spdTo/(double)spaOpp : 2;
            var t5 = te1 > 0 ? te2/4 : 1/2.0;
            var t6 = te2 > 0 ? 1/te2 : 2;

            d[0] *= f1 * f2 * f3 * f4 * f5 * f6 * t1 * t2 * t3 * t4 * t5 * t6;
        });


        // health (TODO: factor current health, speed, etc.)
        // var fhCur = (double)(from.hasPokemon() ? from.getBattlePokemon().getHealth() : 0);
        // var fhMax = (double)(from.hasPokemon() ? from.getBattlePokemon().getMaxHealth() : 1);
        // var fhRel = fhCur/fhMax;
        var thRel = to.getHealth()/(double)to.getMaxHealth();
        d[0] *= thRel < 1.0 ? this.rng.nextDouble(thRel, 1.0) : 1.0;

        return Math.min(1, d[0]) * this.switchBias;
    }

    private double rngSin() {
        return org.joml.Math.sin(this.rng.nextDouble() * (org.joml.Math.PI/2));
    }
}
