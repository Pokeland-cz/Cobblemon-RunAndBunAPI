package net.ctengine.api.trainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Key/value trainer storage.
 */
public class TrainerRegistry {
    private Map<String, Trainer> trainers = new HashMap<>();

    /**
     * Registers the given trainer instance to the trainer registry.
     * 
     * @param trainerId Unique id of the trainer to register.
     * @param trainer Trainer instance to register.
     * @throws IllegalArgumentException If a trainer with the given id is already registered. (TODO: change to not found)
     * @throws IllegalArgumentException If the trainer neither is or extends from TrainerPlayer or TrainerNPC.
     */
    public void register(String trainerId, Trainer trainer) {
        if(this.isRegistered(trainerId)) {
            throw new IllegalArgumentException(String.format("trainer already registered '%s'", trainerId));
        }

        if(!(trainer instanceof TrainerPlayer) && !(trainer instanceof TrainerNPC)) {
            throw new IllegalArgumentException(String.format("invalid trainer type %s ('%s'), must extend from %s or implement %s", trainer.getClass().getName(), trainerId, TrainerPlayer.class.getName(), TrainerNPC.class.getName()));
        }

        this.trainers.put(trainerId, trainer);
    }

    /**
     * Unregisters the trainer with the given id.
     * 
     * @param trainerId Id of the trainer to unregister.
     * @return Boolean indicating if the trainer was unregistered.
     */
    public boolean unregister(String trainerId) {
        return this.trainers.remove(trainerId) != null;
    }

    /**
     * Reports if a trainer is registered.
     * 
     * @param trainerId Id of the trainer to check.
     * @return Boolean indicating if the trainer is registered.
     */
    public boolean isRegistered(String trainerId) {
        return this.trainers.containsKey(trainerId);
    }

    /**
     * Retrieves an unmodifiable set of all trainer ids.
     * 
     * @return Set of registered trainer ids.
     */
    public Set<String> getTrainerIds() {
        return this.trainers.keySet();
    }

    /**
     * Retrieves the trainer with the given id.
     * 
     * @param trainerId Id of the trainer to retrieve.
     * @return Trainer instance from the trainer registry.
     */
    public Trainer getTrainer(String trainerId) {
        return this.getTrainer(trainerId, Trainer.class);
    }

    /**
     * Retrieves the trainer with the given id as the specified type.
     * 
     * @param <T> Target trainer type.
     * @param trainerId Id of the trainer to retrieve.
     * @param type Target trainer type class instance.
     * @throws IllegalArgumentException If a trainer with the given id is not registered. (TODO: change to not found)
     * @throws IllegalArgumentException If the target type neither is or extends from TrainerPlayer or TrainerNPC.
     * @return Trainer instance from the trainer registry.
     */
    public <T extends Trainer> T getTrainer(String trainerId, Class<T> type) {
        if(!this.isRegistered(trainerId)) {
            throw new IllegalArgumentException(String.format("no such trainer registered '%s'", trainerId));
        }

        var trainer = this.trainers.get(trainerId);

        if(!type.isInstance(trainer)) {
            throw new IllegalArgumentException(String.format("invalid trainer type '%s' for '%s', expected '%s'", trainer.getClass().getName(), trainerId, type.getName()));
        }

        return type.cast(trainer);
    }

    /**
     * Removes all registered trainers.
     */
    public void clear() {
        this.trainers.clear();
    }
}
