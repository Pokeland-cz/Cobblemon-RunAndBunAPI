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

public class TrainerPokemon extends Pokemon {

    public TrainerPokemon(){
        this.getCustomProperties().add(UncatchableProperty.INSTANCE.uncatchable());
        this.setUuid(UUID.randomUUID());
    }

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

    public Map<String, Object> getJSONContent(){
        Map<String, Object> JSONContent = new HashMap<>();

        JSONContent.put("species", this.getSpecies().toString());
        JSONContent.put("level", this.getLevel());

        List<String> JSONMoveset = new ArrayList<>();
        this.getMoveSet().forEach(move -> JSONMoveset.add(move.getName()));
        JSONContent.put("moveset", JSONMoveset);

        return JSONContent;

    }

    public Pokemon toPokemon(){
        return new Pokemon().copyFrom(this);
    }

    public static TrainerPokemon of(Pokemon pokemon){
        TrainerPokemon trainerPokemon = new TrainerPokemon();
        trainerPokemon.setSpecies(pokemon.getSpecies());
        trainerPokemon.setLevel(pokemon.getLevel());
        trainerPokemon.getMoveSet().copyFrom(pokemon.getMoveSet());

        return trainerPokemon;
    }

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
