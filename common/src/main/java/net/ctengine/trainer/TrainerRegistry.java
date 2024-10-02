package net.ctengine.trainer;

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
}
