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

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.ModCommon;

// BattleContext.Type.FAINT
// BattleContext.Type.GRAVITY
// BattleContext.Type.HAZARD
// BattleContext.Type.ITEM
// BattleContext.Type.MISC
// BattleContext.Type.ROOM
// BattleContext.Type.SCREEN
// BattleContext.Type.SPORT
// BattleContext.Type.TAILWIND
// BattleContext.Type.TERRAIN
// BattleContext.Type.WEATHER
public final class PokeContext {
    public static enum BattleEffect {
        // TODO: 'trapping moves' are currently never added to a PokemonState (ShowdownActionResponse notices if switch is invalid)
        // TODO: other stuff
        TURN(1, Integer.MAX_VALUE), BLOCK(2), MEANLOOK(4), SPIDERWEB(8);
        private long mask;
        private int expires;

        private BattleEffect(int mask) {
            this(mask, -1);
        }

        private BattleEffect(int mask, int expires) {
            this.mask = mask;
            this.expires = expires;
        }

        public long mask() {
            return this.mask;
        }

        public int expires() {
            return this.expires;
        }
    }

    public static final class State {
        // https://pokemondb.net/glossary#def-raised
        public static boolean raised(BattlePokemon pkmn) {
            // TODO: gravity => false
            // TODO: ingrain => false
            // TODO: smackdown => false

            if(pkmn.getEffectedPokemon().getHeldItem$common().is(CobblemonItems.IRON_BALL)) {
                return false;
            }

            if(pkmn.getEffectedPokemon().getHeldItem$common().is(CobblemonItems.AIR_BALLOON)) {
                return true;
            }

            if(pkmn.getEffectedPokemon().getAbility().getName().equals("levitate")) {
                return true;
            }

            for(var t : pkmn.getEffectedPokemon().getTypes()) {
                if(t.equals(ElementalTypes.INSTANCE.getFLYING())) {
                    return true;
                }
            }
            
            // TODO: steel + magnetrise => true
            // TODO: telekinesis => true

            return false;
        }

        public static boolean magnetic(BattlePokemon pkmn) {
            for(var t : pkmn.getEffectedPokemon().getTypes()) {
                if(t.equals(ElementalTypes.INSTANCE.getSTEEL())) {
                    return true;
                }
            }
            
            return false;
        }

        public static boolean ghost(BattlePokemon pkmn) {
            for(var t : pkmn.getEffectedPokemon().getTypes()) {
                if(t.equals(ElementalTypes.INSTANCE.getGHOST())) {
                    return true;
                }
            }
            
            return false;
        }

        // meanlook: https://bulbapedia.bulbagarden.net/wiki/Mean_Look_(move)
        // block: https://bulbapedia.bulbagarden.net/wiki/Block_(move)
        // spiderweb: https://bulbapedia.bulbagarden.net/wiki/Spider_Web_(move)
        // arenatrap: https://bulbapedia.bulbagarden.net/wiki/Arena_Trap_(Ability)
        // shadowtag: https://bulbapedia.bulbagarden.net/wiki/Shadow_Tag_(Ability)
        // magnetpull: https://bulbapedia.bulbagarden.net/wiki/Magnet_Pull_(Ability)
        public static boolean trapped(BattlePokemon pkmn) {
            return (!ghost(pkmn)
                    && (BattleStates.get(pkmn.getActor().getBattle()).getPokemonState(pkmn).has(BattleEffect.BLOCK)
                    || BattleStates.get(pkmn.getActor().getBattle()).getPokemonState(pkmn).has(BattleEffect.MEANLOOK)
                    || BattleStates.get(pkmn.getActor().getBattle()).getPokemonState(pkmn).has(BattleEffect.SPIDERWEB)))
                || (!pkmn.getEffectedPokemon().getHeldItem$common().is(CobblemonItems.SHED_SHELL)
                    && ((!pkmn.getEffectedPokemon().getAbility().getName().equals("shadowtag") && pkmn.getActor().getSide().getOppositeSide().getActivePokemon().stream().filter(p -> p.isAlive()).map(p -> p.getBattlePokemon()).anyMatch(p -> p.getEffectedPokemon().getAbility().getName().equals("shadowtag")))
                    || (magnetic(pkmn) && pkmn.getActor().getSide().getOppositeSide().getActivePokemon().stream().filter(p -> p.isAlive()).map(p -> p.getBattlePokemon()).anyMatch(p -> p.getEffectedPokemon().getAbility().getName().equals("magnetpull")))
                    || (!raised(pkmn) && pkmn.getActor().getSide().getOppositeSide().getActivePokemon().stream().filter(p -> p.isAlive()).map(p -> p.getBattlePokemon()).anyMatch(p -> p.getEffectedPokemon().getAbility().getName().equals("arenatrap")))));
        }
    }

    // BattleContext.Type.BOOST
    // BattleContext.Type.UNBOOST
    // range: [-1, 1], unboosts yield negative results
    public static final class Boosts {
        public static double atk(BattlePokemon pkmn) { return get(pkmn, "atk"); }
        public static double def(BattlePokemon pkmn) { return get(pkmn, "def"); }
        public static double spa(BattlePokemon pkmn) { return get(pkmn, "spa"); }
        public static double spd(BattlePokemon pkmn) { return get(pkmn, "spd"); }
        public static double spe(BattlePokemon pkmn) { return get(pkmn, "spe"); }

        public static double avg(BattlePokemon pkmn) {
            return (atk(pkmn) + def(pkmn) + spa(pkmn) + spd(pkmn) + spd(pkmn))/5;
        }

        private static double get(BattlePokemon pkmn, String statId) {
            var bctx = pkmn.getContextManager().get(BattleContext.Type.BOOST);
            var uctx = pkmn.getContextManager().get(BattleContext.Type.UNBOOST);
            return ((bctx == null ? 0 : bctx.stream().filter(c -> c.getId().equals(statId)).count())
                - (uctx == null ? 0 : uctx.stream().filter(c -> c.getId().equals(statId)).count()))/6.0;
        }
    }

    // BattleContext.Type.STATUS
    public static final class Statuses {
        public static boolean psn(BattlePokemon pkmn) { return has(pkmn, "psn"); }
        public static boolean tox(BattlePokemon pkmn) { return has(pkmn, "tox"); }
        public static boolean par(BattlePokemon pkmn) { return has(pkmn, "par"); }
        public static boolean slp(BattlePokemon pkmn) { return has(pkmn, "slp"); }
        public static boolean frz(BattlePokemon pkmn) { return has(pkmn, "frz"); }
        public static boolean brn(BattlePokemon pkmn) { return has(pkmn, "brn"); }

        public static boolean any(BattlePokemon pkmn) {
            var ctx = pkmn.getContextManager().get(BattleContext.Type.STATUS);
            return ctx != null && !ctx.isEmpty();
        }

        private static boolean has(BattlePokemon pkmn, String effId) {
            var ctx = pkmn.getContextManager().get(BattleContext.Type.STATUS);
            return ctx != null && ctx.stream().filter(bc -> bc.getId().equals(effId)).findFirst().isPresent();
        }
    }

    // BattleContext.Type.VOLATILE
    public static final class Volatiles {
        public static boolean attract(BattlePokemon pkmn) { return has(pkmn, "attract"); }
        public static boolean confusion(BattlePokemon pkmn) { return has(pkmn, "confusion"); }
        public static boolean cursed(BattlePokemon pkmn) { return has(pkmn, "cursed"); }
        public static boolean leech(BattlePokemon pkmn) { return has(pkmn, "leech"); }

        public static boolean any(BattlePokemon pkmn) {
            var ctx = pkmn.getContextManager().get(BattleContext.Type.VOLATILE);
            return ctx != null && !ctx.isEmpty();
        }

        private static boolean has(BattlePokemon pkmn, String volId) {
            var ctx = pkmn.getContextManager().get(BattleContext.Type.STATUS);
            return ctx != null && ctx.stream().filter(bc -> bc.getId().equals(volId)).findFirst().isPresent();
        }
    }

    // debug function
    public static void dump(BattlePokemon pkmn) {
        ModCommon.LOG.info("CONTEXTS DUMP " + pkmn.getName().getString() + ", turn: " + BattleStates.get(pkmn.getActor().getBattle()).getPokemonState(pkmn).age(BattleEffect.TURN));

        pkmn.getContextManager().getBuckets().forEach((t, c) -> {
            c.forEach(bc -> ModCommon.LOG.info(String.format(
                " - id: %s, turn: %d, type: %s, damaging: %b, exclusive: %b",
                bc.getId(), bc.getTurn(), bc.getType().name(), bc.getType().getDamaging(), bc.getType().getExclusive())));
        });
    }
}
