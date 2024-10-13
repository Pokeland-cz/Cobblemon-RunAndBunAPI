package net.ctengine.api.models.converter;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager;

import net.ctengine.api.errors.CTError;
import net.ctengine.api.errors.CTErrors;
import net.ctengine.api.errors.CTException;
import net.ctengine.api.models.PokemonModel;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Defines a conversion from {@link PokemonModel} to {@link Pokemon}.
 */
public class PokemonModelConverter implements Converter<PokemonModel, Pokemon> {
    @Override
    public Pokemon toTarget(PokemonModel model, CTErrors<CTException> errors) {
        var pokemon = new Pokemon();

        if(!model.getSpecies().isBlank()) {
            errors.doif(
                PokemonSpecies.INSTANCE.getByIdentifier(Identifier.of(model.getSpecies())),
                v -> v != null, v -> pokemon.setSpecies(v),
                "invalid species '" + model.getSpecies() + "'");
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
            errors.doif(
                Natures.INSTANCE.getNature(model.getNature()),
                v -> v != null, v -> pokemon.setNature(v),
                "invalid nature '" + model.getNature() + "'");
        }

        if(!model.getAbility().isBlank()) {
            errors.doif(
                Abilities.INSTANCE.get(model.getAbility()),
                v -> v != null, v -> pokemon.setAbility$common(v.create(true)),
                "invalid ability '" + model.getAbility() + "'");
        }

        if(model.getMoveset().size() > 4) {
            errors.add(CTError.of("too many moves " + model.getMoveset().size() + "/4"));
        }

        model.getMoveset().stream().limit(42).forEach(m -> {
            errors.doif(
                Moves.INSTANCE.getByName(m),
                v -> v != null, v -> pokemon.getMoveSet().add(v.create()),
                "invalid move '" + m + "'");
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
            errors.doif(
                Registries.ITEM.get(Identifier.of(model.getHeldItem())),
                v -> CobblemonHeldItemManager.INSTANCE.showdownIdOf(v) != null,
                v -> pokemon.setHeldItem$common(v.getDefaultStack()),
                "invalid held item '" + model.getHeldItem() + "'");
        }

        return pokemon;
    }
}
