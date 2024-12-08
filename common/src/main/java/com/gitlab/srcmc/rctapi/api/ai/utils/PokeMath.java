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
package com.gitlab.srcmc.rctapi.api.ai.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.ModCommon;

/**
 * Pokemon math utilities derived from davo899s implementation of a 'Pokemon Gen5
 * Battle AI' originally developed for CobblemonTrainers (see
 * https://github.com/davo899/CobblemonTrainers).
 */
public class PokeMath {
    private static final Random RANDOM = new Random();
    private static final double SUPER_EFFECTIVE = 2;
    private static final double NOT_VERY_EFFECTIVE = 0.5;
    private static final double IMMUNE = 0;
    private static final String WEATHER_SUN = "sunny";
    private static final String WEATHER_RAIN = "raining";
    private static final Map<ElementalType, Map<ElementalType, Double>> typeChart = new HashMap<>();
    private static final Map<String, Move> moveMap = new HashMap<>();

    private static Move getMove(InBattleMove move) {
        return moveMap.computeIfAbsent(move.id, key -> {
            var m = Moves.INSTANCE.getByName(key);

            // In my experience this only happened with 'recharge' (not a problem).
            if(m == null) {
                if(!key.equals("recharge")) {
                    ModCommon.LOG.error("Failed to create move template for '" + key + "'");
                }

                m = Moves.INSTANCE.getExceptional();
            }

            return m.create();
        });
    }

    private static double typeEffectiveness(ElementalType attacker, ElementalType defender) {
        if (!typeChart.containsKey(defender))
            return 1;
        if (typeChart.get(defender).containsKey(attacker))
            return typeChart.get(defender).get(attacker);
        return 1;
    }

    public static double typeEffectiveness(Pokemon attacker, Pokemon defender) {
        var d = typeEffectiveness(attacker.getPrimaryType(), defender.getPrimaryType(), defender.getAbility());

        if(d != 0 && attacker.getSecondaryType() != null) {
            d *= typeEffectiveness(attacker.getSecondaryType(), defender.getPrimaryType(), defender.getAbility());

            if(d != 0 && defender.getSecondaryType() != null) {
                d *= typeEffectiveness(attacker.getSecondaryType(), defender.getSecondaryType(), defender.getAbility());
            }
        }

        if(d != 0 && defender.getSecondaryType() != null) {
            d *= typeEffectiveness(attacker.getPrimaryType(), defender.getPrimaryType(), defender.getAbility());
        }

        return d;
    }

    public static double typeEffectiveness(InBattleMove move, Pokemon defender) {
        return typeEffectiveness(getMove(move), defender);
    }

    private static double typeEffectiveness(Move move, Pokemon defender) {
        var d = typeEffectiveness(move.getType(), defender.getPrimaryType(), defender.getAbility());

        if(d != 0 && defender.getSecondaryType() != null) {
            d *= typeEffectiveness(move.getType(), defender.getPrimaryType(), defender.getAbility());
        }

        return d;
    }

    public static double typeEffectiveness(ElementalType attacker, ElementalType defender, Ability defenderAbility) {
        String defenderAbilityId = defenderAbility.getDisplayName();

        if (attacker.equals(ElementalTypes.INSTANCE.getWATER())) {
            if (defenderAbilityId.equals("cobblemon.ability.stormdrain") ||
                    defenderAbilityId.equals("cobblemon.ability.waterabsorb") ||
                    defenderAbilityId.equals("cobblemon.ability.dryskin")) {
                return 0;
            }
        } else if (attacker.equals(ElementalTypes.INSTANCE.getELECTRIC())) {
            if (defenderAbilityId.equals("cobblemon.ability.voltabsorb") ||
                    defenderAbilityId.equals("cobblemon.ability.lightningrod") ||
                    defenderAbilityId.equals("cobblemon.ability.motordrive")) {
                return 0;
            }
        } else if (attacker.equals(ElementalTypes.INSTANCE.getGROUND())) {
            if (defenderAbilityId.equals("cobblemon.ability.levitate") ||
                    defenderAbilityId.equals("cobblemon.ability.eartheater")) {
                return 0;
            }
        } else if (attacker.equals(ElementalTypes.INSTANCE.getFIRE())) {
            if (defenderAbilityId.equals("cobblemon.ability.wellbakedbody") ||
                    defenderAbilityId.equals("cobblemon.ability.flashfire")) {
                return 0;
            }
        } else if (attacker.equals(ElementalTypes.INSTANCE.getGRASS())) {
            if (defenderAbilityId.equals("cobblemon.ability.sapsipper")) {
                return 0;
            }
        }

        double typeEffectiveness = typeEffectiveness(attacker, defender);
        if (defenderAbilityId.equals("cobblemon.ability.wonderguard") &&
                typeEffectiveness != SUPER_EFFECTIVE) {
            return 0;
        }

        return typeEffectiveness;
    }

    private static double damage(
        int attackerLevel,
        int attackerEffectiveAttack,
        int defenderEffectiveDefence,
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
        ElementalType attackerPrimaryType,
        ElementalType attackerSecondaryType,
        ElementalType defenderPrimaryType,
        ElementalType defenderSecondaryType,
        Ability defenderAbility,
        Ability attackerAbility)
    {
        var baseDamage = (int)(((2 * attackerLevel / 5.0 + 2) * movePower * attackerEffectiveAttack / (double) defenderEffectiveDefence)/50.0) + 2;
        if(multiTarget) baseDamage *= 0.75;
        if(parentalBond) baseDamage *= 0.25;
        if(glaiveRush) baseDamage *= 2;
        if(burn && physical && !attackerAbility.getDisplayName().equals("cobblemon.ability.guts")) baseDamage *= 0.5;
        if(attackerHasStatus && attackerAbility.getDisplayName().equals("cobblemon.ability.guts")) baseDamage *= 1.5;
        if(sun && moveType.equals(ElementalTypes.INSTANCE.getFIRE()) || rain && moveType.equals(ElementalTypes.INSTANCE.getWATER())) baseDamage *= 1.5;
        if(sun && moveType.equals(ElementalTypes.INSTANCE.getWATER()) || rain && moveType.equals(ElementalTypes.INSTANCE.getFIRE())) baseDamage *= 0.5;
        if(moveType.equals(attackerPrimaryType) || moveType.equals(attackerSecondaryType)) baseDamage *= 1.5;

        baseDamage = (int)(baseDamage
            * typeEffectiveness(moveType, defenderPrimaryType, defenderAbility)
            * (defenderSecondaryType == null ? 1 : typeEffectiveness(moveType, defenderSecondaryType, defenderAbility)));

        return baseDamage > 0
            ? Math.max(1, baseDamage * ((RANDOM.nextDouble() * 0.15) + 0.85))
            : baseDamage;
    }

    public static boolean isStatus(InBattleMove inBattleMove) {
        var move = getMove(inBattleMove);
        return move.getDamageCategory().getName().equals(DamageCategories.INSTANCE.getSTATUS().getName());
    }

    public static int damage(BattlePokemon attacker, BattlePokemon defender, InBattleMove inBattleMove) {
        var move = getMove(inBattleMove);
        var damageCategory = move.getDamageCategory().getName();

        if(damageCategory.equals(DamageCategories.INSTANCE.getSTATUS().getName())) {
            return 0;
        }

        var isPhysicalMove = damageCategory.equals(DamageCategories.INSTANCE.getPHYSICAL().getName());
        var isAttackerBurned = false;
        var statusContainer = attacker.getEffectedPokemon().getStatus();
        var weather = attacker.getContextManager().get(Type.WEATHER);
        var status = attacker.getContextManager().get(Type.STATUS);
        var acc = move.getAccuracy()/100;

        if(statusContainer != null && !statusContainer.isExpired()) {
            isAttackerBurned = statusContainer.getStatus().equals(Statuses.INSTANCE.getBURN());
        }

        return (int)((acc + RANDOM.nextDouble() * (1.0 - acc)) * damage(
            attacker.getOriginalPokemon().getLevel(),
            attacker.getOriginalPokemon().getStat(isPhysicalMove ? Stats.ATTACK : Stats.SPECIAL_ATTACK),
            defender.getOriginalPokemon().getStat(isPhysicalMove ? Stats.DEFENCE : Stats.SPECIAL_DEFENCE),
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
            move.getType(),
            attacker.getOriginalPokemon().getPrimaryType(),
            attacker.getOriginalPokemon().getSecondaryType(),
            defender.getOriginalPokemon().getPrimaryType(),
            defender.getOriginalPokemon().getSecondaryType(),
            defender.getOriginalPokemon().getAbility(),
            attacker.getOriginalPokemon().getAbility()));
    }

    public static double powerAndTypeDamage(
        double movePower,
        ElementalType moveType,
        ElementalType defenderPrimaryType,
        ElementalType defenderSecondaryType)
    {
        return movePower
            * typeEffectiveness(moveType, defenderPrimaryType)
            * (defenderSecondaryType == null ? 1 : typeEffectiveness(moveType, defenderSecondaryType));
    }

    static {
        ElementalTypes types = ElementalTypes.INSTANCE;
        ElementalType NORMAL = types.getNORMAL();
        ElementalType FIGHTING = types.getFIGHTING();
        ElementalType FLYING = types.getFLYING();
        ElementalType POISON = types.getPOISON();
        ElementalType GROUND = types.getGROUND();
        ElementalType ROCK = types.getROCK();
        ElementalType BUG = types.getBUG();
        ElementalType GHOST = types.getGHOST();
        ElementalType STEEL = types.getSTEEL();
        ElementalType FIRE = types.getFIRE();
        ElementalType WATER = types.getWATER();
        ElementalType GRASS = types.getGRASS();
        ElementalType ELECTRIC = types.getELECTRIC();
        ElementalType PSYCHIC = types.getPSYCHIC();
        ElementalType ICE = types.getICE();
        ElementalType DRAGON = types.getDRAGON();
        ElementalType DARK = types.getDARK();
        ElementalType FAIRY = types.getFAIRY();

        typeChart.put(NORMAL, Map.of(
                FIGHTING, SUPER_EFFECTIVE,
                GHOST, IMMUNE));
        typeChart.put(FIGHTING, Map.of(
                FLYING, SUPER_EFFECTIVE,
                ROCK, NOT_VERY_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                PSYCHIC, SUPER_EFFECTIVE,
                DARK, NOT_VERY_EFFECTIVE,
                FAIRY, SUPER_EFFECTIVE));
        typeChart.put(FLYING, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                GROUND, IMMUNE,
                ROCK, SUPER_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                ELECTRIC, SUPER_EFFECTIVE,
                ICE, SUPER_EFFECTIVE));
        typeChart.put(POISON, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                POISON, NOT_VERY_EFFECTIVE,
                GROUND, SUPER_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                PSYCHIC, SUPER_EFFECTIVE,
                FAIRY, NOT_VERY_EFFECTIVE));
        typeChart.put(GROUND, Map.of(
                POISON, NOT_VERY_EFFECTIVE,
                ROCK, NOT_VERY_EFFECTIVE,
                WATER, SUPER_EFFECTIVE,
                GRASS, SUPER_EFFECTIVE,
                ELECTRIC, IMMUNE,
                ICE, SUPER_EFFECTIVE));
        typeChart.put(ROCK, Map.of(
                NORMAL, NOT_VERY_EFFECTIVE,
                FIGHTING, SUPER_EFFECTIVE,
                FLYING, NOT_VERY_EFFECTIVE,
                POISON, NOT_VERY_EFFECTIVE,
                GROUND, SUPER_EFFECTIVE,
                STEEL, SUPER_EFFECTIVE,
                FIRE, NOT_VERY_EFFECTIVE,
                WATER, SUPER_EFFECTIVE,
                GRASS, SUPER_EFFECTIVE));
        typeChart.put(BUG, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                FLYING, SUPER_EFFECTIVE,
                GROUND, NOT_VERY_EFFECTIVE,
                ROCK, SUPER_EFFECTIVE,
                FIRE, SUPER_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE));
        typeChart.put(GHOST, Map.of(
                NORMAL, IMMUNE,
                FIGHTING, IMMUNE,
                POISON, NOT_VERY_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                GHOST, SUPER_EFFECTIVE,
                DARK, SUPER_EFFECTIVE));
        Map<ElementalType, Double> steelMap = new HashMap<>(Map.of(
                NORMAL, NOT_VERY_EFFECTIVE,
                FIGHTING, SUPER_EFFECTIVE,
                FLYING, NOT_VERY_EFFECTIVE,
                POISON, IMMUNE,
                GROUND, SUPER_EFFECTIVE,
                ROCK, NOT_VERY_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                STEEL, NOT_VERY_EFFECTIVE,
                FIRE, SUPER_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE));
        steelMap.put(PSYCHIC, NOT_VERY_EFFECTIVE);
        steelMap.put(ICE, NOT_VERY_EFFECTIVE);
        steelMap.put(DRAGON, NOT_VERY_EFFECTIVE);
        steelMap.put(FAIRY, NOT_VERY_EFFECTIVE);
        typeChart.put(STEEL, steelMap);
        typeChart.put(FIRE, Map.of(
                GROUND, SUPER_EFFECTIVE,
                ROCK, SUPER_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                STEEL, NOT_VERY_EFFECTIVE,
                FIRE, NOT_VERY_EFFECTIVE,
                WATER, SUPER_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                ICE, NOT_VERY_EFFECTIVE,
                FAIRY, NOT_VERY_EFFECTIVE));
        typeChart.put(WATER, Map.of(
                STEEL, NOT_VERY_EFFECTIVE,
                FIRE, NOT_VERY_EFFECTIVE,
                WATER, NOT_VERY_EFFECTIVE,
                GRASS, SUPER_EFFECTIVE,
                ELECTRIC, SUPER_EFFECTIVE,
                ICE, NOT_VERY_EFFECTIVE));
        typeChart.put(GRASS, Map.of(
                FLYING, SUPER_EFFECTIVE,
                POISON, SUPER_EFFECTIVE,
                GROUND, NOT_VERY_EFFECTIVE,
                BUG, SUPER_EFFECTIVE,
                FIRE, SUPER_EFFECTIVE,
                WATER, NOT_VERY_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                ELECTRIC, NOT_VERY_EFFECTIVE,
                ICE, SUPER_EFFECTIVE));
        typeChart.put(ELECTRIC, Map.of(
                FLYING, NOT_VERY_EFFECTIVE,
                GROUND, SUPER_EFFECTIVE,
                STEEL, NOT_VERY_EFFECTIVE,
                ELECTRIC, NOT_VERY_EFFECTIVE));
        typeChart.put(PSYCHIC, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                BUG, SUPER_EFFECTIVE,
                GHOST, SUPER_EFFECTIVE,
                PSYCHIC, NOT_VERY_EFFECTIVE,
                DARK, SUPER_EFFECTIVE));
        typeChart.put(ICE, Map.of(
                FIGHTING, SUPER_EFFECTIVE,
                ROCK, SUPER_EFFECTIVE,
                STEEL, SUPER_EFFECTIVE,
                FIRE, SUPER_EFFECTIVE,
                ICE, NOT_VERY_EFFECTIVE));
        typeChart.put(DRAGON, Map.of(
                FIRE, NOT_VERY_EFFECTIVE,
                WATER, NOT_VERY_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                ELECTRIC, NOT_VERY_EFFECTIVE,
                ICE, SUPER_EFFECTIVE,
                DRAGON, SUPER_EFFECTIVE,
                FAIRY, SUPER_EFFECTIVE));
        typeChart.put(DARK, Map.of(
                FIGHTING, SUPER_EFFECTIVE,
                BUG, SUPER_EFFECTIVE,
                GHOST, NOT_VERY_EFFECTIVE,
                PSYCHIC, IMMUNE,
                DARK, NOT_VERY_EFFECTIVE,
                FAIRY, SUPER_EFFECTIVE));
        typeChart.put(FAIRY, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                POISON, SUPER_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                STEEL, SUPER_EFFECTIVE,
                DRAGON, IMMUNE,
                DARK, NOT_VERY_EFFECTIVE));
    }
}
