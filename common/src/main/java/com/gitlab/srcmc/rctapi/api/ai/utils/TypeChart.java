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
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
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

    // deprecated: 

    /**
     * No hiddedpower support. Use {@link TypeChart#getHiddenPowerType(BattlePokemon)}
     * and {@link TypeChart#getEffectiveness(ElementalType, BattlePokemon)} instead.
     * 
     * @deprecated This overload will be removed in 0.14.
     */
    @Deprecated(since = "0.13.5")
    public static double getEffectiveness(InBattleMove move, BattlePokemon defender) {
        return getEffectiveness(getMove(move).getType(), defender);
    }

    public static double getEffectiveness(BattlePokemon attacker, BattlePokemon defender) {
        var aep = attacker.getEffectedPokemon();
        var sec = aep.getSecondaryType();
        return getEffectiveness(aep.getPrimaryType(), defender) * (sec != null ? getEffectiveness(sec, defender) : 1.0);
    }

    public static double getEffectiveness(ElementalType attackerType, BattlePokemon defender) {
        var ep = defender.getEffectedPokemon();
        var eff = BattleStates.get(defender.actor.battle).getPokemonState(defender).has(BattleEffects.Custom.TERA)
            ? getEffectiveness(attackerType, ElementalTypes.INSTANCE.get(ep.getTeraType().showdownId()), defender)
            : ((getEffectiveness(attackerType, ep.getPrimaryType(), defender)) * (ep.getSecondaryType() != null ?  getEffectiveness(attackerType, ep.getSecondaryType(), defender) : 1.0));

        if(eff < 2.0 && ep.getAbility().getName().equals("wonderguard")) {
            return 0;
        }

        return eff;
    }

    /**
     * @return 1.0
     * @deprecated This overload will be removed in 0.14.
     */
    @Deprecated(since = "0.11.1")
    public static double getEffectiveness(ElementalType attackerType, ElementalType defenderPrimaryType, ElementalType defenderSecondaryType, Ability defenderAbility) {
        return 1.0;
    }

    private static double getEffectiveness(ElementalType attackerType, ElementalType defenderType, BattlePokemon defender) {
        var defenderAbilityId = defender.getEffectedPokemon().getAbility().getName();

        if(attackerType.equals(ElementalTypes.INSTANCE.getWATER())) {
            if(defenderAbilityId.equals("stormdrain")
                || defenderAbilityId.equals("waterabsorb")
                || defenderAbilityId.equals("dryskin"))
            {
                return 0;
            }
        } else if(attackerType.equals(ElementalTypes.INSTANCE.getELECTRIC())) {
            if(defenderAbilityId.equals("voltabsorb")
                || defenderAbilityId.equals("lightningrod")
                || defenderAbilityId.equals("motordrive"))
            {
                return 0;
            }
        } else if(attackerType.equals(ElementalTypes.INSTANCE.getGROUND())) {
            if(defenderAbilityId.equals("eartheater")) {
                return 0;
            }

            if(BattleEffects.Pokemon.State.raised(defender)) {
                return 0;
            } else if(defenderType.equals(ElementalTypes.INSTANCE.getFLYING())) {
                return 1.0;
            }
        } else if(attackerType.equals(ElementalTypes.INSTANCE.getFIRE())) {
            if(defenderAbilityId.equals("wellbakedbody") || defenderAbilityId.equals("flashfire")) {
                return 0;
            }
        } else if(attackerType.equals(ElementalTypes.INSTANCE.getGRASS())) {
            if(defenderAbilityId.equals("sapsipper")) {
                return 0;
            }
        }
        
        return CHART.getOrDefault(defenderType, EMPTY_MAP).getOrDefault(attackerType, 1.0);
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

    private static final ElementalType[] HIDDENPOWER_TYPES = new ElementalType[]{
        ElementalTypes.INSTANCE.getFIGHTING(),
        ElementalTypes.INSTANCE.getFLYING(),
        ElementalTypes.INSTANCE.getPOISON(),
        ElementalTypes.INSTANCE.getGROUND(),
        ElementalTypes.INSTANCE.getROCK(),
        ElementalTypes.INSTANCE.getBUG(),
        ElementalTypes.INSTANCE.getGHOST(),
        ElementalTypes.INSTANCE.getSTEEL(),
        ElementalTypes.INSTANCE.getFIRE(),
        ElementalTypes.INSTANCE.getWATER(),
        ElementalTypes.INSTANCE.getGRASS(),
        ElementalTypes.INSTANCE.getELECTRIC(),
        ElementalTypes.INSTANCE.getPSYCHIC(),
        ElementalTypes.INSTANCE.getICE(),
        ElementalTypes.INSTANCE.getDRAGON(),
        ElementalTypes.INSTANCE.getDARK()
    };

    // see: https://bulbapedia.bulbagarden.net/wiki/Hidden_Power_(move)/Calculation
    public static ElementalType getHiddenPowerType(BattlePokemon pkmn) {
        var ivs = pkmn.getEffectedPokemon().getIvs();
        return HIDDENPOWER_TYPES[((
            (ivs.get(Stats.HP) & 1)
            + (ivs.get(Stats.ATTACK) & 1) * 2
            + (ivs.get(Stats.DEFENCE) & 1) * 4
            + (ivs.get(Stats.SPEED) & 1) * 8
            + (ivs.get(Stats.SPECIAL_ATTACK) & 1) * 16
            + (ivs.get(Stats.SPECIAL_DEFENCE) & 1) * 32)*15) / 63];
    }

    public static final int NORMAL = 1<<1;
    public static final int FIGHTING = 1<<2;
    public static final int FLYING = 1<<3;
    public static final int POISON = 1<<4;
    public static final int GROUND = 1<<5;
    public static final int ROCK = 1<<6;
    public static final int BUG = 1<<7;
    public static final int GHOST = 1<<8;
    public static final int STEEL = 1<<9;
    public static final int FIRE = 1<<10;
    public static final int WATER = 1<<11;
    public static final int GRASS = 1<<12;
    public static final int ELECTRIC = 1<<13;
    public static final int PSYCHIC = 1<<14;
    public static final int ICE = 1<<15;
    public static final int DRAGON = 1<<16;
    public static final int DARK = 1<<17;
    public static final int FAIRY = 1<<18;

    private static final Map<ElementalType, Integer> TYPE_MASKS = Map.<ElementalType, Integer>ofEntries(
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getNORMAL(), NORMAL),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getFIGHTING(), FIGHTING),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getFLYING(), FLYING),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getPOISON(), POISON),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getGROUND(), GROUND),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getROCK(), ROCK),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getBUG(), BUG),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getGHOST(), GHOST),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getSTEEL(), STEEL),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getFIRE(), FIRE),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getWATER(), WATER),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getGRASS(), GRASS),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getELECTRIC(), ELECTRIC),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getPSYCHIC(), PSYCHIC),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getICE(), ICE),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getDRAGON(), DRAGON),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getDARK(), DARK),
        Map.<ElementalType, Integer>entry(ElementalTypes.INSTANCE.getFAIRY(), FAIRY)
    );

    public static boolean is(BattlePokemon pkmn, int type) {
        var ep = pkmn.getEffectedPokemon();
        var p = ep.getPrimaryType() != null ? TYPE_MASKS.getOrDefault(ep.getPrimaryType(), 0) : 0;
        var s = ep.getSecondaryType() != null ? TYPE_MASKS.getOrDefault(ep.getSecondaryType(), 0) : p;
        return (type & (p | s)) != 0;
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
