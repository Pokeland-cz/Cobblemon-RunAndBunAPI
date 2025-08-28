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
// BattleContext.Type.ITEM
// BattleContext.Type.MISC
// BattleContext.Type.SPORT
public class BattleEffects {
    // keeps track of custom effects or effects that I could not find anywhere else
    public static enum Custom {
        TURN(1, Integer.MAX_VALUE, true),
        BLOCK(1<<1, -1, true),
        MEANLOOK(1<<2, -1, true),
        SPIDERWEB(1<<3, -1, true),
        ITEM_ENDED(1<<4),
        WISH(1<<5, 1, true, true),
        PROTECT(1<<6, 1, true),
        MEGA(1<<7),
        TERA(1<<8),
        DYNAMAX(1<<9),
        ZMOVE(1<<10);

        private long mask;

        // number of turns the effect will stay
        private int expires;

        // volatile effects are removed on switch
        private boolean isVolatile;

        // effect will be passed down to switched pokemon
        private boolean canBePassed;

        private Custom(int mask) {
            this(mask, -1);
        }

        private Custom(int mask, int expires) {
            this(mask, expires, false);
        }

        private Custom(int mask, int expires, boolean isVolatile) {
            this(mask, expires, isVolatile, false);
        }

        private Custom(int mask, int expires, boolean isVolatile, boolean canBePassed) {
            this.mask = mask;
            this.expires = expires;
            this.isVolatile = isVolatile;
            this.canBePassed = canBePassed;
        }

        public long mask() {
            return this.mask;
        }

        public int expires() {
            return this.expires;
        }

        public boolean isVolatile() {
            return this.isVolatile;
        }

        public boolean canBePassed() {
            return this.canBePassed;
        }
    }

    public class Pokemon {
        // range: [-1, 1], unboosts yield negative results
        public static final class Boost {
            public static double atk(BattlePokemon pkmn) { return get(pkmn, "atk"); }
            public static double def(BattlePokemon pkmn) { return get(pkmn, "def"); }
            public static double spa(BattlePokemon pkmn) { return get(pkmn, "spa"); }
            public static double spd(BattlePokemon pkmn) { return get(pkmn, "spd"); }
            public static double spe(BattlePokemon pkmn) { return get(pkmn, "spe"); }
            public static double evasion(BattlePokemon pkmn) { return get(pkmn, "evasion"); }

            public static double avg(BattlePokemon pkmn) {
                return (atk(pkmn) + def(pkmn) + spa(pkmn) + spd(pkmn) + spd(pkmn) + evasion(pkmn))/6;
            }

            private static double get(BattlePokemon pkmn, String statId) {
                var bctx = pkmn.getContextManager().get(BattleContext.Type.BOOST);
                var uctx = pkmn.getContextManager().get(BattleContext.Type.UNBOOST);
                return ((bctx == null ? 0 : bctx.stream().filter(c -> c.getId().equals(statId)).count())
                    - (uctx == null ? 0 : uctx.stream().filter(c -> c.getId().equals(statId)).count()))/6.0;
            }
        }

        public class Volatile {
            public static boolean attract(BattlePokemon pkmn) { return has(pkmn, "attract"); }
            public static boolean confusion(BattlePokemon pkmn) { return has(pkmn, "confusion"); }
            public static boolean cursed(BattlePokemon pkmn) { return has(pkmn, "cursed"); }
            public static boolean leech(BattlePokemon pkmn) { return has(pkmn, "leech"); }
            public static boolean ingrain(BattlePokemon pkmn) { return has(pkmn, "ingrain"); }
            public static boolean aquaring(BattlePokemon pkmn) { return has(pkmn, "aquaring"); }
            public static boolean smackdown(BattlePokemon pkmn) { return has(pkmn, "smackdown"); }
            public static boolean telekinesis(BattlePokemon pkmn) { return has(pkmn, "telekinesis"); }
            public static boolean magnetrise(BattlePokemon pkmn) { return has(pkmn, "magnetrise"); }
            public static boolean yawn(BattlePokemon pkmn) { return has(pkmn, "yawn"); }
            public static boolean taunt(BattlePokemon pkmn) { return has(pkmn, "taunt"); }

            public static boolean any(BattlePokemon pkmn) {
                var ctx = pkmn.getContextManager().get(BattleContext.Type.VOLATILE);
                return ctx != null && !ctx.isEmpty();
            }

            private static boolean has(BattlePokemon pkmn, String volId) {
                var ctx = pkmn.getContextManager().get(BattleContext.Type.VOLATILE);
                return ctx != null && ctx.stream().filter(bc -> bc.getId().equals(volId)).findFirst().isPresent();
            }
        }

        public class Status {
            public static boolean psn(BattlePokemon pkmn) { return has(pkmn, "psn"); }
            public static boolean tox(BattlePokemon pkmn) { return has(pkmn, "tox"); }
            public static boolean par(BattlePokemon pkmn) { return has(pkmn, "par"); }
            public static boolean slp(BattlePokemon pkmn) { return has(pkmn, "slp"); }
            public static boolean frz(BattlePokemon pkmn) { return has(pkmn, "frz"); }
            public static boolean brn(BattlePokemon pkmn) {return has(pkmn, "brn"); }

            public static boolean any(BattlePokemon pkmn) {
                var ctx = pkmn.getContextManager().get(BattleContext.Type.STATUS);
                return ctx != null && !ctx.isEmpty();
            }

            private static boolean has(BattlePokemon pkmn, String effId) {
                var ctx = pkmn.getContextManager().get(BattleContext.Type.STATUS);
                return ctx != null && ctx.stream().filter(bc -> bc.getId().equals(effId)).findFirst().isPresent();
            }
        }

        // special states based on (combinations of) other conditions
        public static final class State {
            // https://pokemondb.net/glossary#def-raised
            public static boolean raised(BattlePokemon pkmn) {
                if(BattleEffects.Field.Gravity.gravity(pkmn)) {
                    return false;
                }

                if(Volatile.ingrain(pkmn)) {
                    return false;
                }

                if(Volatile.smackdown(pkmn)) {
                    return false;
                }

                if(pkmn.getEffectedPokemon().heldItem().is(CobblemonItems.IRON_BALL) && !BattleStates.get(pkmn.actor.battle).getPokemonState(pkmn).has(BattleEffects.Custom.ITEM_ENDED)) {
                    return false;
                }

                if(pkmn.getEffectedPokemon().heldItem().is(CobblemonItems.AIR_BALLOON) && !BattleStates.get(pkmn.actor.battle).getPokemonState(pkmn).has(BattleEffects.Custom.ITEM_ENDED)) {
                    return true;
                }

                if(BattleStates.getTransformationOrEffected(pkmn).getAbility().getName().equals("levitate")) {
                    return true;
                }

                for(var t : BattleStates.getTransformationOrEffected(pkmn).getTypes()) {
                    if(t.equals(ElementalTypes.INSTANCE.getFLYING())) {
                        return true;
                    }
                }

                if(Volatile.magnetrise(pkmn)) {
                    return true;
                }

                return Volatile.telekinesis(pkmn);
            }

            public static boolean magnetic(BattlePokemon pkmn) {
                return TypeChart.is(pkmn, TypeChart.STEEL);
            }

            // TODO: infestation ?
            // meanlook: https://bulbapedia.bulbagarden.net/wiki/Mean_Look_(move)
            // block: https://bulbapedia.bulbagarden.net/wiki/Block_(move)
            // spiderweb: https://bulbapedia.bulbagarden.net/wiki/Spider_Web_(move)
            // arenatrap: https://bulbapedia.bulbagarden.net/wiki/Arena_Trap_(Ability)
            // shadowtag: https://bulbapedia.bulbagarden.net/wiki/Shadow_Tag_(Ability)
            // magnetpull: https://bulbapedia.bulbagarden.net/wiki/Magnet_Pull_(Ability)
            public static boolean trapped(BattlePokemon pkmn) {
                return (!TypeChart.is(pkmn, TypeChart.GHOST)
                        && (BattleStates.get(pkmn.getActor().getBattle()).getPokemonState(pkmn).has(BattleEffects.Custom.BLOCK)
                        || BattleStates.get(pkmn.getActor().getBattle()).getPokemonState(pkmn).has(BattleEffects.Custom.MEANLOOK)
                        || BattleStates.get(pkmn.getActor().getBattle()).getPokemonState(pkmn).has(BattleEffects.Custom.SPIDERWEB)))
                    || (!pkmn.getEffectedPokemon().getHeldItem$common().is(CobblemonItems.SHED_SHELL)
                        && ((!BattleStates.getTransformationOrEffected(pkmn).getAbility().getName().equals("shadowtag") && pkmn.getActor().getSide().getOppositeSide().getActivePokemon().stream().filter(p -> p.isAlive()).map(p -> p.getBattlePokemon()).anyMatch(p -> BattleStates.getTransformationOrEffected(p).getAbility().getName().equals("shadowtag")))
                        || (magnetic(pkmn) && pkmn.getActor().getSide().getOppositeSide().getActivePokemon().stream().filter(p -> p.isAlive()).map(p -> p.getBattlePokemon()).anyMatch(p -> BattleStates.getTransformationOrEffected(p).getAbility().getName().equals("magnetpull")))
                        || (!raised(pkmn) && pkmn.getActor().getSide().getOppositeSide().getActivePokemon().stream().filter(p -> p.isAlive()).map(p -> p.getBattlePokemon()).anyMatch(p -> BattleStates.getTransformationOrEffected(p).getAbility().getName().equals("arenatrap")))));
            }
        }
    }

    public static class Side {
        public static final class Screen {
            public static boolean auroraveil(BattlePokemon pkmn) { return has(pkmn, "auroraveil"); }
            public static boolean lightscreen(BattlePokemon pkmn) { return has(pkmn, "lightscreen"); }
            public static boolean reflect(BattlePokemon pkmn) { return has(pkmn, "reflect"); }

            private static boolean has(BattlePokemon pkmn, String screenId) {
                var ctx = pkmn.actor.getSide().getContextManager().get(BattleContext.Type.SCREEN);
                return ctx != null && ctx.stream().anyMatch(bc -> bc.getId().equals(screenId));
            }
        }

        // https://bulbapedia.bulbagarden.net/wiki/List_of_moves_that_cause_entry_hazards
        public static final class Hazard {
            public static int spikes(BattlePokemon pkmn) { return get(pkmn, "spikes"); } // spikes
            public static int stealthrock(BattlePokemon pkmn) { return get(pkmn, "stealthrock"); } // stealthrock (pointedstones)
            public static int toxicspikes(BattlePokemon pkmn) { return get(pkmn, "toxicspikes"); } // toxicspikes (poisonspikes)
            public static int stickyweb(BattlePokemon pkmn) { return get(pkmn, "stickyweb"); } // stickyweb
            public static int sharpsteel(BattlePokemon pkmn) { return get(pkmn, "sharpsteel"); } // (g-max) steelsurge

            private static int get(BattlePokemon pkmn, String hazardId) {
                var ctx = pkmn.actor.getSide().getContextManager().get(BattleContext.Type.HAZARD);
                return ctx != null ? (int)ctx.stream().filter(bc -> bc.getId().equals(hazardId)).count() : 0;
            }
        }

        public static final class Tailwind {
            public static boolean tailwind(BattlePokemon pkmn) { return has(pkmn, "tailwind"); }

            private static boolean has(BattlePokemon pkmn, String tailwindId) {
                var ctx = pkmn.actor.getSide().getContextManager().get(BattleContext.Type.TAILWIND);
                return ctx != null && ctx.stream().anyMatch(bc -> bc.getId().equals(tailwindId));
            }
        }
    }

    public static class Field {
        // https://bulbapedia.bulbagarden.net/wiki/Terrain#In_the_core_series_games
        public static final class Terrain {
            public static boolean electricterrain(BattlePokemon pkmn) { return has(pkmn, "electricterrain"); }
            public static boolean grassyterrain(BattlePokemon pkmn) { return has(pkmn, "grassyterrain"); }
            public static boolean mistyterrain(BattlePokemon pkmn) { return has(pkmn, "mistyterrain"); }
            public static boolean psychicterrain(BattlePokemon pkmn) { return has(pkmn, "psychicterrain"); }

            private static boolean has(BattlePokemon pkmn, String terrainId) {
                var ctx = pkmn.actor.battle.getContextManager().get(BattleContext.Type.TERRAIN);
                return ctx != null && ctx.stream().anyMatch(bc -> bc.getId().equals(terrainId));
            }
        }

        // https://bulbapedia.bulbagarden.net/wiki/Weather#List_of_weather
        public static final class Weather {
            public static boolean harshsunlight(BattlePokemon pkmn) { return has(pkmn, "harshsunlight"); }
            public static boolean rain(BattlePokemon pkmn) { return has(pkmn, "rain"); }
            public static boolean sandstorm(BattlePokemon pkmn) { return has(pkmn, "sandstorm"); }
            public static boolean hail(BattlePokemon pkmn) { return has(pkmn, "hail"); }
            public static boolean snow(BattlePokemon pkmn) { return has(pkmn, "snow"); }
            public static boolean fog(BattlePokemon pkmn) { return has(pkmn, "fog"); }
            public static boolean extremelyharshsunlight(BattlePokemon pkmn) { return has(pkmn, "extremelyharshsunlight"); }
            public static boolean heavyrain(BattlePokemon pkmn) { return has(pkmn, "heavyrain"); }
            public static boolean strongwinds(BattlePokemon pkmn) { return has(pkmn, "strongwinds"); }
            public static boolean shadowaura(BattlePokemon pkmn) { return has(pkmn, "shadowaura"); }

            private static boolean has(BattlePokemon pkmn, String weatherId) {
                var ctx = pkmn.actor.battle.getContextManager().get(BattleContext.Type.WEATHER);
                return ctx != null && ctx.stream().anyMatch(bc -> bc.getId().equals(weatherId));
            }
        }

        public static final class Room {
            public static boolean trickroom(BattlePokemon pkmn) { return has(pkmn, "trickroom"); }

            private static boolean has(BattlePokemon pkmn, String roomId) {
                var ctx = pkmn.actor.battle.getContextManager().get(BattleContext.Type.ROOM);
                return ctx != null && ctx.stream().anyMatch(bc -> bc.getId().equals(roomId));
            }
        }

        public static final class Gravity {
            public static boolean gravity(BattlePokemon pkmn) { return has(pkmn, "gravity"); }
            
            private static boolean has(BattlePokemon pkmn, String gravityId) {
                var ctx = pkmn.actor.battle.getContextManager().get(BattleContext.Type.GRAVITY);
                return ctx != null && ctx.stream().anyMatch(bc -> bc.getId().equals(gravityId));
            }
        }
    }

    // debug function
    public static void dump(BattlePokemon pkmn) {
        Debug.log(2, () -> ModCommon.LOG.info("CONTEXTS DUMP "
            + pkmn.getName().getString() + ", turn: "
            + BattleStates.get(pkmn.getActor().getBattle()).getPokemonState(pkmn).age(BattleEffects.Custom.TURN)));

        Debug.log(2, () -> {
            ModCommon.LOG.info("CONTEXT TYPES:");
            pkmn.getContextManager().getBuckets().forEach((t, c) -> {
                c.forEach(bc -> ModCommon.LOG.info(String.format(
                    " - id: %s, turn: %d, type: %s, damaging: %b, exclusive: %b",
                    bc.getId(), bc.getTurn(), bc.getType().name(), bc.getType().getDamaging(), bc.getType().getExclusive())));
            });

            ModCommon.LOG.info("SIDE CONTEXT:");
            pkmn.actor.getSide().getContextManager().getBuckets().forEach((t, c) -> {
                c.forEach(bc -> ModCommon.LOG.info(String.format(
                    " - id: %s, turn: %d, type: %s, damaging: %b, exclusive: %b",
                    bc.getId(), bc.getTurn(), bc.getType().name(), bc.getType().getDamaging(), bc.getType().getExclusive())));
            });

            ModCommon.LOG.info("BATTLE CONTEXT:");
            pkmn.actor.battle.getContextManager().getBuckets().forEach((t, c) -> {
                c.forEach(bc -> ModCommon.LOG.info(String.format(
                    " - id: %s, turn: %d, type: %s, damaging: %b, exclusive: %b",
                    bc.getId(), bc.getTurn(), bc.getType().name(), bc.getType().getDamaging(), bc.getType().getExclusive())));
            });
        });

        Debug.log(3, () -> {
            ModCommon.LOG.info("MAJOR ACTIONS:");
            pkmn.actor.battle.getMajorBattleActions().values().forEach(msg -> ModCommon.LOG.info(" - " + msg.getRawMessage()));

            ModCommon.LOG.info("MINOR ACTIONS:");
            pkmn.actor.battle.getMinorBattleActions().values().forEach(msg -> ModCommon.LOG.info(" - " + msg.getRawMessage()));
        });
    }
}
