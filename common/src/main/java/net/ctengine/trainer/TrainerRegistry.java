package net.ctengine.trainer;

import net.ctengine.CTEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TrainerRegistry {
    // Initialise as null in case we need to check if it has been properly initialised or not
    private static List<Trainer> trainers = null;

    public static List<Trainer> getTrainers(){
        return (trainers == null) ? null : Collections.unmodifiableList(trainers);
    }

    public static Trainer getTrainer(String id){
        if (trainers != null){
            for (Trainer trainer : trainers){
                if (Objects.equals(trainer.getId(), id)){
                    return trainer;
                }
            }
        }
        return null;
    }

    public static void removeTrainer(Trainer trainer){
        if (trainers != null){
            trainer.deleteJSONFile();
            trainers.remove(trainer);
            WinRegistry.removeAllWins(trainer);
            WinRegistry.save();
        }
    }

    public static void removeTrainer(String id){
        if (trainers != null){
            for (Trainer trainer : trainers){
                if (Objects.equals(trainer.getId(), id)){
                    removeTrainer(trainer);
                }
            }
        }
    }

    // Trainer class is protected so people are forced to use createTrainer()
    // This ensures that the trainer is properly loaded into the TrainerRegistry
    // Sometimes we don't need to save the trainer (i.e loading in from a datapack)
    public static Trainer createTrainer(String id, boolean saveOnCreation){
        if (trainers == null){
            trainers = new ArrayList<>();
        }
        Trainer trainer = new Trainer(id);
        trainers.add(trainer);
        if(saveOnCreation) trainer.save();
        return trainer;
    }

    public static Trainer createTrainer(String id){
        return createTrainer(id, false);
    }

    public static void save(){
        List<Trainer> registryTrainers = TrainerRegistry.getTrainers();
        if (registryTrainers != null){
            CTEngine.LOGGER.info("Saving: "+registryTrainers.size()+" trainers");
            for (Trainer trainer : TrainerRegistry.getTrainers()){
                trainer.save();
            }
        }
    }

    // This is to reset the registry to the default of null.
    // Needed otherwise swapping between singleplayer worlds caches the data
    public static void resetRegistry(){
        trainers = null;
    }
}
