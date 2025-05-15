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
    public static class PokemonState {
        private static final int[] ZERO = new int[]{0};
        private Map<BattleEffects.Custom, int[]> turnEffectCounters = new HashMap<>();
        private BattlePokemon switchFor;
        private PokemonState preReset;
        private long effects;

        private PokemonState() {
            this(true);
        }

        private PokemonState(boolean resettable) {
            if(resettable) {
                this.preReset = new PokemonState(false);
            }
        }

        private void copy(PokemonState other) {
            this.turnEffectCounters = new HashMap<>(other.turnEffectCounters);
            this.switchFor = other.switchFor;
            this.effects = other.effects;
        }

        private PokemonState reset() {
            this.preReset.copy(this);
            this.switchFor = null;
            this.effects  = 0;
            this.turnEffectCounters.clear();
            this.add(BattleEffects.Custom.TURN);
            return this;
        }

        private void undo() {
            this.copy(this.preReset);
        }

        private void nextTurn() {
            if(!this.has(BattleEffects.Custom.TURN)) {
                this.add(BattleEffects.Custom.TURN);
            }
            
            var it = this.turnEffectCounters.entrySet().iterator();

            while(it.hasNext()) {
                var e = it.next();

                if(e.getValue()[0]-- <= 0) {
                    this.effects ^= e.getKey().mask();
                    it.remove();
                }
            }

            this.switchFor = null;
        }

        public boolean has(BattleEffects.Custom e) {
            return (e.mask() & this.effects) != 0;
        }

        public int age(BattleEffects.Custom e) {
            return this.has(e) ? e.expires() - this.turnEffectCounters.getOrDefault(e, ZERO)[0] : 0;
        }

        public void add(BattleEffects.Custom e) {
            if(e.expires() >= 0) {
                this.turnEffectCounters.put(e, new int[]{e.expires()});
            }

            this.effects |= e.mask();
        }
    }

    public static class BattleState {
        private Map<BattlePokemon, PokemonState> pokemonStates = new HashMap<>();
        private Map<ActiveBattlePokemon, Boolean> turnStates = new HashMap<>();
        private BattleState() {}

        public boolean isTurn(ActiveBattlePokemon pkmn) {
            return this.turnStates.getOrDefault(pkmn, false);
        }

        public PokemonState getPokemonState(BattlePokemon pkmn) {
            return this.pokemonStates.computeIfAbsent(pkmn, k -> new PokemonState());
        }
    }

    public static BattleState get(PokemonBattle battle) {
        if(!battle.getEnded()) {
            return BattleStates.STATES.computeIfAbsent(battle.getBattleId(), k -> new BattleState());
        }

        return new BattleState();
    }

    public static void setTurn(ActiveBattlePokemon pkmn, boolean turn) {
        var bs = BattleStates.get(pkmn.getBattle());

        if(turn && pkmn.hasPokemon()) {
            bs.getPokemonState(pkmn.getBattlePokemon()).nextTurn();
        }
        
        bs.turnStates.put(pkmn, turn);
    }

    public static void setWillBeSwitchedFor(BattlePokemon in, ActiveBattlePokemon out) {
        var bs = BattleStates.get(out.getBattle());

        if(out.hasPokemon()) {
            // store the choice to revert the switch state in case the active pokemon dies
            var ps = bs.getPokemonState(out.getBattlePokemon());

            if(ps.switchFor != null) {
                ps.switchFor.setWillBeSwitchedIn(false);
            }

            out.getBattlePokemon().setWillBeSwitchedIn(false);
            ps.switchFor = in;
        }

        bs.getPokemonState(in).reset();
        in.setWillBeSwitchedIn(true);
    }

    public static void notifyPokemonFainted(PokemonBattle battle, BattlePokemon pkmn) {
        if(BattleStates.STATES.containsKey(battle.getBattleId())) {
            var bs = BattleStates.get(battle);
            var ps = bs.getPokemonState(pkmn);

            if(ps.switchFor != null) {
                bs.getPokemonState(ps.switchFor).undo();
                ps.switchFor.setWillBeSwitchedIn(false);
                ps.switchFor = null;
            }
        }
    }

    public static void notifyBattleEnded(PokemonBattle battle) {
        BattleStates.STATES.remove(battle.getBattleId());
    }

    private static Map<UUID, BattleState> STATES = new HashMap<>();
    private BattleStates() {}
}
