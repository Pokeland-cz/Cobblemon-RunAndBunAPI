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

import java.util.HashMap;
import java.util.Map;

import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.ModCommon;

public final class TypeChart {
    private static final Map<ElementalType, Map<ElementalType, Double>> CHART = new HashMap<>();
    private static final Map<ElementalType, Double> EMPTY_MAP = Map.of();
    private static final Map<String, Move> MOVE_MAP = new HashMap<>();
    private static final double SUPER_EFFECTIVE = 2;
    private static final double NOT_VERY_EFFECTIVE = 0.5;
    private static final double IMMUNE = 0;

    public static double getEffectiveness(InBattleMove move, BattlePokemon defender) {
        return getEffectiveness(getMove(move).getType(), defender);
    }

    public static double getEffectiveness(BattlePokemon attacker, BattlePokemon defender) {
        var aep = attacker.getEffectedPokemon();
        var sec = aep.getSecondaryType();
        return getEffectiveness(aep.getPrimaryType(), defender) * (sec != null ? getEffectiveness(sec, defender) : 1.0);
    }

    public static double getEffectiveness(ElementalType attacker, BattlePokemon defender) {
        var ep = defender.getEffectedPokemon();
        return getEffectiveness(attacker, ep.getPrimaryType(), ep.getSecondaryType(), ep.getAbility());
    }

    public static double getEffectiveness(ElementalType attacker, ElementalType defenderPrimaryType, ElementalType defenderSecondaryType, Ability defenderAbility) {
        var eff = (getEffectiveness(attacker, defenderPrimaryType, defenderAbility)) * (defenderSecondaryType != null ?  getEffectiveness(attacker, defenderSecondaryType, defenderAbility) : 1.0);

        if(eff < 2.0 && defenderAbility.getName().equals("wonderguard")) {
            return 0;
        }

        return eff;
    }

    private static double getEffectiveness(ElementalType attacker, ElementalType defender, Ability defenderAbility) {
        String defenderAbilityId = defenderAbility.getName();

        if(attacker.equals(ElementalTypes.INSTANCE.getWATER())) {
            if(defenderAbilityId.equals("stormdrain")
                || defenderAbilityId.equals("waterabsorb")
                || defenderAbilityId.equals("dryskin"))
            {
                return 0;
            }
        } else if(attacker.equals(ElementalTypes.INSTANCE.getELECTRIC())) {
            if(defenderAbilityId.equals("voltabsorb")
                || defenderAbilityId.equals("lightningrod")
                || defenderAbilityId.equals("motordrive"))
            {
                return 0;
            }
        } else if(attacker.equals(ElementalTypes.INSTANCE.getGROUND())) {
            if(defenderAbilityId.equals("levitate") || defenderAbilityId.equals("eartheater")) {
                return 0;
            }
        } else if(attacker.equals(ElementalTypes.INSTANCE.getFIRE())) {
            if(defenderAbilityId.equals("wellbakedbody") || defenderAbilityId.equals("flashfire")) {
                return 0;
            }
        } else if(attacker.equals(ElementalTypes.INSTANCE.getGRASS())) {
            if(defenderAbilityId.equals("sapsipper")) {
                return 0;
            }
        }
        
        return CHART.getOrDefault(defender, EMPTY_MAP).getOrDefault(attacker, 1.0);
    }

    public static Move getMove(InBattleMove move) {
        return MOVE_MAP.computeIfAbsent(move.id, key -> {
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

    static {
        var types = ElementalTypes.INSTANCE;
        var NORMAL = types.getNORMAL();
        var FIGHTING = types.getFIGHTING();
        var FLYING = types.getFLYING();
        var POISON = types.getPOISON();
        var GROUND = types.getGROUND();
        var ROCK = types.getROCK();
        var BUG = types.getBUG();
        var GHOST = types.getGHOST();
        var STEEL = types.getSTEEL();
        var FIRE = types.getFIRE();
        var WATER = types.getWATER();
        var GRASS = types.getGRASS();
        var ELECTRIC = types.getELECTRIC();
        var PSYCHIC = types.getPSYCHIC();
        var ICE = types.getICE();
        var DRAGON = types.getDRAGON();
        var DARK = types.getDARK();
        var FAIRY = types.getFAIRY();

        CHART.put(NORMAL, Map.of(
            FIGHTING, SUPER_EFFECTIVE,
            GHOST, IMMUNE));

        CHART.put(FIGHTING, Map.of(
            FLYING, SUPER_EFFECTIVE,
            ROCK, NOT_VERY_EFFECTIVE,
            BUG, NOT_VERY_EFFECTIVE,
            PSYCHIC, SUPER_EFFECTIVE,
            DARK, NOT_VERY_EFFECTIVE,
            FAIRY, SUPER_EFFECTIVE));

        CHART.put(FLYING, Map.of(
            FIGHTING, NOT_VERY_EFFECTIVE,
            GROUND, IMMUNE,
            ROCK, SUPER_EFFECTIVE,
            BUG, NOT_VERY_EFFECTIVE,
            GRASS, NOT_VERY_EFFECTIVE,
            ELECTRIC, SUPER_EFFECTIVE,
            ICE, SUPER_EFFECTIVE));

        CHART.put(POISON, Map.of(
            FIGHTING, NOT_VERY_EFFECTIVE,
            POISON, NOT_VERY_EFFECTIVE,
            GROUND, SUPER_EFFECTIVE,
            BUG, NOT_VERY_EFFECTIVE,
            GRASS, NOT_VERY_EFFECTIVE,
            PSYCHIC, SUPER_EFFECTIVE,
            FAIRY, NOT_VERY_EFFECTIVE));

        CHART.put(GROUND, Map.of(
            POISON, NOT_VERY_EFFECTIVE,
            ROCK, NOT_VERY_EFFECTIVE,
            WATER, SUPER_EFFECTIVE,
            GRASS, SUPER_EFFECTIVE,
            ELECTRIC, IMMUNE,
            ICE, SUPER_EFFECTIVE));

        CHART.put(ROCK, Map.of(
            NORMAL, NOT_VERY_EFFECTIVE,
            FIGHTING, SUPER_EFFECTIVE,
            FLYING, NOT_VERY_EFFECTIVE,
            POISON, NOT_VERY_EFFECTIVE,
            GROUND, SUPER_EFFECTIVE,
            STEEL, SUPER_EFFECTIVE,
            FIRE, NOT_VERY_EFFECTIVE,
            WATER, SUPER_EFFECTIVE,
            GRASS, SUPER_EFFECTIVE));

        CHART.put(BUG, Map.of(
            FIGHTING, NOT_VERY_EFFECTIVE,
            FLYING, SUPER_EFFECTIVE,
            GROUND, NOT_VERY_EFFECTIVE,
            ROCK, SUPER_EFFECTIVE,
            FIRE, SUPER_EFFECTIVE,
            GRASS, NOT_VERY_EFFECTIVE));

        CHART.put(GHOST, Map.of(
            NORMAL, IMMUNE,
            FIGHTING, IMMUNE,
            POISON, NOT_VERY_EFFECTIVE,
            BUG, NOT_VERY_EFFECTIVE,
            GHOST, SUPER_EFFECTIVE,
            DARK, SUPER_EFFECTIVE));

        var steelMap = new HashMap<>(Map.of(
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
        CHART.put(STEEL, steelMap);

        CHART.put(FIRE, Map.of(
            GROUND, SUPER_EFFECTIVE,
            ROCK, SUPER_EFFECTIVE,
            BUG, NOT_VERY_EFFECTIVE,
            STEEL, NOT_VERY_EFFECTIVE,
            FIRE, NOT_VERY_EFFECTIVE,
            WATER, SUPER_EFFECTIVE,
            GRASS, NOT_VERY_EFFECTIVE,
            ICE, NOT_VERY_EFFECTIVE,
            FAIRY, NOT_VERY_EFFECTIVE));

        CHART.put(WATER, Map.of(
            STEEL, NOT_VERY_EFFECTIVE,
            FIRE, NOT_VERY_EFFECTIVE,
            WATER, NOT_VERY_EFFECTIVE,
            GRASS, SUPER_EFFECTIVE,
            ELECTRIC, SUPER_EFFECTIVE,
            ICE, NOT_VERY_EFFECTIVE));

        CHART.put(GRASS, Map.of(
            FLYING, SUPER_EFFECTIVE,
            POISON, SUPER_EFFECTIVE,
            GROUND, NOT_VERY_EFFECTIVE,
            BUG, SUPER_EFFECTIVE,
            FIRE, SUPER_EFFECTIVE,
            WATER, NOT_VERY_EFFECTIVE,
            GRASS, NOT_VERY_EFFECTIVE,
            ELECTRIC, NOT_VERY_EFFECTIVE,
            ICE, SUPER_EFFECTIVE));

        CHART.put(ELECTRIC, Map.of(
            FLYING, NOT_VERY_EFFECTIVE,
            GROUND, SUPER_EFFECTIVE,
            STEEL, NOT_VERY_EFFECTIVE,
            ELECTRIC, NOT_VERY_EFFECTIVE));

        CHART.put(PSYCHIC, Map.of(
            FIGHTING, NOT_VERY_EFFECTIVE,
            BUG, SUPER_EFFECTIVE,
            GHOST, SUPER_EFFECTIVE,
            PSYCHIC, NOT_VERY_EFFECTIVE,
            DARK, SUPER_EFFECTIVE));

        CHART.put(ICE, Map.of(
            FIGHTING, SUPER_EFFECTIVE,
            ROCK, SUPER_EFFECTIVE,
            STEEL, SUPER_EFFECTIVE,
            FIRE, SUPER_EFFECTIVE,
            ICE, NOT_VERY_EFFECTIVE));

        CHART.put(DRAGON, Map.of(
            FIRE, NOT_VERY_EFFECTIVE,
            WATER, NOT_VERY_EFFECTIVE,
            GRASS, NOT_VERY_EFFECTIVE,
            ELECTRIC, NOT_VERY_EFFECTIVE,
            ICE, SUPER_EFFECTIVE,
            DRAGON, SUPER_EFFECTIVE,
            FAIRY, SUPER_EFFECTIVE));

        CHART.put(DARK, Map.of(
            FIGHTING, SUPER_EFFECTIVE,
            BUG, SUPER_EFFECTIVE,
            GHOST, NOT_VERY_EFFECTIVE,
            PSYCHIC, IMMUNE,
            DARK, NOT_VERY_EFFECTIVE,
            FAIRY, SUPER_EFFECTIVE));

        CHART.put(FAIRY, Map.of(
            FIGHTING, NOT_VERY_EFFECTIVE,
            POISON, SUPER_EFFECTIVE,
            BUG, NOT_VERY_EFFECTIVE,
            STEEL, SUPER_EFFECTIVE,
            DRAGON, IMMUNE,
            DARK, NOT_VERY_EFFECTIVE));
    }
}
