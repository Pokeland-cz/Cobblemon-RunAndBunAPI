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
package com.gitlab.srcmc.rctapi.api.ai.experimental.lai;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;

class BattleState {
    public static final Set<String> BOOST_IDS = Set.of("hp", "atk", "def", "spa", "sde", "spd");
    public static final Set<String> STATUS_EFFECT_IDS = Set.of("slp", "brn", "psn", "par");

    public static final int MAX_PKMN_LEVEL = 100;
    public static final int MAX_PARTY_COUNT = 6;
    public static final int MAX_STAT_BOOST = 6;
    public static final int MAX_STAT_BOOSTS = BOOST_IDS.size() * MAX_STAT_BOOST;

    public final int sourcePartyCount;
    public final int targetPartyCount;
    public final int sourcePartyHealth;
    public final int sourcePartyMaxHealth;
    public final int targetPartyHealth;
    public final int targetPartyMaxHealth;
    public final int sourcePartyStatBoosts;
    public final int targetPartyStatBoosts;
    public final int sourcePartyStatusEffects;
    public final int targetPartyStatusEffects;
    public final double sourceAverageLevel;
    public final double targetAverageLevel;

    private BattleState(
        int sourcePartyCount,
        int targetPartyCount,
        int sourcePartyHealth,
        int sourcePartyMaxHealth,
        int targetPartyHealth,
        int targetPartyMaxHealth,
        int sourcePartyStatBoosts,
        int targetPartyStatBoosts,
        int sourcePartyStatusEffects,
        int targetPartyStatusEffects,
        double sourceAverageLevel,
        double targetAverageLevel)
    {
        this.sourcePartyCount = sourcePartyCount;
        this.targetPartyCount = targetPartyCount;
        this.sourcePartyHealth = sourcePartyHealth;
        this.sourcePartyMaxHealth = sourcePartyMaxHealth;
        this.targetPartyHealth = targetPartyHealth;
        this.targetPartyMaxHealth = targetPartyMaxHealth;
        this.sourcePartyStatBoosts = sourcePartyStatBoosts;
        this.targetPartyStatBoosts = targetPartyStatBoosts;
        this.sourcePartyStatusEffects = sourcePartyStatusEffects;
        this.targetPartyStatusEffects = targetPartyStatusEffects;
        this.sourceAverageLevel = sourceAverageLevel;
        this.targetAverageLevel = targetAverageLevel;

        // ModCommon.LOG.info("## New BattleState");
        // ModCommon.LOG.info("sourcePartyCount: " + sourcePartyCount);
        // ModCommon.LOG.info("targetPartyCount: " + targetPartyCount);
        // ModCommon.LOG.info("sourcePartyHealth: " + sourcePartyHealth);
        // ModCommon.LOG.info("sourcePartyMaxHealth: " + sourcePartyMaxHealth);
        // ModCommon.LOG.info("targetPartyHealth: " + targetPartyHealth);
        // ModCommon.LOG.info("targetPartyMaxHealth: " + targetPartyMaxHealth);
        // ModCommon.LOG.info("sourcePartyStatBoosts: " + sourcePartyStatBoosts);
        // ModCommon.LOG.info("targetPartyStatBoosts: " + targetPartyStatBoosts);
        // ModCommon.LOG.info("sourcePartyStatusEffects: " + sourcePartyStatusEffects);
        // ModCommon.LOG.info("targetPartyStatusEffects: " + targetPartyStatusEffects);
        // ModCommon.LOG.info("sourceAverageLevel: " + sourceAverageLevel);
        // ModCommon.LOG.info("targetAverageLevel: " + targetAverageLevel);
    }

    public static BattleState of(ActiveBattlePokemon pkmn) {
        var side1Levels = new ArrayList<Integer>();
        var side2Levels = new ArrayList<Integer>();
        var side1Boosts = new Counter();
        var side2Boosts = new Counter();
        var side1StatusEffects = new Counter();
        var side2StatusEffects = new Counter();

        Stream.of(pkmn.getActor().getSide().getActors()).map(a ->  a.getPokemonList().stream()).forEach(pkmns -> {
            pkmns.forEach(p -> {
                var boosts = p.getContextManager().get(BattleContext.Type.BOOST);
                var unboosts = p.getContextManager().get(BattleContext.Type.UNBOOST);
                var statusEffects = p.getContextManager().get(BattleContext.Type.STATUS);
                if(boosts != null) side1Boosts.value += (int)boosts.stream().filter(b -> BOOST_IDS.contains(b.getId())).count();
                if(unboosts != null) side1Boosts.value -= (int)unboosts.stream().filter(u -> BOOST_IDS.contains(u.getId())).count();
                if(statusEffects != null) side1StatusEffects.value += (int)statusEffects.stream().filter(s -> STATUS_EFFECT_IDS.contains(s.getId())).count();
                side1Levels.add(Math.min(MAX_PKMN_LEVEL, Math.max(0, p.getOriginalPokemon().getLevel())));
            });
        });

        Stream.of(pkmn.getActor().getSide().getOppositeSide().getActors()).map(a ->  a.getPokemonList().stream()).forEach(pkmns -> {
            pkmns.forEach(p -> {
                var boosts = p.getContextManager().get(BattleContext.Type.BOOST);
                var unboosts = p.getContextManager().get(BattleContext.Type.UNBOOST);
                var statusEffects = p.getContextManager().get(BattleContext.Type.STATUS);
                if(boosts != null) side2Boosts.value += (int)boosts.stream().filter(b -> BOOST_IDS.contains(b.getId())).count();
                if(unboosts != null) side2Boosts.value -= (int)unboosts.stream().filter(u -> BOOST_IDS.contains(u.getId())).count();
                if(statusEffects != null) side2StatusEffects.value += (int)statusEffects.stream().filter(s -> STATUS_EFFECT_IDS.contains(s.getId())).count();
                side2Levels.add(Math.min(MAX_PKMN_LEVEL, Math.max(0, p.getOriginalPokemon().getLevel())));
            });
        });

        return new BattleState(
            side1Levels.size(),
            side2Levels.size(),
            Stream.of(pkmn.getActor().getSide().getActors()).map(a ->  a.getPokemonList().stream().map(p -> p.getHealth()).reduce(0, BattleState::addI)).reduce(0, BattleState::addI),
            Stream.of(pkmn.getActor().getSide().getActors()).map(a ->  a.getPokemonList().stream().map(p -> p.getMaxHealth()).reduce(0, BattleState::addI)).reduce(0, BattleState::addI),
            Stream.of(pkmn.getActor().getSide().getOppositeSide().getActors()).map(a ->  a.getPokemonList().stream().map(p -> p.getHealth()).reduce(0, BattleState::addI)).reduce(0, BattleState::addI),
            Stream.of(pkmn.getActor().getSide().getOppositeSide().getActors()).map(a ->  a.getPokemonList().stream().map(p -> p.getMaxHealth()).reduce(0, BattleState::addI)).reduce(0, BattleState::addI),
            side1Boosts.value,
            side2Boosts.value,
            side1StatusEffects.value,
            side2StatusEffects.value,
            side1Levels.stream().mapToDouble(i -> i).average().orElse(0),
            side2Levels.stream().mapToDouble(i -> i).average().orElse(0)
        );
    }

    // private static double addD(double a, double b) {
    //     return a + b;
    // }

    private static int addI(int a, int b) {
        return a + b;
    }

    private static class Counter {
        public int value;
    }
}
