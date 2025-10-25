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
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.registries.BuiltInRegistries;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.api.util.Text;

/**
 * A POJO class for parsing {@link Pokemon}.
 * Now supports old string, new object, and list formats for heldItem.
 */
@JsonAdapter(PokemonModel.Deserializer.class)
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

        public StatsModel() {}

        public StatsModel(int hp, int atk, int def, int spa, int spd, int spe) {
            this.hp = hp; this.atk = atk; this.def = def;
            this.spa = spa; this.spd = spd; this.spe = spe;
        }

        @Override public boolean equals(Object obj) {
            return (obj instanceof StatsModel other)
                    && this.hp == other.hp && this.atk == other.atk
                    && this.def == other.def && this.spa == other.spa
                    && this.spd == other.spd && this.spe == other.spe;
        }

        @Override public int hashCode() {
            return Objects.hash(hp, atk, def, spa, spd, spe);
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
    private HeldItemsModel heldItem;
    private Set<String> aspects;
    private Gimmicks gimmicks;

    // --- Getters ---
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
    public String[] getHeldItems() { return this.heldItem.getItemIds(); }
    public Set<String> getAspects() { return Collections.unmodifiableSet(this.aspects); }
    public Gimmicks getGimmicks() { return this.gimmicks; }

    // --- Constructors (kept for compatibility) ---
    public PokemonModel() {
        this("", "GENDERLESS", 1, "", "", Set.of(), new StatsModel(), new StatsModel(), false, "", Set.of());
    }

    public PokemonModel(@NotNull String species, @NotNull String gender, int level, @NotNull String nature,
                        @NotNull String ability, @NotNull Set<String> moveset, @NotNull StatsModel ivs,
                        @NotNull StatsModel evs, boolean shiny, @NotNull String heldItem,
                        @NotNull Set<String> aspects) {
        this(species, Text.empty(), gender, level, nature, ability, moveset, ivs, evs, shiny, heldItem, aspects);
    }

    public PokemonModel(@NotNull String species, @NotNull String nickname, @NotNull String gender, int level,
                        @NotNull String nature, @NotNull String ability, @NotNull Set<String> moveset,
                        @NotNull StatsModel ivs, @NotNull StatsModel evs, boolean shiny,
                        @NotNull String heldItem, @NotNull Set<String> aspects) {
        this(species, Text.literal(nickname), gender, level, nature, ability, moveset, ivs, evs, shiny, heldItem, aspects);
    }

    public PokemonModel(@NotNull String species, @NotNull Text nickname, @NotNull String gender, int level,
                        @NotNull String nature, @NotNull String ability, @NotNull Set<String> moveset,
                        @NotNull StatsModel ivs, @NotNull StatsModel evs, boolean shiny,
                        @NotNull String heldItem, @NotNull Set<String> aspects) {
        this(species, nickname, gender, level, nature, ability, moveset, ivs, evs, shiny, List.of(heldItem), aspects, new Gimmicks());
    }

    public PokemonModel(@NotNull String species, @NotNull Text nickname, @NotNull String gender, int level,
                        @NotNull String nature, @NotNull String ability, @NotNull Set<String> moveset,
                        @NotNull StatsModel ivs, @NotNull StatsModel evs, boolean shiny,
                        @NotNull List<String> heldItems, @NotNull Set<String> aspects, @NotNull Gimmicks gimmicks) {
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
        this.heldItem = new HeldItemsModel(heldItems);
        this.aspects = aspects;
        this.gimmicks = gimmicks;
    }

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
        this.heldItem = new HeldItemsModel(BuiltInRegistries.ITEM.getKey(pokemon.heldItem().getItem()).toString());
        this.aspects = pokemon.getAspects();
        this.gimmicks = new Gimmicks();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PokemonModel other)
                && Objects.equals(this.species, other.species)
                && Objects.equals(this.nickname, other.nickname)
                && Objects.equals(this.gender, other.gender)
                && this.level == other.level
                && Objects.equals(this.nature, other.nature)
                && Objects.equals(this.ability, other.ability)
                && Objects.equals(this.moveset, other.moveset)
                && Objects.equals(this.ivs, other.ivs)
                && Objects.equals(this.evs, other.evs)
                && this.shiny == other.shiny
                && Objects.equals(this.heldItem, other.heldItem)
                && Objects.equals(this.aspects, other.aspects)
                && Objects.equals(this.gimmicks, other.gimmicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(species, nickname, gender, level, nature, ability, moveset, ivs, evs, shiny, heldItem, aspects, gimmicks);
    }

    // ===================================================================
    // CUSTOM GSON DESERIALIZER – SUPPORTS ALL HELDITEM FORMATS
    // ===================================================================
    public static class Deserializer implements JsonDeserializer<PokemonModel> {
        @Override
        public PokemonModel deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            PokemonModel model = new PokemonModel();

            model.species = getString(obj, "species", "");
            model.nickname = obj.has("nickname") ? Text.literal(getString(obj, "nickname", "")) : Text.empty();
            model.gender = getString(obj, "gender", "GENDERLESS");
            model.level = getInt(obj, "level", 1);
            model.nature = getString(obj, "nature", "");
            model.ability = getString(obj, "ability", "");
            model.moveset = ctx.deserialize(obj.get("moveset"), new com.google.gson.reflect.TypeToken<Set<String>>(){}.getType());
            if (model.moveset == null) model.moveset = Set.of();

            model.ivs = ctx.deserialize(obj.get("ivs"), StatsModel.class);
            if (model.ivs == null) model.ivs = new StatsModel();

            model.evs = ctx.deserialize(obj.get("evs"), StatsModel.class);
            if (model.evs == null) model.evs = new StatsModel();

            model.shiny = getBoolean(obj, "shiny", false);
            model.aspects = ctx.deserialize(obj.get("aspects"), new com.google.gson.reflect.TypeToken<Set<String>>(){}.getType());
            if (model.aspects == null) model.aspects = Set.of();

            model.gimmicks = ctx.deserialize(obj.get("gimmicks"), Gimmicks.class);
            if (model.gimmicks == null) model.gimmicks = new Gimmicks();

            // === HELDITEM: STRING, OBJECT, OR LIST ===
            JsonElement heldEl = obj.get("heldItem");
            List<String> heldItems = new ArrayList<>();

            if (heldEl != null) {
                if (heldEl.isJsonPrimitive() && heldEl.getAsJsonPrimitive().isString()) {
                    // "heldItem": "potion"
                    heldItems.add(heldEl.getAsString());
                } else if (heldEl.isJsonObject()) {
                    // "heldItem": { "item": "potion", "count": 1 }
                    JsonObject itemObj = heldEl.getAsJsonObject();
                    String item = getString(itemObj, "item", null);
                    if (item != null) {
                        int count = getInt(itemObj, "count", 1);
                        for (int i = 0; i < count; i++) heldItems.add(item);
                    }
                } else if (heldEl.isJsonArray()) {
                    // "heldItem": ["potion", "berry"]
                    for (JsonElement e : heldEl.getAsJsonArray()) {
                        if (e.isJsonPrimitive()) heldItems.add(e.getAsString());
                    }
                }
            }

            model.heldItem = new HeldItemsModel(heldItems);

            return model;
        }

        private String getString(JsonObject obj, String key, String def) {
            JsonElement el = obj.get(key);
            return el != null && el.isJsonPrimitive() ? el.getAsString() : def;
        }

        private int getInt(JsonObject obj, String key, int def) {
            JsonElement el = obj.get(key);
            return el != null && el.isJsonPrimitive() ? el.getAsInt() : def;
        }

        private boolean getBoolean(JsonObject obj, String key, boolean def) {
            JsonElement el = obj.get(key);
            return el != null && el.isJsonPrimitive() ? el.getAsBoolean() : def;
        }
    }
}