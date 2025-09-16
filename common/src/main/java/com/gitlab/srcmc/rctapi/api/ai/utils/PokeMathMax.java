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
package com.gitlab.srcmc.rctapi.api.ai.utils;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.animations.ActionEffects;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.FormData;
import com.gitlab.srcmc.rctapi.ModCommon;
import java.util.*;

/**
 * Pokemon math utilities derived from davo899s implementation of a 'Pokemon Gen5
 * Battle AI' originally developed for CobblemonTrainers (see
 * https://github.com/davo899/CobblemonTrainers).
 */
public class PokeMathMax {
    private static final Random RANDOM = new Random();
    private static final String WEATHER_SUN = "sunny";
    private static final String WEATHER_RAIN = "raining";

    private static double damage(
            int attackerLevel,
            double attackerEffectiveAttack,
            double defenderEffectiveDefence,
            double movePower,
            boolean physical,
            boolean multiTarget,
            boolean rain,
            boolean sun,
            boolean parentalBond,
            boolean glaiveRush,
            boolean burn,
            boolean zmove,
            boolean reflect,
            boolean lightscreen,
            boolean attackerHasStatus,
            ElementalType moveType,
            BattlePokemon attacker,
            BattlePokemon defender,
            Map<Stat, Integer> attackerStages,
            Map<Stat, Integer> defenderStages) {
        var attackerEp = BattleStates.getTransformationOrEffected(attacker);
        var attackerPrimaryType = attackerEp.getPrimaryType();
        var attackerSecondaryType = attackerEp.getSecondaryType();
        var attackerTeraType = attacker.getEffectedPokemon().getTeraType();
        var attackerAbility = attackerEp.getAbility();
        String attackerHeldItem = attacker.getHeldItemManager().showdownId(attacker)!=null ? attacker.getHeldItemManager().showdownId(attacker): "";
        String defenderHeldItem = defender.getHeldItemManager().showdownId(defender)!=null ? defender.getHeldItemManager().showdownId(defender): "";
        // https://bulbapedia.bulbagarden.net/wiki/Damage#Generation_V_onward
        double baseDamage = (((2 * attackerLevel / 5.0 + 2) * movePower * attackerEffectiveAttack / defenderEffectiveDefence) / 50.0) + 2;
        if (multiTarget) baseDamage *= 0.75;
        if (parentalBond) baseDamage *= 0.25;
        if (glaiveRush) baseDamage *= 2;
        if (burn && physical && !attackerAbility.getName().equals("guts")) baseDamage *= 0.5;
        if (attackerHasStatus && attackerAbility.getName().equals("guts")) baseDamage *= 1.5;
        if (sun && moveType.equals(ElementalTypes.INSTANCE.getFIRE()) || rain && moveType.equals(ElementalTypes.INSTANCE.getWATER()))
            baseDamage *= 1.5;
        if (sun && moveType.equals(ElementalTypes.INSTANCE.getWATER()) || rain && moveType.equals(ElementalTypes.INSTANCE.getFIRE()))
            baseDamage *= 0.5;

        // STAB
        double stab = 1.0;
        var terastal = BattleStates.get(attacker.actor.battle).getPokemonState(attacker).has(BattleEffects.Custom.TERA);
        var adapt = attackerAbility.getName().equals("adaptability");

        if (terastal && attackerTeraType != null) {
            if (moveType.equals(attackerPrimaryType) || moveType.equals(attackerSecondaryType) || moveType.getName().equals(attackerTeraType.showdownId())) {
                var teraSame = attackerTeraType.showdownId().equals(attackerPrimaryType.getName())
                        || (attackerSecondaryType != null && attackerTeraType.showdownId().equals(attackerSecondaryType.getName()));

                stab = (teraSame && !adapt) ? 2.0
                        : (teraSame && adapt) ? 2.25
                        : 1.5; // (!teraSame && !adapt) || (!teraSame && adapt)
            }
        } else if (moveType.equals(attackerPrimaryType) || moveType.equals(attackerSecondaryType)) {
            stab = adapt ? 2.0 : 1.5;
        }

        baseDamage *= stab;
        //ModCommon.LOG.info(attackerHeldItem + " IS THE HELD ITEM");
        switch (attackerHeldItem) {
            case "choiceband":
                if (physical) {
                    baseDamage *= 1.5;
                }
                break;

            case "choicespecs":
                if (!physical) {
                    baseDamage *= 1.5;
                }
                break;

            case "muscleband":
                if (physical) {
                    baseDamage *= 1.1;
                }
                break;

            case "wiseglasses":
                if (!physical) {
                    baseDamage *= 1.1;
                }
                break;

            case "blackbelt":
                if (moveType.equals(ElementalTypes.INSTANCE.getFIGHTING())) {
                    baseDamage *= 1.2;
                }
                break;

            case "blackglasses":
                if (moveType.equals(ElementalTypes.INSTANCE.getDARK())) {
                    baseDamage *= 1.2;
                }
                break;

            case "charcoalstick":
                if (moveType.equals(ElementalTypes.INSTANCE.getFIRE())) {
                    baseDamage *= 1.2;
                }
                break;

            case "dragonfang":
                if (moveType.equals(ElementalTypes.INSTANCE.getDRAGON())) {
                    baseDamage *= 1.2;
                }
                break;

            case "hardstone":
                if (moveType.equals(ElementalTypes.INSTANCE.getROCK())) {
                    baseDamage *= 1.2;
                }
                break;

            case "magnet":
                if (moveType.equals(ElementalTypes.INSTANCE.getELECTRIC())) {
                    baseDamage *= 1.2;
                }
                break;

            case "metalcoat":
                if (moveType.equals(ElementalTypes.INSTANCE.getSTEEL())) {
                    baseDamage *= 1.2;
                }
                break;

            case "miracleseed":
                if (moveType.equals(ElementalTypes.INSTANCE.getGRASS())) {
                    baseDamage *= 1.2;
                }
                break;

            case "mysticwater":
                if (moveType.equals(ElementalTypes.INSTANCE.getWATER())) {
                    baseDamage *= 1.2;
                }
                break;

            case "nevermeltice":
                if (moveType.equals(ElementalTypes.INSTANCE.getICE())) {
                    baseDamage *= 1.2;
                }
                break;

            case "poisonbarb":
                if (moveType.equals(ElementalTypes.INSTANCE.getPOISON())) {
                    baseDamage *= 1.2;
                }
                break;

            case "sharpbeak":
                if (moveType.equals(ElementalTypes.INSTANCE.getFLYING())) {
                    baseDamage *= 1.2;
                }
                break;

            case "silkscarf":
                if (moveType.equals(ElementalTypes.INSTANCE.getNORMAL())) {
                    baseDamage *= 1.2;
                }
                break;

            case "silverpowder":
                if (moveType.equals(ElementalTypes.INSTANCE.getBUG())) {
                    baseDamage *= 1.2;
                }
                break;

            case "softsand":
                if (moveType.equals(ElementalTypes.INSTANCE.getGROUND())) {
                    baseDamage *= 1.2;
                }
                break;

            case "spelltag":
                if (moveType.equals(ElementalTypes.INSTANCE.getGHOST())) {
                    baseDamage *= 1.2;
                }
                break;

            case "twistedspoon":
                if (moveType.equals(ElementalTypes.INSTANCE.getPSYCHIC())) {
                    baseDamage *= 1.2;
                }
                break;

            case "lifeorb":
                baseDamage *= 1.3;
                break;
        }

        //if(isevio or is assaultvest) -> do stuff
        //else { baseDamage *= new function;
        //TODO: Create a function that handles move multiplier and returns it here.
        // TYPE
        baseDamage = baseDamage * TypeChart.getEffectiveness(moveType, defender);

        switch(defenderHeldItem){
            case "chilanberry":
                if(moveType.equals(ElementalTypes.INSTANCE.getNORMAL())){
                    baseDamage /= 2;
                }
                break;
            case "occaberry":

                break;
            case "passhoberry":
                
                break;
            case "wacanberry":
                
                break;
            case "rindoberry":
                
                break;
            case "yacheberry":
                
                break;
            case "chopleberry":
                
                break;
            case "kebiaberry":
                
                break;
            case "shucaberry":
                
                break;
            case "cobaberry":
                
                break;
            case "payapaberry":
                
                break;
            case "tangaberry":
                
                break;
            case "chartiberry":
                
                break;
            case "kasibberry":
                
                break;
            case "habanberry":
                
                break;
            case "colburberry":
                
                break;
            case "babiriberry":
                
                break;
        }
        
        return baseDamage;
    }

    public static int damage(BattlePokemon attacker, BattlePokemon defender, InBattleMove inBattleMove, Map<Stat, Integer> attackerStages, Map<Stat, Integer> defenderStages) {
        return damage(attacker, defender, TypeChart.getMove(inBattleMove), attackerStages, defenderStages);
    }

    public static int damage(BattlePokemon attacker, BattlePokemon defender, Move move, Map<Stat, Integer> attackerStages, Map<Stat, Integer> defenderStages) {
        var damageCategory = move.getDamageCategory().getName();

        if (damageCategory.equals(DamageCategories.INSTANCE.getSTATUS().getName())) {
            return 0;
        }

        var isPhysicalMove = damageCategory.equals(DamageCategories.INSTANCE.getPHYSICAL().getName());
        var isAttackerBurned = false;
        var statusContainer = attacker.getEffectedPokemon().getStatus();
        var weather = attacker.getContextManager().get(Type.WEATHER);
        var status = attacker.getContextManager().get(Type.STATUS);
        var acc = move.getAccuracy() / 100;

        if (statusContainer != null && !statusContainer.isExpired()) {
            isAttackerBurned = statusContainer.getStatus().equals(Statuses.INSTANCE.getBURN());
        }
        // accuracy factor is a custom modification
        return (int) Math.ceil(damage(
                attacker.getEffectedPokemon().getLevel(),
                calcAttackWithStatChanges(isPhysicalMove, attacker, attackerStages),
                calcDefenseWithStatChanges(isPhysicalMove, defender, defenderStages),
                move.getPower(),
                isPhysicalMove,
                false, // multiTarget
                weather != null && weather.stream().anyMatch(c -> c.getId().equals(WEATHER_RAIN)),
                weather != null && weather.stream().anyMatch(c -> c.getId().equals(WEATHER_SUN)),
                false, // parentalBond
                false, // glaiveRush
                isAttackerBurned,
                false, // zmove
                false, // reflect
                false, // lightscreen
                status != null && !status.isEmpty(),
                move.getName().equals("hiddenpower") ? TypeChart.getHiddenPowerType(attacker) : move.getType(),
                attacker,
                defender,
                attackerStages,
                defenderStages));
    }

    public static double calcAttackWithStatChanges(boolean isPhysical, BattlePokemon attacker, Map<Stat, Integer> statStages) {
        double multiplier = 1;
        if (isPhysical) {
            if (statStages.getOrDefault(Stats.ATTACK, 0) < 0) {
                double statChange = statStages.getOrDefault(Stats.ATTACK, 0);
                multiplier = 2 / (2 - (statChange));
                return BattleStates.getTransformationOrEffected(attacker).getAttack() * multiplier;
            } else if (statStages.getOrDefault(Stats.ATTACK, 0) > 0) {
                double statChange = statStages.getOrDefault(Stats.ATTACK, 0);
                multiplier = (2 + (statChange)) / 2;
                return BattleStates.getTransformationOrEffected(attacker).getAttack() * multiplier;
            }
            return BattleStates.getTransformationOrEffected(attacker).getAttack();
        }
        if (!isPhysical) {
            if (statStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) < 0) {
                double statChange = statStages.getOrDefault(Stats.SPECIAL_ATTACK, 0);
                multiplier = 2 / (2 - statChange);
                return BattleStates.getTransformationOrEffected(attacker).getSpecialAttack() * multiplier;
            } else if (statStages.getOrDefault(Stats.SPECIAL_ATTACK, 0) > 0) {
                double statChange = statStages.getOrDefault(Stats.SPECIAL_ATTACK, 0);
                multiplier = ((2 + statChange) / 2);
                return BattleStates.getTransformationOrEffected(attacker).getSpecialAttack() * multiplier;
            }
            return BattleStates.getTransformationOrEffected(attacker).getSpecialAttack();
        }
        return -1;
    }

    public static double calcDefenseWithStatChanges(boolean isPhysical, BattlePokemon defender, Map<Stat, Integer> statStages) {
        FormData data = defender.getEffectedPokemon().getForm();
        boolean hasEvolution = !data.getEvolutions().isEmpty();
        double multiplier = 1;
        double itemMultiplier = 1.5;
        boolean hasItem = false;
        double specialDefenseStat = BattleStates.getTransformationOrEffected(defender).getSpecialDefence();
        if (isPhysical) {
            if(defender.getHeldItemManager().showdownId(defender)!=null){
                if(defender.getHeldItemManager().showdownId(defender).equals("eviolite") && hasEvolution){
                    hasItem = true;
                }
            }
            if (statStages.getOrDefault(Stats.DEFENCE, 0) < 0) {
                double statChange = statStages.getOrDefault(Stats.DEFENCE, 0);
                multiplier = 2 / (2 - statChange);
            } else if (statStages.getOrDefault(Stats.DEFENCE, 0) > 0) {
                double statChange = statStages.getOrDefault(Stats.DEFENCE, 0);
                multiplier = (2 + statChange) / 2;
            }
            return hasItem == true ? specialDefenseStat * multiplier * itemMultiplier
                    : specialDefenseStat * multiplier;
        }
        if (!isPhysical) {
            if(defender.getHeldItemManager().showdownId(defender)!=null){
                if(defender.getHeldItemManager().showdownId(defender).equals("assaultvest")
                    || (defender.getHeldItemManager().showdownId(defender).equals("eviolite") && hasEvolution)){
                    hasItem = true;
                }
            }
            if (statStages.getOrDefault(Stats.SPECIAL_DEFENCE, 0) < 0) {
                double statChange = statStages.getOrDefault(Stats.SPECIAL_DEFENCE, 0);
                multiplier = 2 / (2 - statChange);
            } else if (statStages.getOrDefault(Stats.ATTACK, 0) > 0) {
                double statChange = statStages.getOrDefault(Stats.SPECIAL_DEFENCE, 0);
                multiplier = (2 + statChange) / 2;
            }
            return hasItem == true ? specialDefenseStat * multiplier * itemMultiplier
                    : specialDefenseStat * multiplier;
        }
        return -1;
    }
}
