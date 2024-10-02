package net.ctengine.trainer;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;
import net.ctengine.CTEngine;
import net.ctengine.util.CastingUtil;
import net.minecraft.util.Identifier;

import java.util.*;

// TODO - Add more attributes (i.e IVs, gender, ability etc.)

public class TrainerPokemon extends Pokemon {

    public TrainerPokemon(){
        this.getCustomProperties().add(UncatchableProperty.INSTANCE.uncatchable());
        // Set a random UUID on initialisation otherwise the mc entity manager will
        // throw an error on server startup.
        this.setUuid(UUID.randomUUID());
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
    }

    // Convert the attributes into a JSON format, used when saving a trainer and their team.
    public Map<String, Object> getJSONContent(){
        Map<String, Object> JSONContent = new HashMap<>();

        JSONContent.put("species", this.getSpecies().toString());
        JSONContent.put("level", this.getLevel());

        List<String> JSONMoveset = new ArrayList<>();
        this.getMoveSet().forEach(move -> JSONMoveset.add(move.getName()));
        JSONContent.put("moveset", JSONMoveset);

        return JSONContent;

    }

    // Return a new instance of a Pokemon here as this is used for the battle team.
    // If you don't make a new instance then the trainer's pokemon will stay fainted
    // across battles.
    public Pokemon toNewPokemon(){
        return new Pokemon().copyFrom(this);
    }

    // Cast singular Pokemon object to a Trainer Pokemon.
    // There might be a more efficient way??
    public static TrainerPokemon of(Pokemon pokemon){
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        trainerPokemon.setSpecies(pokemon.getSpecies());
        trainerPokemon.setLevel(pokemon.getLevel());
        trainerPokemon.getMoveSet().copyFrom(pokemon.getMoveSet());

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
