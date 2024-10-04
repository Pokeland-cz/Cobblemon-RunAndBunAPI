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
import java.util.*;

public class Trainer {
    private final String id;
    private final List<TrainerPokemon> team = Arrays.asList(null, null, null, null, null, null);
    private String displayName = null;
    private final List<String> mustHaveDefeated = new ArrayList<>();
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
            if (teamMember != null) {
                Pokemon pokemon = teamMember.toNewPokemon();
                // When we get the battle team we want to register this new pokemon objects
                // in the isTrainerOwned list.
                TrainerPokemon.isTrainerOwned.add(pokemon.getUuid());
                battlePokemonList.add(new BattlePokemon(pokemon, pokemon, (pokemonEntity -> Unit.INSTANCE)));
            }
        }
        return battlePokemonList;
    }



    public void setTeamMember(int teamIndex, TrainerPokemon trainerPokemon){
        if (teamIndex >= 0 && teamIndex < 6){
            this.team.set(teamIndex, trainerPokemon);
        } else {
            CTEngine.LOGGER.info("team index must be between 0-6");
        }
    }

    public void removeTeamMember(int teamIndex){
        if (teamIndex >= 0 && teamIndex < 6){
            this.team.set(teamIndex, null);
        } else {
            CTEngine.LOGGER.info("team index must be between 0-6");
        }
    }


    // Load the attributes from the read in JSON content
    public void initFromJSONContent(Map<String, Object> JSONContent){
        // Set display name
        String displayName = CastingUtil.safeCast(JSONContent.get("displayName"), String.class, true);
        this.setDisplayName((displayName != null) ? displayName : this.id);

        // Set defeat requirements
        if (JSONContent.get("mustHaveDefeated") instanceof List<?> mustHaveDefeatedList){
            for (Object mustHaveDefeatedObj : mustHaveDefeatedList){
                String mustHaveDefeated = CastingUtil.safeCast(mustHaveDefeatedObj, String.class, true);
                if(mustHaveDefeated != null) this.addMustHaveDefeated(mustHaveDefeated);
            }
        }

        Boolean canOnlyBeatOnce = CastingUtil.safeCast(JSONContent.get("canOnlyBeatOnce"), Boolean.class, true);
        this.setCanOnlyBeatOnce((canOnlyBeatOnce != null) ? canOnlyBeatOnce : false);

        this.setWinCommand(CastingUtil.safeCast(JSONContent.get("winCommand"), String.class, true));
        this.setLossCommand(CastingUtil.safeCast(JSONContent.get("lossCommand"), String.class, true));

        // Set and build pokemon team
        if (JSONContent.get("team") instanceof List<?> teamMembers){
            for (int i = 0; i < teamMembers.size(); i++) {
                if (teamMembers.get(i) != null && teamMembers.get(i) instanceof Map<?, ?> teamMemberInfo){
                    Map<String, Object> teamMemberInfoParsed = CastingUtil.rebuildMap(teamMemberInfo);

                    TrainerPokemon trainerPokemon = new TrainerPokemon();
                    trainerPokemon.initFromJSONContent(teamMemberInfoParsed);

                    this.team.set(i,trainerPokemon);
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
            JSONContent.put("mustHaveDefeated",this.mustHaveDefeated);
            JSONContent.put("canOnlyBeatOnce", this.canOnlyBeatOnce);

            if (this.winCommand != null) JSONContent.put("winCommand", this.winCommand);
            if (this.lossCommand != null) JSONContent.put("lossCommand", this.lossCommand);

            List<Map<String, Object>> parsedTeam = Arrays.asList(null, null, null, null, null, null);
            for (int i = 0; i < this.team.size(); i++) {
                if (this.team.get(i) != null){
                    parsedTeam.set(i,this.team.get(i).getJSONContent());
                }
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
    public void addMustHaveDefeated(String trainerId){
        this.mustHaveDefeated.add(trainerId);
    }
    public void removeMustHaveDefeated(String trainerId){
        this.mustHaveDefeated.remove(trainerId);
    }
    public List<String> getMustHaveDefeated(){
        return this.mustHaveDefeated;
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
