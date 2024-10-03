package net.ctengine.trainer;

import net.ctengine.CTEngine;
import net.ctengine.util.CastingUtil;
import net.ctengine.util.JSONHandler;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// WinRegistry tracks what player has won against what trainer IDs
// If a trainer is deleted then their tracked win will also be deleted
public class WinRegistry {
    // Initialise as null in case we need to check if it has been properly initialised or not
    private static Map<String, List<String>> winMapper = null;


    // Initialise is mostly just safe casting checks
    // Probably not needed but is safest
    public static void initFromJSONContent(Map<String, Object> JSONContent){
        if(winMapper == null) winMapper = new HashMap<>();
        for (String trainerId : JSONContent.keySet()){
            ArrayList<String> newPlayerIdList = new ArrayList<>();
            if (JSONContent.get(trainerId) instanceof List<?> playerIdList){
                for (Object playerId : playerIdList){
                    newPlayerIdList.add(CastingUtil.safeCast(playerId, String.class, true));
                }
            }
            winMapper.put(trainerId, newPlayerIdList);
        }
    }

    public static void addWin(Trainer trainer, UUID playerId){
        if(winMapper == null) winMapper = new HashMap<>();
        List<String> playerIdList = winMapper.get(trainer.getId());
        String playerIdString = playerId.toString();
        if (playerIdList == null){
            List<String> newList = new ArrayList<>();
            newList.add(playerIdString);
            winMapper.put(trainer.getId(), newList);
        } else {
            if (!playerIdList.contains(playerIdString)) playerIdList.add(playerIdString);
        }
    }

    public static void removeWin(Trainer trainer, UUID playerId){
        String playerIdString = playerId.toString();
        if (winMapper != null){
            List<String> playerIdList = winMapper.get(trainer.getId());
            if (playerIdList != null) playerIdList.remove(playerIdString);
        }
    }

    public static void removeAllWins(Trainer trainer){
        if (winMapper != null) winMapper.remove(trainer.getId());
    }

    public static void save(){
        if (CTEngine.runServer != null && winMapper != null) {
            Path JSONPath = Paths.get(CTEngine.runServer.getSavePath(WorldSavePath.PLAYERDATA).getParent().toString(),
                    "ctengine", "winRegistry.json");

            JSONHandler.writeJSON(CastingUtil.rebuildMap(winMapper), JSONPath);
        }
    }

    public static boolean getWin(String trainerId, UUID playerId){
        if (winMapper != null){
            List<?> playerIdList = winMapper.get(trainerId);
            return playerIdList != null && playerIdList.contains(playerId.toString());
        }
        return false;
    }

    // This is to reset the registry to the default of null.
    // Needed otherwise swapping between singleplayer worlds caches the data
    public static void resetRegistry(){
        winMapper = null;
    }
}
