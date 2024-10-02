package net.ctengine.trainer;

import net.ctengine.CTEngine;
import net.ctengine.util.JSONHandler;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TrainerInitialiser {
    public static void initialiseTrainersFromJSON(){
        if (CTEngine.runServer != null){
            CTEngine.LOGGER.info("Loading Trainers from JSON files...");

            Path worldSavePath = Paths.get(CTEngine.runServer.getSavePath(WorldSavePath.PLAYERDATA).getParent().toString(),
                    "ctengine", "trainers");

            try {
                List<Path> JSONPaths = JSONHandler.getAllJsonFiles(worldSavePath);
                for (Path JSONPath : JSONPaths){
                    String fileName = JSONPath.getFileName().toString();
                    String trainerID = fileName.substring(0, fileName.length() - 5);
                    Trainer trainer = TrainerRegistry.createTrainer(trainerID, false);
                    Map<String, Object> JSONContent = JSONHandler.readJSON(JSONPath);
                    trainer.initFromJSONContent(JSONContent);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            CTEngine.LOGGER.info("ERROR: Trainers could not be loaded as runServer is null.");
        }
    }

    public static void initialiseTrainersFromDatapack(){
        if (CTEngine.runServer != null){
            CTEngine.LOGGER.info("Loading Trainers from Datapack files...");

            // idk do datapack stuff
        }
    }
}
