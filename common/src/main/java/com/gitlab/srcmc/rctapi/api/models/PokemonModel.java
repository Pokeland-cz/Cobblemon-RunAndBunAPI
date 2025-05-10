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
package com.gitlab.srcmc.rctapi.api.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.registries.BuiltInRegistries;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.api.util.Text;

/**
 * A pojo class for parsing {@link Pokemon}.
 */
public class PokemonModel implements Serializable {
    private static final long serialVersionUID = 0L;

    public static class StatsModel implements Serializable {
        private static final long serialVersionUID = 0L;
        
        private int hp;
        private int atk;
        private int def;
        private int spa;
        private int spd;
        private int spe;

        public int getHP() { return this.hp; }
        public int getAtk() { return this.atk; }
        public int getDef() { return this.def; }
        public int getSpA() { return this.spa; }
        public int getSpD() { return this.spd; }
        public int getSpe() { return this.spe; }

        /**
         * Creates a new StatsModel.
         */
        public StatsModel() {
        }

        /**
         * Creates a new Stats model with the given properties.
         * 
         * @param hp HP stat.
         * @param atk Attack stat.
         * @param def Defense stat.
         * @param spa Special attack stat.
         * @param spd Special defense stat.
         * @param spe Speed stat.
         */
        public StatsModel(int hp, int atk, int def, int spa, int spd, int spe) {
            this.hp = hp;
            this.atk = atk;
            this.def = def;
            this.spa = spa;
            this.spd = spd;
            this.spe = spe;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof StatsModel other)
                && this.hp == other.hp
                && this.atk == other.atk
                && this.def == other.def
                && this.spa == other.spa
                && this.spd == other.spd
                && this.spe == other.spe;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                this.hp, this.atk,
                this.def, this.spa,
                this.spd, this.spe);
        }
    }

    private String species;
    private Text nickname;
    private String gender;
    private int level;
    private String nature;
    private String ability;
    private Set<String> moveset;
    private StatsModel ivs;
    private StatsModel evs;
    private boolean shiny;
    private String heldItem;
    private Set<String> aspects;
    
    public String getSpecies() { return this.species; }
    public Text getNickname() { return this.nickname; }
    public String getGender() { return this.gender; }
    public int getLevel() { return this.level; }
    public String getNature() { return this.nature; }
    public String getAbility() { return this.ability; }
    public Set<String> getMoveset() { return Collections.unmodifiableSet(this.moveset); }
    public StatsModel getIVs() { return this.ivs; }
    public StatsModel getEVs() { return this.evs; }
    public boolean isShiny() { return this.shiny; }
    public String getHeldItem() { return this.heldItem; }
    public Set<String> getAspects() { return Collections.unmodifiableSet(this.aspects); }

    /**
     * Creates a new pokemon model.
     */
    public PokemonModel() {
        this("", "GENDERLESS", 1, "", "", Set.of(), new StatsModel(), new StatsModel(), false, "", Set.of());
    }

    /**
     * Creates a new pokemon model with the given properties.
     * 
     * @param species Pokemon species.
     * @param gender Pokemon gender ("GENDERLESS", "MALE" or "FEMALE").
     * @param level Pokemon level.
     * @param nature Pokemon nature.
     * @param ability Pokemon ability.
     * @param moveset Set of moves.
     * @param ivs Pokemon ivs.
     * @param evs Pokemon evs.
     * @param shiny If the pokemon is shiny or not.
     * @param heldItem Item held by the pokemon.
     * @param aspects Set of pokemon aspects.
     */
    public PokemonModel(
        @NotNull String species, @NotNull String gender,
        int level, @NotNull String nature,
        @NotNull String ability, @NotNull Set<String> moveset,
        @NotNull StatsModel ivs, @NotNull StatsModel evs,
        boolean shiny, @NotNull String heldItem,
        @NotNull Set<String> aspects)
    {
        this(species, Text.empty(), gender, level, nature, ability, moveset, ivs, evs, shiny, heldItem, aspects);
    }

    /**
     * Creates a new pokemon model with the given properties.
     * 
     * @param species Pokemon species.
     * @param nickname Pokemon nickname.
     * @param gender Pokemon gender ("GENDERLESS", "MALE" or "FEMALE").
     * @param level Pokemon level.
     * @param nature Pokemon nature.
     * @param ability Pokemon ability.
     * @param moveset Set of moves.
     * @param ivs Pokemon ivs.
     * @param evs Pokemon evs.
     * @param shiny If the pokemon is shiny or not.
     * @param heldItem Item held by the pokemon.
     * @param aspects Set of pokemon aspects.
     */
    public PokemonModel(
        @NotNull String species, @NotNull String nickname,
        @NotNull String gender, int level, @NotNull String nature,
        @NotNull String ability, @NotNull Set<String> moveset,
        @NotNull StatsModel ivs, @NotNull StatsModel evs,
        boolean shiny, @NotNull String heldItem,
        @NotNull Set<String> aspects)
    {
        this(species, Text.literal(nickname), gender, level, nature, ability, moveset, ivs, evs, shiny, heldItem, aspects);
    }

    /**
     * Creates a new pokemon model with the given properties.
     * 
     * @param species Pokemon species.
     * @param nickname Pokemon nickname.
     * @param gender Pokemon gender ("GENDERLESS", "MALE" or "FEMALE").
     * @param level Pokemon level.
     * @param nature Pokemon nature.
     * @param ability Pokemon ability.
     * @param moveset Set of moves.
     * @param ivs Pokemon ivs.
     * @param evs Pokemon evs.
     * @param shiny If the pokemon is shiny or not.
     * @param heldItem Item held by the pokemon.
     * @param aspects Set of pokemon aspects.
     */
    public PokemonModel(
        @NotNull String species, @NotNull Text nickname,
        @NotNull String gender, int level, @NotNull String nature,
        @NotNull String ability, @NotNull Set<String> moveset,
        @NotNull StatsModel ivs, @NotNull StatsModel evs,
        boolean shiny, @NotNull String heldItem,
        @NotNull Set<String> aspects)
    {
        this.species = species;
        this.nickname = nickname;
        this.gender = gender;
        this.level = level;
        this.nature = nature;
        this.ability = ability;
        this.moveset = moveset;
        this.ivs = ivs;
        this.evs = evs;
        this.shiny = shiny;
        this.heldItem = heldItem;
        this.aspects = aspects;
    }

    /**
     * Creates a new pokemon model with its properties copied from the given pokemon.
     * 
     * @param pokemon Pokemon to copy.
     */
    public PokemonModel(Pokemon pokemon) {
        this.species = pokemon.getSpecies().getName();
        this.nickname = pokemon.getNickname() != null ? Text.literal(pokemon.getNickname().getString()) : Text.empty();
        this.gender = pokemon.getGender().getSerializedName();
        this.level = pokemon.getLevel();
        this.nature = pokemon.getNature().getName().toString();
        this.ability = pokemon.getAbility().getName();
        this.moveset = pokemon.getMoveSet().getMoves().stream().map(m -> m.getName()).collect(Collectors.toSet());
        this.ivs = new StatsModel();
        this.ivs.hp = pokemon.getIvs().getOrDefault(Stats.HP);
        this.ivs.atk = pokemon.getIvs().getOrDefault(Stats.ATTACK);
        this.ivs.def = pokemon.getIvs().getOrDefault(Stats.DEFENCE);
        this.ivs.spa = pokemon.getIvs().getOrDefault(Stats.SPECIAL_ATTACK);
        this.ivs.spd = pokemon.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE);
        this.ivs.spe = pokemon.getIvs().getOrDefault(Stats.SPEED);
        this.evs = new StatsModel();
        this.evs.hp = pokemon.getEvs().getOrDefault(Stats.HP);
        this.evs.atk = pokemon.getEvs().getOrDefault(Stats.ATTACK);
        this.evs.def = pokemon.getEvs().getOrDefault(Stats.DEFENCE);
        this.evs.spa = pokemon.getEvs().getOrDefault(Stats.SPECIAL_ATTACK);
        this.evs.spd = pokemon.getEvs().getOrDefault(Stats.SPECIAL_DEFENCE);
        this.evs.spe = pokemon.getEvs().getOrDefault(Stats.SPEED);
        this.shiny = pokemon.getShiny();
        this.heldItem = BuiltInRegistries.ITEM.getKey(pokemon.heldItem().getItem()).toString();
        this.aspects = pokemon.getAspects();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PokemonModel other)
            && this.species.equals(other.species)
            && this.nickname.equals(other.nickname)
            && this.gender.equals(other.gender)
            && this.level == other.level
            && this.nature.equals(other.nature)
            && this.ability.equals(other.ability)
            && this.moveset.equals(other.moveset)
            && this.ivs.equals(other.ivs)
            && this.evs.equals(other.evs)
            && this.shiny == other.shiny
            && this.heldItem.equals(other.heldItem)
            && this.aspects.equals(other.aspects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.species, this.nickname,
            this.gender, this.level,
            this.nature, this.ability,
            this.moveset, this.ivs, this.evs,
            this.shiny, this.heldItem,
            this.aspects);
    }
}
