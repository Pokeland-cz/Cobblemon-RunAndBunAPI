package net.ctengine.trainer;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.pokemon.*;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.ctengine.CTEngine;
import net.ctengine.util.CastingUtil;
import net.minecraft.util.Identifier;

import java.util.*;

// TODO - Add more attributes (i.e IVs, gender, ability etc.)

public class TrainerPokemon extends Pokemon {
    // The Pokemon object itself needs to be tracked to check if it is owned by a trainer.
    // However as we can't really set a custom attribute on the Pokemon object as it's not
    // a Trainer Pokemon, we have to store this information in a static List.
    public static final List<UUID> isTrainerOwned = new ArrayList<>();

    public TrainerPokemon(){
        this.getCustomProperties().add(UncatchableProperty.INSTANCE.uncatchable());
    }

    // A lot of the if statements here are to check for safe casts.
    // Probably more efficient ways but good enough for now.
    public void initFromJSONContent(Map<String, Object> JSONContent){

        // Set Species
        String speciesString = CastingUtil.safeCast(JSONContent.get("species"), String.class, true);
        if(speciesString != null){
            Species species = PokemonSpecies.INSTANCE.getByIdentifier(Identifier.of("cobblemon", speciesString));
            if (species != null) {
                this.setSpecies(species);
            } else {
                CTEngine.LOGGER.info("Invalid cobblemon species for: " + speciesString);
            }
        }

        // Set Level
        Double levelDouble = CastingUtil.safeCast(JSONContent.get("level"), Double.class, true);
        if (levelDouble != null) this.setLevel((int) Math.floor(levelDouble));

        // Set Moveset
        if (JSONContent.get("moveset") instanceof List<?> moveset){
            for (int i = 0; i < Math.min(4, moveset.size()); i ++){
                String moveString = CastingUtil.safeCast(moveset.get(i), String.class, true);
                MoveTemplate moveTemplate = (moveString != null) ? Moves.INSTANCE.getByName(moveString) : null;
                if (moveTemplate != null){
                    Move move = moveTemplate.create();
                    this.getMoveSet().setMove(i, move);
                } else {
                    CTEngine.LOGGER.info("Invalid cobblemon move for: " + moveString);
                }
            }
        }

        // Set Gender
        String genderString = CastingUtil.safeCast(JSONContent.get("gender"), String.class, true);
        if ((Objects.equals(genderString, "MALE") || Objects.equals(genderString, "FEMALE") || Objects.equals(genderString, "GENDERLESS"))){
            this.setGender(Gender.valueOf(genderString));
        } else {
            // If no Gender is found in JSON then use MALE as default
            this.setGender(Gender.MALE);
        }

        // Set nature
        String natureString = CastingUtil.safeCast(JSONContent.get("nature"), String.class, true);
        Nature nature = (natureString != null) ? Natures.INSTANCE.getNature(Identifier.of("cobblemon",natureString)) : null;
        if (nature != null) this.setNature(nature);

        // Set Ability
        String abilityString = CastingUtil.safeCast(JSONContent.get("ability"), String.class, true);
        AbilityTemplate abilityTemplate = (abilityString != null) ? Abilities.INSTANCE.get(abilityString) : null;
        Ability ability = (abilityTemplate != null) ? new Ability(abilityTemplate, false) : null;
        if (ability != null) this.updateAbility(ability);

        // Set IVs
        // Set and build pokemon team
        if (JSONContent.get("ivs") instanceof Map<?, ?> ivMap){
            Map<String, Object> ivMapParsed = CastingUtil.rebuildMap(ivMap);
            for (String statString : ivMapParsed.keySet()){
                Double ivValue = CastingUtil.safeCast(ivMapParsed.get(statString), Double.class, true);
                Integer ivInt = (ivValue != null) ? (int) Math.floor(ivValue) : null;
                Stat stat = Cobblemon.INSTANCE.getStatProvider().fromIdentifier(Identifier.of("cobblemon",statString));
                if (stat != null && ivInt != null) {
                    this.setIV(stat, ivInt);
                } else {
                    CTEngine.LOGGER.info("COULD NOT SET IV FOR "+statString+" : "+ivValue);
                }
            }
        }
    }

    // Convert the attributes into a JSON format, used when saving a trainer and their team.
    public Map<String, Object> getJSONContent(){
        Map<String, Object> JSONContent = new HashMap<>();

        JSONContent.put("species", this.getSpecies().toString());
        JSONContent.put("level", this.getLevel());
        JSONContent.put("gender", this.getGender().asString());
        JSONContent.put("nature", this.getNature().getName().getPath());
        JSONContent.put("ability", this.getAbility().getName());

        List<String> JSONMoveset = new ArrayList<>();
        this.getMoveSet().forEach(move -> JSONMoveset.add(move.getName()));
        JSONContent.put("moveset", JSONMoveset);

        Map<String, Object> JSONIVs = new HashMap<>();
        IVs ivs = this.getIvs();
        ivs.spliterator().forEachRemaining(entry -> JSONIVs.put(entry.getKey().getIdentifier().getPath(), entry.getValue()));
        JSONContent.put("ivs",JSONIVs);

        return JSONContent;
    }

    // Return a new instance of a Pokemon here as this is used for the battle team.
    // If you don't make a new instance then the trainer's pokemon will stay fainted
    // across battles.
    public Pokemon toNewPokemon(){
        Pokemon pokemon = new Pokemon().copyFrom(this);
        // We generate a random UUID here as multiple trainers
        // might use the same Trainer Pokemon object on creation.
        // This makes instances of the Trainer Pokemon unique when battling.
        pokemon.setUuid(UUID.randomUUID());
        return pokemon;
    }

    // Cast singular Pokemon object to a Trainer Pokemon.
    // There might be a more efficient way??
    public static TrainerPokemon of(Pokemon pokemon){
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        trainerPokemon.setSpecies(pokemon.getSpecies());
        trainerPokemon.setLevel(pokemon.getLevel());
        trainerPokemon.setGender(pokemon.getGender());
        trainerPokemon.setNature(pokemon.getNature());
        trainerPokemon.updateAbility(pokemon.getAbility());
        trainerPokemon.getMoveSet().copyFrom(pokemon.getMoveSet());

        IVs ivs = pokemon.getIvs();
        ivs.spliterator().forEachRemaining(entry -> trainerPokemon.setIV(entry.getKey(), entry.getValue()));

        return trainerPokemon;
    }

    // Cast list of Pokemon objects to Trainer Pokemon
    public static List<TrainerPokemon> of(List<Pokemon> pokemonList){
        List<TrainerPokemon> trainerPokemonList = new ArrayList<>();
        for (Pokemon pokemon : pokemonList){
            if(pokemon != null){
                trainerPokemonList.add(TrainerPokemon.of(pokemon));
            }
        }
        return trainerPokemonList;
    }
}
