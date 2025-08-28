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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.ModCommon;

import net.minecraft.server.MinecraftServer;

/**
 * Utility class to keep track of information throughout a battle.
 */
public final class BattleStates {
    public static class ActorState {
        // private final Map<ActiveBattlePokemon, ShowdownActionResponse> responses = new HashMap<>();
        // private final Set<ActiveBattlePokemon> responses = new HashSet<>();
        private final Map<ActiveBattlePokemon, ShowdownActionResponse> responses = new HashMap<>();
        private final Set<BattlePokemon> switchChoices = new HashSet<>();
        private final Set<String> gimmicks = new HashSet<>();
        private int turn;

        public void addGimmick(String showdownId) {
            this.gimmicks.add(showdownId);
        }

        public boolean hasGimmick(String showdownId) {
            return this.gimmicks.contains(showdownId);
        }

        // public void setResponse(ActiveBattlePokemon pkmn, ShowdownActionResponse response) {
        //     if(response == null) {
        //         responses.remove(pkmn);
        //     } else {
        //         responses.put(pkmn, response);
        //     }
        // }

        // public ShowdownActionResponse getResponse(ActiveBattlePokemon pkmn) {
        //     return this.responses.get(pkmn);
        // }

        public void setWillBeSwitchedIn(BattlePokemon pkmn) {
            this.setWillBeSwitchedIn(pkmn, true);
        }

        public void setWillBeSwitchedIn(BattlePokemon pkmn, boolean value) {
            if(value) {
                this.switchChoices.add(pkmn);
            } else {
                this.switchChoices.remove(pkmn);
            }
        }

        public boolean willBeSwitchedIn(BattlePokemon pkmn) {
            return this.switchChoices.contains(pkmn);
        }

        // public void addResponse(ActiveBattlePokemon pkmn) {
        //     this.responses.add(pkmn);
        // }

        // public void removeResponse(ActiveBattlePokemon pkmn) {
        //     this.responses.remove(pkmn);
        // }

        public void setResponse(ActiveBattlePokemon pkmn, ShowdownActionResponse response) {
            if(response != null) {
                this.responses.put(pkmn, response);
            } else {
                this.responses.remove(pkmn);
            }
        }

        public ShowdownActionResponse getResponse(ActiveBattlePokemon pkmn) {
            return this.responses.get(pkmn);
        }

        public boolean hasResponse(ActiveBattlePokemon pkmn) {
            return this.responses.containsKey(pkmn);
        }

        // public void nextUpkeep() {
        //     this.switchChoices.clear();
        // }

        public void nextRequest() {
            this.switchChoices.clear();
            this.responses.clear(); // will it work?
        }

        // deprecated ?
        public void nextTurn() {
            // this.switchChoices.clear();
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
        private Pokemon transformation;
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

        public void setTransformation(BattlePokemon target) {
            this.transformation = target.getEffectedPokemon().createPokemonProperties(PokemonPropertyExtractor.ALL).create();
        }

        public Pokemon getTransformation() {
            return this.transformation;
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

        public void onSwitch(BattlePokemon target) {
            var targetState = BattleStates.get(target.actor.battle).getPokemonState(target);

            for(var e : BattleEffects.Custom.values()) {
                if(e.isVolatile()) {                    
                    if((targetState.effects & e.mask()) != 0) {
                        targetState.effects ^= e.mask();
                        targetState.turnEffectCounters.remove(e);
                    }
                }

                if(e.canBePassed() && (this.effects & e.mask()) != 0) {
                    targetState.effects |= e.mask();

                    if(e.expires() >= 0) {
                        targetState.turnEffectCounters.put(e, this.turnEffectCounters.get(e));
                    }
                }

                if(e.isVolatile()) {
                    if((this.effects & e.mask()) != 0) {
                        this.effects ^= e.mask();
                        this.turnEffectCounters.remove(e);
                    }
                }
            }

            this.transformation = null;
            targetState.transformation = null;
        }
    }

    public static class BattleState {
        private Map<BattlePokemon, PokemonState> pokemonStates = new HashMap<>();
        private Map<BattleActor, ActorState> actorStates = new HashMap<>();

        private Queue<Runnable> execLaterHandlers = new LinkedList<>();
        private int forcedSwitches;

        private BattleState() {
        }

        // public int getForcedSwitches() {
        //     return this.forcedSwitches;
        // }

        // public void incForcedSwitches() {
        //     this.forcedSwitches++;
        // }

        // public void decForcedSwitches() {
        //     if(this.forcedSwitches > 0 && --this.forcedSwitches == 0) {
        //         this.execAll();
        //     }
        // }

        // public void execAfterForceSwitch(MinecraftServer server, Runnable r) {
        //     this.execAfterForceSwitch(server, r, 0);
        // }

        // public void execAfterForceSwitch(MinecraftServer server, Runnable r, int timeout) {
        //     if(this.forcedSwitches > 0) {
        //         ModCommon.LOG.info("EXEC LATER");
        //         this.execLaterHandlers.offer(() -> {
        //             // try {Thread.sleep(timeout);} catch (InterruptedException e) {}
        //             server.execute(r);
        //         });
        //     } else {
        //         ModCommon.LOG.info("EXEC NOW");
        //         server.execute(r);
        //     }
        // }

        // private void execAll() {
        //     ModCommon.LOG.info("AFTER SWITCHES (" + this.forcedSwitches + ")");

        //     while(!this.execLaterHandlers.isEmpty()) {
        //         this.execLaterHandlers.poll().run();
        //         // new Thread(this.execLaterHandlers.poll()).start();
        //     }
        // }

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

    public static Pokemon getTransformationOrEffected(BattlePokemon pkmn) {
        var transformation = BattleStates.get(pkmn.actor.battle).getPokemonState(pkmn).getTransformation();
        return transformation != null ? transformation : pkmn.getEffectedPokemon();
    }

    // public static void notifyPokemonFainted(PokemonBattle battle, BattlePokemon pkmn) {
    //     if(BattleStates.STATES.containsKey(battle.getBattleId())) {
    //         BattleStates.get(battle).getActorState(pkmn.actor).removeResponse(pkmn.actor.getActivePokemon().stream().filter(ap -> ap.getBattlePokemon() == pkmn).findFirst().get());
    //     }
    // }

    public static void notifyBattleEnded(PokemonBattle battle) {
        BattleStates.STATES.remove(battle.getBattleId());
    }

    private static Map<UUID, BattleState> STATES = new HashMap<>();
    private BattleStates() {}
}
