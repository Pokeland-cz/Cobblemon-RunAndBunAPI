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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;

/**
 * Utility class to keep track of information throughout a battle.
 */
public final class BattleStates {
    public static class ActorState {
        private final Set<ActiveBattlePokemon> responses = new HashSet<>();
        private final Set<String> gimmicks = new HashSet<>();
        private int turn;

        public void addGimmick(String showdownId) {
            this.gimmicks.add(showdownId);
        }

        public boolean hasGimmick(String showdownId) {
            return this.gimmicks.contains(showdownId);
        }

        public void addResponse(ActiveBattlePokemon pkmn) {
            responses.add(pkmn);
        }

        public void removeResponse(ActiveBattlePokemon pkmn) {
            this.responses.remove(pkmn);
        }

        public boolean hasResponse(ActiveBattlePokemon pkmn) {
            return this.responses.contains(pkmn);
        }

        public void nextTurn() {
            this.responses.clear();
            this.turn++;
        }

        public int getTurn() {
            return this.turn;
        }
    }

    public static class PokemonState {
        private static final int[] ZERO = new int[]{0};
        private Map<BattleEffects.Custom, int[]> turnEffectCounters = new HashMap<>();
        private long effects;

        private PokemonState() {
        }

        public void nextTurn() {
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
        private Map<BattleActor, ActorState> actorStates = new HashMap<>();

        private BattleState() {
        }

        public PokemonState getPokemonState(BattlePokemon pkmn) {
            return this.pokemonStates.computeIfAbsent(pkmn, k -> new PokemonState());
        }

        public ActorState getActorState(BattleActor actor) {
            return this.actorStates.computeIfAbsent(actor, k -> new ActorState());
        }
    }

    public static BattleState get(PokemonBattle battle) {
        if(!battle.getEnded()) {
            return BattleStates.STATES.computeIfAbsent(battle.getBattleId(), k -> new BattleState());
        }

        return new BattleState();
    }

    public static void notifyPokemonFainted(PokemonBattle battle, BattlePokemon pkmn) {
        if(BattleStates.STATES.containsKey(battle.getBattleId())) {
            BattleStates.get(battle).getActorState(pkmn.actor).removeResponse(pkmn.actor.getActivePokemon().stream().filter(ap -> ap.getBattlePokemon() == pkmn).findFirst().get());
        }
    }

    public static void notifyBattleEnded(PokemonBattle battle) {
        BattleStates.STATES.remove(battle.getBattleId());
    }

    private static Map<UUID, BattleState> STATES = new HashMap<>();
    private BattleStates() {}
}
