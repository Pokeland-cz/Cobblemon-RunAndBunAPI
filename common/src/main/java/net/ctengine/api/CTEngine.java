package net.ctengine.api;

import java.util.function.Supplier;

import net.ctengine.api.battle.BattleManager;
import net.ctengine.api.trainer.TrainerRegistry;

/**
 * API entrypoint (see CTEngine#init() and CTEngine#getInstance()).
 */
public class CTEngine {
    private TrainerRegistry trainerRegistry;
    private BattleManager battleManager;

    private CTEngine(TrainerRegistry trainerRegistry, BattleManager battleManager) {
        this.trainerRegistry = trainerRegistry;
        this.battleManager = battleManager;
    }

    public TrainerRegistry getTrainerRegistry() {
        return this.trainerRegistry;
    }

    public BattleManager getBattleManager() {
        return this.battleManager;
    }

    private static Supplier<CTEngine> instanceSupplier = () -> {
        throw new IllegalStateException(CTEngine.class.getName() + " not initialized");
    };

    public static void init(TrainerRegistry trainerRegistry) {
        var instance = new CTEngine(trainerRegistry, new BattleManager());
        instanceSupplier = () -> instance;
    }

    public static CTEngine getInstance() {
        return instanceSupplier.get();
    }
}
