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
package com.gitlab.srcmc.rctapi.api.battle;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;

/**
 * Contains information about an ongoing trainer battle.
 */
public class BattleState {
    private final Map<UUID, ActorState> actorStates = new HashMap<>();
    private final PokemonBattle battle;
    private final BattleFormat format;
    private final BattleRules rules;
    private final List<Trainer> participants1;
    private final List<Trainer> participants2;

    /**
     * Creates a new battle state for the given {@link PokemonBattle}.
     * 
     * @param battle {@link PokemonBattle} referenced by this battle state.
     * @param rules {@link BattleRules} enforced on the battle.
     * @deprecated Use {@link BattleState#BattleState(PokemonBattle, BattleFormat, BattleRules, List, List)} instead.
     */
    public BattleState(
        @NotNull PokemonBattle battle,
        @NotNull BattleRules rules)
    {
        this(battle, BattleFormat.GEN_9_SINGLES, rules, List.of(), List.of());
    }

    /**
     * Creates a new battle state for the given {@link PokemonBattle}.
     * 
     * @param battle {@link PokemonBattle} referenced by this battle state.
     * @param format {@link BattleFormat} of the battle.
     * @param rules {@link BattleRules} enforced on the battle.
     * @param participants1 Trainer participants from the first side.
     * @param participants2 Trainer participants from the second side.
     */
    public BattleState(
        @NotNull PokemonBattle battle,
        @NotNull BattleFormat format,
        @NotNull BattleRules rules,
        @NotNull List<Trainer> participants1,
        @NotNull List<Trainer> participants2)
    {
        this.battle = battle;
        this.format = format;
        this.rules = rules;
        this.participants1 = Collections.unmodifiableList(participants1);
        this.participants2 = Collections.unmodifiableList(participants2);
    }

    /**
     * Retrieves the {@link BattleFormat} defined for the battle.
     * 
     * @return {@link BattleFormat} of the battle.
     */
    @NotNull
    public BattleFormat getFormat() {
        return this.format;
    }

    /**
     * Retrieves an {@link ActorState} from this battle state.
     * 
     * @param actorUUID UUID of the {@link BattleActor}.
     * @return The {@link ActorState} of the {@link BattleActor}.
     */
    @NotNull
    public ActorState getState(@NotNull UUID actorUUID) {
        return this.actorStates.computeIfAbsent(actorUUID, k -> new ActorState());
    }

    /**
     * Retrieves the {@link PokemonBattle} referenced by this battle state.
     * 
     * @return {@link PokemonBattle} referenced by this battle state.
     */
    @NotNull
    public PokemonBattle getBattle() {
        return this.battle;
    }

    /**
     * Retrieves the {@link BattleRules} enforced on the battle.
     * 
     * @return {@link BattleRules} enforced on the battle.
     */
    @NotNull
    public BattleRules getRules() {
        return this.rules;
    }

    /**
     * Retrieves the {@link Trainer} participants for the first battle side.
     * 
     * @return List of {@link Trainer} participants.
     */
    @NotNull
    public List<Trainer> getParticipants1() {
        return this.participants1;
    }

    /**
     * Retrieves the {@link Trainer} participants for the second battle side.
     * 
     * @return List of {@link Trainer} participants.
     */
    @NotNull
    public List<Trainer> getParticipants2() {
        return this.participants2;
    }

    /**
     * Retrieves the winning {@link Trainer} participants of the battle.
     * 
     * @return List of winning {@link Trainer} participants or empty list if battle has not ended.
     */
    @NotNull
    public List<Trainer> getWinners() {
        return this.battle.getEnded()
            ? this.battle.getWinners().size() > 0 && battle.getWinners().getFirst().getSide() == battle.getSide1()
                ? this.participants1
                : this.participants2
            : List.of();
    }

    /**
     * Retrieves the losing {@link Trainer} participants of the battle.
     * 
     * @return List of losing {@link Trainer} participants or empty list if battle has not ended.
     */
    @NotNull
    public List<Trainer> getLosers() {
        return this.battle.getEnded()
            ? this.battle.getLosers().size() > 0 && battle.getLosers().getFirst().getSide() == battle.getSide1()
                ? this.participants1
                : this.participants2
            : List.of();
    }

    /**
     * Retrieves the battle side of the winning team.
     * 
     * @return Battle side of winning team or 0 if the battle has not ended.
     */
    public int getWinnerSide() {
        return this.battle.getEnded()
            ? this.battle.getWinners().size() > 0 && battle.getWinners().getFirst().getSide() == battle.getSide1()
                ? 1
                : 2
            : 0;
    }

    /**
     * Retrieves the battle side of the losing team.
     * 
     * @return Battle side of losing team or 0 if the battle has not ended.
     */
    public int getLoserSide() {
        return this.battle.getEnded()
            ? this.battle.getLosers().size() > 0 && battle.getLosers().getFirst().getSide() == battle.getSide1()
                ? 1
                : 2
            : 0;
    }

    /**
     * Defines the state of a {@link BattleActor} within an active trainer battle.
     */
    public class ActorState {
        private int itemsUsed;

        /**
         * Sets the amount of items used by the {@link BattleActor} during the battle.
         * 
         * @param itemsUsed Number of items used.
         */
        public void setItemsUsed(int itemsUsed) {
            this.itemsUsed = itemsUsed;
        }

        /**
         * Retrieves the amount of items used by the {@link BattleActor} during the battle.
         * 
         * @return Number of items used.
         */
        public int getItemsUsed() {
            return this.itemsUsed;
        }
    }
}
