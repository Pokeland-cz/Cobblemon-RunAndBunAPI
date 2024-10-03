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

public class Trainer {
    private final String id;
    private final List<TrainerPokemon> team = new ArrayList<>();
    private String displayName = null;
    private final List<String> defeatRequirements = new ArrayList<>();
    private String winCommand = null;
    private String lossCommand = null;
    private boolean canOnlyBeatOnce = false;

    // Trainer is protected so people have to use the TrainerRegistry to create a trainer.
    // This way trainers will always be loaded into the TrainerRegistry rather than having to manually add
    protected Trainer(String trainerID){
        this.id = trainerID;
    }

    // getBattleTeam is used when initiating a battle. We convert Trainer Pokemon to
    // Pokemon, then to BattlePokemon and build a team from that.
    public List<BattlePokemon> getBattleTeam() {
        List<BattlePokemon> battlePokemonList = new ArrayList<>();
        for (TrainerPokemon teamMember : this.team){
            Pokemon pokemon = teamMember.toNewPokemon();
            // When we get the battle team we want to register this new pokemon objects
            // in the isTrainerOwned list.
            TrainerPokemon.isTrainerOwned.add(pokemon.getUuid());
            battlePokemonList.add(new BattlePokemon(pokemon, pokemon, (pokemonEntity -> Unit.INSTANCE)));
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
            this.addTrainerPokemon(trainerPokemon);
        }
    }
    public void removeTrainerPokemon(TrainerPokemon trainerPokemon){
        if (trainerPokemon != null) {
            this.team.remove(trainerPokemon);
        }
    }
    public void removeMultipleTrainerPokemon(List<TrainerPokemon> trainerPokemonList){
        for (TrainerPokemon trainerPokemon : trainerPokemonList){
            this.removeTrainerPokemon(trainerPokemon);
        }
    }



    // Load the attributes from the read in JSON content
    public void initFromJSONContent(Map<String, Object> JSONContent){
        // Set display name
        String displayName = CastingUtil.safeCast(JSONContent.get("displayName"), String.class, true);
        this.setDisplayName((displayName != null) ? displayName : this.id);

        // Set defeat requirements
        if (JSONContent.get("defeatRequirements") instanceof List<?> defeatRequirementsList){
            for (Object defeatRequirementObj : defeatRequirementsList){
                String defeatRequirement = CastingUtil.safeCast(defeatRequirementObj, String.class, true);
                if(defeatRequirement != null) this.addDefeatRequirement(defeatRequirement);
            }
        }

        Boolean canOnlyBeatOnce = CastingUtil.safeCast(JSONContent.get("canOnlyBeatOnce"), Boolean.class, true);
        this.setCanOnlyBeatOnce((canOnlyBeatOnce != null) ? canOnlyBeatOnce : false);

        this.setWinCommand(CastingUtil.safeCast(JSONContent.get("winCommand"), String.class, true));
        this.setLossCommand(CastingUtil.safeCast(JSONContent.get("lossCommand"), String.class, true));

        // Set and build pokemon team
        if (JSONContent.get("team") instanceof List<?> trainerTeam){
            for (Object teamMember : trainerTeam){
                if (teamMember instanceof Map<?, ?> teamMemberInfo){
                    Map<String, Object> teamMemberInfoParsed = CastingUtil.rebuildMap(teamMemberInfo);

                    TrainerPokemon trainerPokemon = new TrainerPokemon();
                    trainerPokemon.initFromJSONContent(teamMemberInfoParsed);
                    this.addTrainerPokemon(trainerPokemon);
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
            JSONContent.put("canOnlyBeatOnce", this.canOnlyBeatOnce);

            if (this.winCommand != null) JSONContent.put("winCommand", this.winCommand);
            if (this.lossCommand != null) JSONContent.put("lossCommand", this.lossCommand);

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
    protected void deleteJSONFile() {
        Path JSONPath = Paths.get(CTEngine.runServer.getSavePath(WorldSavePath.PLAYERDATA).getParent().toString(),
                "ctengine", "trainers", this.id + ".json");
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



    public void setWinCommand(String winCommand){
        this.winCommand = winCommand;
    }
    public void removeWinCommand(){
        this.winCommand = null;
    }
    public String getWinCommand(){
        return this.winCommand;
    }



    public void setLossCommand(String lossCommand){
        this.lossCommand = lossCommand;
    }
    public void removeLossCommand(){
        this.lossCommand = null;
    }
    public String getLossCommand(){
        return this.lossCommand;
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


    // Will be used to check against the WinRegistry to see if the trainer
    // can be beaten multiple times by the same player
    public void setCanOnlyBeatOnce(boolean canOnlyBeatOnce){
        this.canOnlyBeatOnce = canOnlyBeatOnce;
    }
    public boolean getCanOnlyBeatOnce(){
        return this.canOnlyBeatOnce;
    }

}
