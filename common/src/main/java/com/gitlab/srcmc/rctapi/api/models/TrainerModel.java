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
package com.gitlab.srcmc.rctapi.api.models;

import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import com.gitlab.srcmc.rctapi.api.ai.AIType;

/**
 * A pojo class for parsing {@link Trainer}.
 */
public class TrainerModel {
    private String name;
    private AIType ai;
    private List<BagItemModel> bag;
    private List<PokemonModel> team;

    /**
     * Creates a new TrainerModel.
     */
    public TrainerModel() {
        this("", AIType.LAI, List.of(), List.of());
    }

    /**
     * Creates a new TrainerModel with the given properties.
     * 
     * @param name Name of the trainer.
     * @param ai Battle AI type used by the trainer.
     * @param bag Bag of items the trainer may use in a battle.
     * @param team Pokemon party of the trainer.
     */
    public TrainerModel(@NotNull String name, @NotNull AIType ai, @NotNull List<BagItemModel> bag, @NotNull List<PokemonModel> team) {
        this.name = name;
        this.ai = ai;
        this.bag = bag;
        this.team = team;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public List<BagItemModel> getBag() {
        return this.bag;
    }

    @NotNull
    public List<PokemonModel> getTeam() {
        return this.team;
    }

    @NotNull
    public AIType getAI() {
        return this.ai;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.team);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TrainerModel other)
            && this.name.equals(other.name)
            && this.bag.equals(other.bag)
            && this.team.equals(other.team);
    }
}
