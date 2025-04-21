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

import java.util.Random;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext.Type;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.api.ai.utils.PokeContext.BattleEffect;

/**
 * Pokemon math utilities derived from davo899s implementation of a 'Pokemon Gen5
 * Battle AI' originally developed for CobblemonTrainers (see
 * https://github.com/davo899/CobblemonTrainers).
 */
public class PokeMath {
    private static final Random RANDOM = new Random();
    private static final String WEATHER_SUN = "sunny";
    private static final String WEATHER_RAIN = "raining";

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
        // https://bulbapedia.bulbagarden.net/wiki/Damage#Generation_V_onward
        var baseDamage = (int)(((2 * attackerLevel / 5.0 + 2) * movePower * attackerEffectiveAttack / (double) defenderEffectiveDefence)/50.0) + 2;
        if(multiTarget) baseDamage *= 0.75;
        if(parentalBond) baseDamage *= 0.25;
        if(glaiveRush) baseDamage *= 2;
        if(burn && physical && !attackerAbility.getDisplayName().equals("cobblemon.ability.guts")) baseDamage *= 0.5;
        if(attackerHasStatus && attackerAbility.getDisplayName().equals("cobblemon.ability.guts")) baseDamage *= 1.5;
        if(sun && moveType.equals(ElementalTypes.INSTANCE.getFIRE()) || rain && moveType.equals(ElementalTypes.INSTANCE.getWATER())) baseDamage *= 1.5;
        if(sun && moveType.equals(ElementalTypes.INSTANCE.getWATER()) || rain && moveType.equals(ElementalTypes.INSTANCE.getFIRE())) baseDamage *= 0.5;
        if(moveType.equals(attackerPrimaryType) || moveType.equals(attackerSecondaryType)) baseDamage *= 1.5;
        baseDamage = (int)(baseDamage * TypeChart.getEffectiveness(moveType, defenderPrimaryType, defenderSecondaryType, defenderAbility));

        return baseDamage > 0
            ? Math.max(1, baseDamage * ((RANDOM.nextDouble() * 0.15) + 0.85))
            : baseDamage;
    }

    public static boolean isStatus(InBattleMove inBattleMove) {
        var move = TypeChart.getMove(inBattleMove);
        return move.getDamageCategory().getName().equals(DamageCategories.INSTANCE.getSTATUS().getName());
    }

    public static int damage(BattlePokemon attacker, BattlePokemon defender, InBattleMove inBattleMove) {
        var move = TypeChart.getMove(inBattleMove);
        var damageCategory = move.getDamageCategory().getName();

        if(damageCategory.equals(DamageCategories.INSTANCE.getSTATUS().getName())) {
            return 0;
        }

        if(move.getName().equals("fakeout") && BattleStates.get(attacker.getActor().getBattle()).getPokemonState(attacker).age(BattleEffect.TURN) > 1) {
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

        // accuracy factor is a custom modification
        return (int)Math.ceil((acc + RANDOM.nextDouble() * (1.0 - acc)) * damage(
            attacker.getEffectedPokemon().getLevel(),
            attacker.getEffectedPokemon().getStat(isPhysicalMove ? Stats.ATTACK : Stats.SPECIAL_ATTACK),
            defender.getEffectedPokemon().getStat(isPhysicalMove ? Stats.DEFENCE : Stats.SPECIAL_DEFENCE),
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
            attacker.getEffectedPokemon().getPrimaryType(),
            attacker.getEffectedPokemon().getSecondaryType(),
            defender.getEffectedPokemon().getPrimaryType(),
            defender.getEffectedPokemon().getSecondaryType(),
            defender.getEffectedPokemon().getAbility(),
            attacker.getEffectedPokemon().getAbility()));
    }
}
