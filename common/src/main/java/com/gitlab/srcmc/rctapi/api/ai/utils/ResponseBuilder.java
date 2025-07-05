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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.DefaultActionResponse;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Custom;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager.TrainerEntityBattleActor;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

public class ResponseBuilder {
    private Supplier<Stream<BattlePokemon>> switchCandidates = Stream::empty;
    private Supplier<Stream<Pair<BagItem, BattlePokemon>>> itemCandidates = Stream::empty;
    private Supplier<Stream<Pair<InBattleMove, Targetable>>> moveCandidates = Stream::empty;
    private List<Choice<ShowdownActionResponse>> choices = new ArrayList<>();
    private Random rng = new Random(0);

    private ActiveBattlePokemon pkmn;
    private ShowdownMoveset moveset;
    private boolean forceSwitch, forceMove, mustChoose;
    private double margin;

    public static ResponseBuilder create(ActiveBattlePokemon pkmn, ShowdownMoveset moveset, boolean forceSwitch) {
        var actorSt = BattleStates.get(pkmn.getBattle()).getActorState(pkmn.getActor());
        var builder = new ResponseBuilder();
        builder.mustChoose = pkmn.getActor().getMustChoose();
        builder.forceSwitch = forceSwitch;
        builder.forceMove = false;
        builder.moveset = moveset;
        builder.pkmn = pkmn;

        if(!actorSt.hasResponse(pkmn)) {
            if(!builder.forceSwitch && builder.mustChoose && pkmn.hasPokemon()) {
                // all possible move usages
                if(builder.moveset != null) {
                    if(builder.moveset.moves.stream().findFirst().isPresent()) {
                        Stream<InBattleMove> stream;

                        if(builder.moveset.moves.stream().anyMatch(InBattleMove::mustBeUsed)) {
                            stream = builder.moveset.moves.stream().filter(InBattleMove::mustBeUsed);
                            builder.forceMove = true;
                        } else {
                            stream = builder.moveset.moves.stream().filter(InBattleMove::canBeUsed);
                        }

                        builder.moveCandidates = () -> stream
                            // .flatMap(mv -> {
                            //     return Stream.of(mv); // TODO: replace here with gimmick moves for evaluation?
                            // })
                            .flatMap(mv -> mv.getTargets(pkmn) == null || mv.getTargets(pkmn).isEmpty()
                                ? Stream.of(new Pair<>(mv, (Targetable)null))
                                : mv.getTargets(pkmn).stream().map(t -> new Pair<>(mv, t)));
                    }
                }

                if(!builder.forceMove) {
                    // all possible item usages
                    if(pkmn.getActor().canFitForcedAction() && pkmn.getActor() instanceof TrainerEntityBattleActor actor) {
                        builder.itemCandidates = () -> actor.getBag().getItems().stream()
                            .flatMap(bi -> pkmn.getActor().getPokemonList().stream()
                                .filter(p -> bi.canUse(pkmn.getBattle(), p))
                                .map(p -> new Pair<>(bi, p)));
                    }
                }
            }

            if(!pkmn.hasPokemon() || !builder.forceMove) {
                // all possible switches
                builder.switchCandidates = () -> pkmn.getActor()
                    .getPokemonList().stream()
                    .filter(BattlePokemon::canBeSentOut);
            }

            actorSt.addResponse(pkmn);
        } else if(pkmn.isAlive() && builder.mustChoose && builder.forceSwitch) {
            // all possible switches
            builder.switchCandidates = () -> pkmn.getActor()
                .getPokemonList().stream()
                .filter(BattlePokemon::canBeSentOut);

            actorSt.addResponse(pkmn);
        }
        
        return builder;
    }

    public ResponseBuilder suggestSwitches(Function<Stream<BattlePokemon>, Stream<Choice<BattlePokemon>>> consumer) {
        consumer.apply(this.switchCandidates.get()).forEach(choice -> {
            this.choices.add(new Choice<>(
                choice.name, new SwitchActionResponse(choice.value.getUuid()), choice.weight, () -> {
                    choice.value.setWillBeSwitchedIn(true);

                    if(this.pkmn.hasPokemon()) {
                        this.pkmn.getBattlePokemon().setWillBeSwitchedIn(false);
                    }
                }));
        });
        
        return this;
    }

    public ResponseBuilder suggestItems(Function<Stream<Pair<BagItem, BattlePokemon>>, Stream<Choice<Pair<BagItem, BattlePokemon>>>> consumer) {
        if(this.pkmn.getActor() instanceof TrainerEntityBattleActor actor) {
            consumer.apply(this.itemCandidates.get()).forEach(choice -> {
                this.choices.add(new Choice<>(choice.name,
                    new ForcePassActionResponse(), choice.weight, () -> {
                        var item = choice.value.first;
                        var pkmn = choice.value.second;
                        this.pkmn.getActor().forceChoose(new BagItemActionResponse(actor.getBag().use(item), pkmn, pkmn.getUuid().toString()));
                    }, true));
            });
        }

        return this;
    }

    public ResponseBuilder suggestMoves(Function<Stream<Pair<InBattleMove, Targetable>>, Stream<Choice<Pair<InBattleMove, Targetable>>>> consumer) {
        var gimmick = (this.moveset != null && this.moveset.getGimmicks().size() > 0)
            ? this.moveset.getGimmicks().get(this.rng.nextInt(this.moveset.getGimmicks().size()))
            : null;
            
        Debug.log(1, () -> {
            ModCommon.LOG.info("[GIMMICKS of " + ((this.pkmn.hasPokemon() ? this.pkmn.getBattlePokemon().getName().getString() : "<dead>") + "]"));

            if(this.moveset != null) {
                this.moveset.getGimmicks().forEach(g -> ModCommon.LOG.info(" - " + g.getId() + ", " + g.name()));
            }

            if(gimmick != null) {
                ModCommon.LOG.info("ACTIVATED: " + gimmick.getId());
            }
        });

        consumer.apply(this.moveCandidates.get()).forEach(choice -> {
            var move = choice.value.first;
            var target = choice.value.second;
            var gimmickId = (gimmick != null && (!Gimmick.Z_POWER.equals(gimmick) || move.getGimmickMove() != null)) ? gimmick.getId() : null;

            this.choices.add(new Choice<>(choice.name,
                new MoveActionResponse(move.id, target != null ? target.getPNX() : null, gimmickId), choice.weight,
                () -> {
                    var battleState = BattleStates.get(this.pkmn.getBattle());
                    battleState.getActorState(this.pkmn.getActor()).addGimmick(gimmickId);

                    if(this.pkmn.hasPokemon() && gimmickId != null) {
                        if(Gimmick.DYNAMAX.getId().equals(gimmickId)) {
                            battleState.getPokemonState(this.pkmn.getBattlePokemon()).add(Custom.DYNAMAX);
                        } else if(Gimmick.MEGA_EVOLUTION.getId().equals(gimmickId)) {
                            battleState.getPokemonState(this.pkmn.getBattlePokemon()).add(Custom.MEGA);
                        } else if(Gimmick.TERASTALLIZATION.getId().equals(gimmickId)) {
                            battleState.getPokemonState(this.pkmn.getBattlePokemon()).add(Custom.TERA);
                        } else if(Gimmick.Z_POWER.getId().equals(gimmickId) || Gimmick.ULTRA_BURST.getId().equals(gimmickId)) {
                            battleState.getPokemonState(this.pkmn.getBattlePokemon()).add(Custom.ZMOVE);
                        }
                    }
                })); // TODO: getSideState()?
        });

        return this;
    }

    public ShowdownActionResponse response(Consumer<ShowdownActionResponse> consumer) {
        var choices = this.choices.stream()
            .filter(c -> c.forced || c.value.isValid(this.pkmn, this.moveset, this.forceMove))
            .toList();

        Debug.log(1, "[CHOICES OF %s]:%s",
            this.pkmn.isAlive() ? this.pkmn.getBattlePokemon().getName().getString() : "<dead>",
            ", forceMove: " + this.forceMove + ", forceSwitch: " + this.forceSwitch + ", mustChoose: " + this.mustChoose);
            
        Debug.log(1, () -> choices
            .stream().sorted()
            .forEach(ch -> ModCommon.LOG.info(String.format(" - %s: %.4f", ch.name, ch.weight))));

        var response = choices.isEmpty()
            ? this.forceMove && this.mustChoose && this.pkmn.hasPokemon()
                ? new DefaultActionResponse()
                : PassActionResponse.INSTANCE
            : getRandom(takeWithMargin(choices.stream().sorted(), this.margin), this.rng, this.margin)
                .orElse(new Choice<>("DEFAULT", new DefaultActionResponse(), 0)).pick().value;

        consumer.accept(response);
        return response;
    }

    public ShowdownActionResponse response() {
        return this.response(r -> Debug.log(1, "RESPONSE: " + r));
    }

    public ResponseBuilder margin(double margin) {
        this.margin = margin;
        return this;
    }

    public ResponseBuilder random(Random rng) {
        this.rng = rng;
        return this;
    }

    // Utility types

    public static class Pair<T, U> {
        public final T first;
        public final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }
    }

    public static class Choice<T> implements Comparable<Choice<?>> {
        public final T value;
        public final double weight;
        public final String name;
        public final boolean forced;
        private final Action onpick;

        public Choice(String name, T value, double weight) {
            this(name, value, weight, () -> {}, false);
        }

        public Choice(String name, T value, double weight, boolean forced) {
            this(name, value, weight, () -> {}, forced);
        }

        public Choice(String name, T value, double weight, Action onpick) {
            this(name, value, weight, onpick, false);
        }

        public Choice(String name, T value, double weight, Action onpick, boolean forced) {
            this.value = value;
            this.weight = weight;
            this.name = name;
            this.onpick = onpick;
            this.forced = forced;
        }

        public Choice<T> pick() {
            this.onpick.perform();
            return this;
        }

        @Override
        public int compareTo(Choice<?> other) {
            return Double.compare(this.weight, other.weight);
        }

        public static interface Action {
            void perform();            
        }
    }

    // Stream utils

    private static final int MAX_CHOICE_RNG = 16;

    private static <T> Stream<Choice<T>> takeWithMargin(Stream<Choice<T>> in, double margin) {
        double[] w = {Double.NEGATIVE_INFINITY};

        return in.takeWhile(choice -> {
            if(w[0] == Double.NEGATIVE_INFINITY) {
                w[0] = choice.weight;
            } else if(choice.weight - w[0] > margin) {
                return false;
            }
            
            return true;
        });
    }

    private static <T> Optional<Choice<T>> getRandom(Stream<Choice<T>> stream, Random rng, double margin) {
        var it = stream.iterator();
        Choice<T> c;
        
        if(it.hasNext()) {
            var next= it.next();
            var start = next.weight;
            var w = 0.0;
            var i = 1;
            c = next;

            while(it.hasNext()) {
                next = it.next();
                w = margin > 0 ? (next.weight - start)/margin : 1;

                if(rng.nextInt((++i) + (int)(w * MAX_CHOICE_RNG)) == 0) {
                    c = next;
                }
            }
        } else {
            c = null;
        }

        return Optional.ofNullable(c);
    }
}
