package com.gitlab.srcmc.rctapi.api.ai.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.ModCommon;

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

        if(turn) {
            bs.switchReplacements.clear();
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
        ModCommon.LOG.info("############# A POKEMON DIED: " + pkmn.getName().getString());
        if(BattleStates.STATES.containsKey(battle.getBattleId())) {
            var replacement = BattleStates.STATES
                .get(battle.getBattleId())
                .switchReplacements.remove(pkmn);

            if(replacement != null) {
                ModCommon.LOG.info("############# RESET REPLACEMENT: " + replacement.getName().getString());
                replacement.setWillBeSwitchedIn(false);
            }
        }
    }

    public static void notifyBattleEnded(PokemonBattle battle) {
        ModCommon.LOG.info("############# A BATTLE ENDED: " +
        BattleStates.STATES.remove(battle.getBattleId())
        );
    }

    private static Map<UUID, State> STATES = new HashMap<>();
    private BattleStates() {}
}
