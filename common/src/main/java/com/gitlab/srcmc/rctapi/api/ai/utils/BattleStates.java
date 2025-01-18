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
import java.util.UUID;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;

/**
 * Utility class to keep track of information throughout a battle.
 */
public final class BattleStates {
    public static class State {
        private Map<BattlePokemon, BattlePokemon> switchReplacements = new HashMap<>();
        private Map<ActiveBattlePokemon, Boolean> turnStates = new HashMap<>();
        private State() {}

        public boolean isTurn(ActiveBattlePokemon pkmn) {
            return this.turnStates.getOrDefault(pkmn, false);
        }
    }

    public static State get(PokemonBattle battle) {
        if(!battle.getEnded()) {
            return BattleStates.STATES.computeIfAbsent(battle.getBattleId(), k -> new State());
        }

        return new State();
    }

    public static void setTurn(ActiveBattlePokemon pkmn, boolean turn) {
        var bs = BattleStates.get(pkmn.getBattle());

        if(turn && pkmn.hasPokemon()) {
            bs.switchReplacements.remove(pkmn.getBattlePokemon());
        }
        
        bs.turnStates.put(pkmn, turn);
    }

    public static void setWillBeSwitchedInFor(BattlePokemon in, ActiveBattlePokemon out) {
        if(out.hasPokemon()) {
            // store the choice to revert the switch state in case the active pokemon dies
            BattleStates.get(out.getBattle()).switchReplacements.put(out.getBattlePokemon(), in);
            out.getBattlePokemon().setWillBeSwitchedIn(false);
        }

        in.setWillBeSwitchedIn(true);
    }

    public static void notifyPokemonFainted(PokemonBattle battle, BattlePokemon pkmn) {
        if(BattleStates.STATES.containsKey(battle.getBattleId())) {
            var replacement = BattleStates.STATES
                .get(battle.getBattleId())
                .switchReplacements.remove(pkmn);

            if(replacement != null) {
                replacement.setWillBeSwitchedIn(false);
            }
        }
    }

    public static void notifyBattleEnded(PokemonBattle battle) {
        BattleStates.STATES.remove(battle.getBattleId());
    }

    private static Map<UUID, State> STATES = new HashMap<>();
    private BattleStates() {}
}
