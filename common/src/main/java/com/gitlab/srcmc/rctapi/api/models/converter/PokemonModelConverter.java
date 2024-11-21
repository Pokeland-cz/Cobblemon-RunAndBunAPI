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
package com.gitlab.srcmc.rctapi.api.models.converter;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.api.errors.RCTError;
import com.gitlab.srcmc.rctapi.api.errors.RCTErrors;
import com.gitlab.srcmc.rctapi.api.errors.RCTException;
import com.gitlab.srcmc.rctapi.api.models.PokemonModel;
import com.gitlab.srcmc.rctapi.api.util.Locations;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Defines a conversion from {@link PokemonModel} to {@link Pokemon}.
 */
public class PokemonModelConverter implements Converter<PokemonModel, Pokemon> {
    @Override
    public Pokemon toTarget(PokemonModel model, RCTErrors<RCTException> errors) {
        var pokemon = new Pokemon();

        if(!model.getSpecies().isBlank()) {
            var species = Locations.withNamespace("cobblemon", model.getSpecies());

            errors.doif(
                PokemonSpecies.INSTANCE.getByIdentifier(ResourceLocation.parse(species)),
                v -> v != null, v -> pokemon.setSpecies(v),
                "invalid species '" + species + "'");
        }

        var g = Gender.FEMALE.name().equals(model.getGender()) ? Gender.FEMALE
            : Gender.MALE.name().equals(model.getGender()) ? Gender.MALE
            : Gender.GENDERLESS;

        pokemon.setGender(errors.expect(g, v -> {
            try {
                Gender.valueOf(model.getGender());
            } catch(IllegalArgumentException e) {
                return false;
            }
            
            return true;
        }, "invalid gender '" + model.getGender() + "'"));

        pokemon.setLevel(errors.expect(model.getLevel(), v -> v > 0, "invalid level '%s'"));

        if(!model.getNature().isBlank()) {
            var nature = Locations.withoutNamespace(model.getNature());

            errors.doif(
                Natures.INSTANCE.getNature(nature),
                v -> v != null, v -> pokemon.setNature(v),
                "invalid nature '" + nature + "'");
        }

        if(!model.getAbility().isBlank()) {
            var ability = Locations.withoutNamespace(model.getAbility());

            errors.doif(
                Abilities.INSTANCE.get(ability),
                v -> v != null, v -> pokemon.updateAbility(v.create(true, Priority.NORMAL)),
                "invalid ability '" + ability + "'");
        }

        if(model.getMoveset().size() > 4) {
            errors.add(RCTError.of("too many moves " + model.getMoveset().size() + "/4"));
        }

        model.getMoveset().stream().limit(42).forEach(m -> {
            var move = Locations.withoutNamespace(m);

            errors.doif(
                Moves.INSTANCE.getByName(move),
                v -> v != null, v -> pokemon.getMoveSet().add(v.create()),
                "invalid move '" + move + "'");
        });

        pokemon.setIV(Stats.HP, errors.expect(model.getIVs().getHP(), v -> v >=0 && v <= 31, "invalid hp iv '%s'"));
        pokemon.setIV(Stats.ATTACK, errors.expect(model.getIVs().getAtk(), v -> v >=0 && v <= 31, "invalid atk iv '%s'"));
        pokemon.setIV(Stats.DEFENCE, errors.expect(model.getIVs().getDef(), v -> v >=0 && v <= 31, "invalid def iv '%s'"));
        pokemon.setIV(Stats.SPECIAL_ATTACK, errors.expect(model.getIVs().getSpA(), v -> v >=0 && v <= 31, "invalid spa iv '%s'"));
        pokemon.setIV(Stats.SPECIAL_DEFENCE, errors.expect(model.getIVs().getSpD(), v -> v >=0 && v <= 31, "invalid spd iv '%s'"));
        pokemon.setIV(Stats.SPEED, errors.expect(model.getIVs().getSpe(), v -> v >=0 && v <= 31, "invalid spe iv '%s'"));
        pokemon.setEV(Stats.HP, errors.expect(model.getEVs().getHP(), v -> v >=0 && v <= 255, "invalid hp ev '%s'"));
        pokemon.setEV(Stats.ATTACK, errors.expect(model.getEVs().getAtk(), v -> v >=0 && v <= 255, "invalid atk ev '%s'"));
        pokemon.setEV(Stats.DEFENCE, errors.expect(model.getEVs().getDef(), v -> v >=0 && v <= 255, "invalid def ev '%s'"));
        pokemon.setEV(Stats.SPECIAL_ATTACK, errors.expect(model.getEVs().getSpA(), v -> v >=0 && v <= 255, "invalid spa ev '%s'"));
        pokemon.setEV(Stats.SPECIAL_DEFENCE, errors.expect(model.getEVs().getSpD(), v -> v >=0 && v <= 255, "invalid spd ev '%s'"));
        pokemon.setEV(Stats.SPEED, errors.expect(model.getEVs().getSpe(), v -> v >=0 && v <= 255, "invalid spe ev '%s'"));
        pokemon.setShiny(model.isShiny());
        pokemon.setForcedAspects(model.getAspects());

        if(!model.getHeldItem().isBlank()) {
            var item = Locations.withNamespace("cobblemon", model.getHeldItem());
            var rl = ResourceLocation.parse(item);

            errors.doif(BuiltInRegistries.ITEM.containsKey(rl), v -> v,
                v -> pokemon.swapHeldItem(BuiltInRegistries.ITEM.get(rl).getDefaultInstance(), true),
                "invalid held item '" + item + "'");
        }

        return pokemon;
    }
}
