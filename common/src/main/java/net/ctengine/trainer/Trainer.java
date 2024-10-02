package net.ctengine.trainer;

import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import net.ctengine.CTEngine;
import net.ctengine.util.JSONHandler;
import net.ctengine.util.CastingUtil;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO - add more attributes (i.e. Win command, Loss command etc.)

public class Trainer {
    private final String id;
    private final List<TrainerPokemon> team = new ArrayList<>();
    private String displayName = null;
    private final List<String> defeatRequirements = new ArrayList<>();

    // Trainer is protected so people have to use the TrainerRegistry to create a trainer.
    // This way trainers will always be loaded into the TrainerRegistry rather than having to manually add
    protected Trainer(String trainerID){
        this.id = trainerID;
    }

    public String getId(){
        return this.id;
    }

    // Display name used for battling, if it has not been initialised then just return ID
    public String getDisplayName(){
        return  (this.displayName == null) ? this.id : this.displayName;
    }

    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }

    // getBattleTeam is used when initiating a battle. We convert Trainer Pokemon to
    // Pokemon, then to BattlePokemon and build a team from that.
    public List<BattlePokemon> getBattleTeam() {
        List<BattlePokemon> battlePokemonList = new ArrayList<>();
        for (TrainerPokemon teamMember : this.team){
            Pokemon pokemon = teamMember.toNewPokemon();
            battlePokemonList.add(new BattlePokemon(pokemon, pokemon, (pokemonEntity -> {
                // We discard pokemonEntity here because if not then a pokemon wild pokemon will be left over
                // If the player forfeits the battle
                pokemonEntity.discard();
                return Unit.INSTANCE;
            })));
        }
        return battlePokemonList;
    }

    public void addTrainerPokemon(TrainerPokemon trainerPokemon){
        if (trainerPokemon != null){
            this.team.add(trainerPokemon);
        }
    }

    // Used to add multiple Pokemon at a time in case you just want to pass in a List
    public void addMultipleTrainerPokemon(List<TrainerPokemon> trainerPokemonList){
        for (TrainerPokemon trainerPokemon : trainerPokemonList){
            if (trainerPokemon != null){
                this.team.add(trainerPokemon);
            }
        }
    }

    public void removeTrainerPokemon(TrainerPokemon trainerPokemon){
        this.team.remove(trainerPokemon);
    }

    public void removeMultipleTrainerPokemon(List<TrainerPokemon> trainerPokemonList){
        for (TrainerPokemon trainerPokemon : trainerPokemonList){
            if (trainerPokemon != null){
                this.team.remove(trainerPokemon);
            }
        }
    }

    // Load the attributes from the read in JSON content
    public void initFromJSONContent(Map<String, Object> JSONContent){
        // Set display name
        String displayName = CastingUtil.safeCast(JSONContent.get("species"), String.class, true);
        this.displayName = (displayName != null) ? displayName : this.id;

        // Set defeat requirements
        if (JSONContent.get("defeatRequirements") instanceof List<?> defeatRequirementsList){
            for (Object defeatRequirementObj : defeatRequirementsList){
                String defeatRequirement = CastingUtil.safeCast(defeatRequirementObj, String.class, true);
                if(defeatRequirement != null) this.defeatRequirements.add(defeatRequirement);
            }
        }

        // Set and build pokemon team
        if (JSONContent.get("team") instanceof List<?> trainerTeam){
            CTEngine.LOGGER.info("initialising team for trainer: "+this.id);
            for (Object teamMember : trainerTeam){
                if (teamMember instanceof Map<?, ?> teamMemberInfo){
                    Map<String, Object> teamMemberInfoParsed = CastingUtil.rebuildMap(teamMemberInfo);

                    TrainerPokemon trainerPokemon = new TrainerPokemon();
                    trainerPokemon.initFromJSONContent(teamMemberInfoParsed);
                    this.team.add(trainerPokemon);
                }
            }
        }
    }

    // Saves trainer data to a JSON located in world/ctengine/trainers directory.
    // Any JSON in this directory will be initialised into the trainer registry on server start.
    public void save(){
        if (CTEngine.runServer != null){
            Path JSONPath = Paths.get(CTEngine.runServer.getSavePath(WorldSavePath.PLAYERDATA).getParent().toString(),
                    "ctengine", "trainers", this.id+".json");

            Map<String, Object> JSONContent = new HashMap<>();

            JSONContent.put("displayName", this.displayName);
            JSONContent.put("defeatRequirements",this.defeatRequirements);

            List<Map<String, Object>> parsedTeam = new ArrayList<>();
            for (TrainerPokemon teamMember : this.team){
                parsedTeam.add(teamMember.getJSONContent());
            }
            JSONContent.put("team", parsedTeam);

            JSONHandler.writeJSON(JSONContent, JSONPath);
            CTEngine.LOGGER.info("Saving to: "+JSONPath);

        }
    }

    // When the trainer is removed from the Trainer Registry this is called, hence the protected,
    // as Creation/Deletion of trainers should only be done through the TrainerRegistry
    protected void deleteJSONFile(){
        Path JSONPath = Paths.get(CTEngine.runServer.getSavePath(WorldSavePath.PLAYERDATA).getParent().toString(),
                "ctengine", "trainers", this.id+".json");
        try {
            Files.delete(JSONPath);
        } catch (IOException e) {
            // Probably don't need to log anything here
            //CTEngine.LOGGER.info(e.toString());
        }
    }

    // Used when deciding if a player can battle a trainer
    public void addDefeatRequirement(String defeatRequirementTrainerId){
        this.defeatRequirements.add(defeatRequirementTrainerId);
    }

    public void removeDefeatRequirement(String defeatRequirementTrainerId){
        this.defeatRequirements.remove(defeatRequirementTrainerId);
    }

    public List<String> getDefeatRequirements(){
        return this.defeatRequirements;
    }

}
