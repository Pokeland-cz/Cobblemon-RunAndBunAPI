package net.ctengine.api.trainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.ctengine.api.battle.BattleParticipant;

public class TrainerRegistry {
    private Map<String, BattleParticipant> trainers = new HashMap<>();

    public void register(String trainerId, BattleParticipant participant) {
        if(this.isRegistered(trainerId)) {
            throw new IllegalArgumentException(String.format("trainer already registered '%s'", trainerId));
        }

        this.trainers.put(trainerId, participant);
    }

    public boolean unregister(String trainerId) {
        return this.trainers.remove(trainerId) != null;
    }

    public boolean isRegistered(String trainerId) {
        return this.trainers.containsKey(trainerId);
    }

    public Set<String> getTrainerIds() {
        return this.trainers.keySet();
    }

    public BattleParticipant getTrainer(String trainerId) {
        return this.getTrainer(trainerId, BattleParticipant.class);
    }

    public <T extends BattleParticipant> T getTrainer(String trainerId, Class<T> type) {
        if(!this.isRegistered(trainerId)) {
            throw new IllegalArgumentException(String.format("no such trainer registered '%s'", trainerId));
        }

        var trainer = this.trainers.get(trainerId);

        if(!type.isInstance(trainer)) {
            throw new IllegalArgumentException(String.format("invalid trainer type '%s' for '%s', expected '%s'", trainer.getClass().getName(), trainerId, type.getName()));
        }

        return type.cast(trainer);
    }

    public void clear() {
        this.trainers.clear();
    }
}
