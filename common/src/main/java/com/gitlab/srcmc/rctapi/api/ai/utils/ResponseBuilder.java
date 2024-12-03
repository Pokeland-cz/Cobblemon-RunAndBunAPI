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
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager.TrainerEntityBattleActor;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

public class ResponseBuilder {
    private Supplier<Stream<BattlePokemon>> switchCandidates = Stream::empty;
    private Supplier<Stream<Pair<BagItem, BattlePokemon>>> itemCandidates = Stream::empty;
    private Supplier<Stream<Pair<InBattleMove, Targetable>>> moveCandidates = Stream::empty;
    private List<Choice<Supplier<ShowdownActionResponse>>> choices = new ArrayList<>();
    private Random rng = new Random(0);

    private ActiveBattlePokemon pkmn;
    private ShowdownMoveset moveset;
    private boolean forceSwitch, forceMove, mustChoose;
    private double margin;
    private double rdf = 3; // randomDistributionFactor >= 1

    public static ResponseBuilder create(ActiveBattlePokemon pkmn, ShowdownMoveset moveset, boolean forceSwitch) {
        var builder = new ResponseBuilder();
        builder.mustChoose = pkmn.getActor().getMustChoose();
        builder.forceSwitch = forceSwitch;
        builder.forceMove = false;
        builder.moveset = moveset;
        builder.pkmn = pkmn;

        if(!builder.forceSwitch && builder.mustChoose && pkmn.hasPokemon()) {
            // all possible move usages
            if(builder.moveset != null) {
                if(builder.moveset.moves.stream().findFirst().isPresent()) {
                    if(builder.moveset.moves.stream().anyMatch(InBattleMove::mustBeUsed)) {
                        builder.moveCandidates = () -> builder.moveset.moves.stream()
                            .filter(InBattleMove::mustBeUsed)
                            .flatMap(mv -> mv.getTargets(pkmn) == null || mv.getTargets(pkmn).isEmpty()
                                ? Stream.of(new Pair<>(mv, (Targetable)null))
                                : mv.getTargets(pkmn).stream().map(t -> new Pair<>(mv, t)));

                        builder.forceMove = true;
                    } else {
                        builder.moveCandidates = () -> builder.moveset.moves.stream()
                            .filter(InBattleMove::canBeUsed)
                            .flatMap(mv -> mv.getTargets(pkmn) == null || mv.getTargets(pkmn).isEmpty()
                                ? Stream.of(new Pair<>(mv, (Targetable)null))
                                : mv.getTargets(pkmn).stream().map(t -> new Pair<>(mv, t)));
                    }
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

        if(!builder.forceMove && (builder.forceSwitch || builder.mustChoose)) {
            // all possible switches
            builder.switchCandidates = () -> pkmn.getActor()
                .getPokemonList().stream()
                .filter(BattlePokemon::canBeSentOut);
        }

        return builder;
    }

    public ResponseBuilder suggestSwitches(Function<Stream<BattlePokemon>, Stream<Choice<BattlePokemon>>> consumer) {
        if(this.forceSwitch || this.mustChoose) {
            consumer.apply(this.switchCandidates.get()).forEach(choice -> {
                this.choices.add(new Choice<>(() -> {
                    if(this.pkmn.hasPokemon()) {
                        this.pkmn.getBattlePokemon().setWillBeSwitchedIn(false);
                    }

                    choice.value.setWillBeSwitchedIn(true);
                    return new SwitchActionResponse(choice.value.getUuid());
                }, choice.weight));
            });
        }
        
        return this;
    }

    public ResponseBuilder suggestItems(Function<Stream<Pair<BagItem, BattlePokemon>>, Stream<Choice<Pair<BagItem, BattlePokemon>>>> consumer) {
        if(this.mustChoose && this.pkmn.getActor() instanceof TrainerEntityBattleActor actor) {
            consumer.apply(this.itemCandidates.get()).forEach(choice -> {
                this.choices.add(new Choice<>(() -> {
                    var item = choice.value.first;
                    var pkmn = choice.value.second;
                    actor.forceChoose(new BagItemActionResponse(actor.getBag().use(item), pkmn, pkmn.getUuid().toString()));
                    return new ForcePassActionResponse();
                }, choice.weight));
            });
        }

        return this;
    }

    public ResponseBuilder suggestMoves(Function<Stream<Pair<InBattleMove, Targetable>>, Stream<Choice<Pair<InBattleMove, Targetable>>>> consumer) {
        if(this.forceMove || this.mustChoose) {
            consumer.apply(this.moveCandidates.get()).forEach(choice -> {
                var move = choice.value.first;
                var target = choice.value.second;
                this.choices.add(new Choice<>(() -> new MoveActionResponse(move.id, target != null ? target.getPNX() : null, null), choice.weight));
            });
        }

        return this;
    }

    public ShowdownActionResponse response(Consumer<ShowdownActionResponse> consumer) {
        var response = this.choices.isEmpty()
            ? this.mustChoose && this.pkmn.hasPokemon()
                ? new DefaultActionResponse()
                : PassActionResponse.INSTANCE
            : getRandom(takeWithMargin(this.choices.stream().sorted(), this.margin), this.rng, this.margin, this.rdf)
                .orElse(new Choice<>(DefaultActionResponse::new, 0)).value.get();
            
        consumer.accept(response);
        return response;
    }

    public ShowdownActionResponse response() {
        return this.response(r -> {});
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

        public Choice(T value, double weight) {
            this.value = value;
            this.weight = weight;
        }

        @Override
        public int compareTo(Choice<?> other) {
            return Double.compare(this.weight, other.weight);
        }
    }

    // Stream utils

    private static <T> Stream<Choice<T>> takeWithMargin(Stream<Choice<T>> in, double margin) {
        double[] w = {Double.NEGATIVE_INFINITY};
        ModCommon.LOG.info("-------- CHOICES:");
        
        return in.takeWhile(choice -> {
            if(w[0] == Double.NEGATIVE_INFINITY) {
                w[0] = choice.weight;
            } else if(choice.weight - w[0] > margin) {
                return false;
            }

            ModCommon.LOG.info(" " + choice.value + ": " + choice.weight);
            return true;
        });
    }

    private static <T> Optional<Choice<T>> getRandom(Stream<Choice<T>> stream, Random rng, double margin, double f) {
        var it = stream.iterator();
        Choice<T> c;

        if(it.hasNext()) {
            var next= it.next();
            var start = next.weight;
            var w = 0.0;
            var i = 0;
            c = next;

            while(it.hasNext()) {
                next = it.next();
                w = margin > 0 ? (next.weight - start)/margin : 1;

                if(rng.nextInt((int)((++i) * (1 + w * f))) == 0) {
                    c = next;
                }
            }
        } else {
            c = null;
        }

        return Optional.ofNullable(c);
    }
}
