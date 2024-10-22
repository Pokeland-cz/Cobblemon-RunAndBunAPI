package net.ctengine.api;

import java.util.function.Supplier;

import net.ctengine.api.battle.BattleManager;
import net.ctengine.api.trainer.TrainerRegistry;

/**
 * API entrypoint (see CTEngine#init() and CTEngine#getInstance()).
 */
public class CTEngine {
    private static Supplier<CTEngine> instanceSupplier = () -> {
        throw new IllegalStateException(CTEngine.class.getName() + " not initialized");
    };

    private TrainerRegistry trainerRegistry;
    private BattleManager battleManager;

    private CTEngine(TrainerRegistry trainerRegistry, BattleManager battleManager) {
        this.trainerRegistry = trainerRegistry;
        this.battleManager = battleManager;
    }

    /**
     * Retrieves the {@link TrainerRegistry}.
     * 
     * @return {@link TrainerRegistry} instance.
     */
    public TrainerRegistry getTrainerRegistry() {
        return this.trainerRegistry;
    }

    /**
     * Retrieves the {@link BattleManager}.
     * 
     * @return {@link BattleManager} instance.
     */
    public BattleManager getBattleManager() {
        return this.battleManager;
    }

    /**
     * Initializes a new singleton instance that can be accessed with {@link CTEngine#getInstance()}.
     * 
     * @param trainerRegistry {@link TrainerRegistry} to use.
     */
    public static void init(TrainerRegistry trainerRegistry) {
        var instance = new CTEngine(trainerRegistry, new BattleManager());
        instanceSupplier = () -> instance;
    }

    /**
     * Retrieves the singleton instance of the CTEngine service.
     * 
     * @return CTEngine singleton.
     */
    public static CTEngine getInstance() {
        return instanceSupplier.get();
    }
}
