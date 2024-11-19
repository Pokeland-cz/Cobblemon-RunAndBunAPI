package net.ctengine.api;

import java.util.function.Supplier;

import net.ctengine.api.battle.BattleManager;
import net.ctengine.api.trainer.TrainerRegistry;

/**
 * API entrypoint (see {@link RCTApi#init(TrainerRegistry, BattleManager)} and
 * {@link RCTApi#getInstance()})).
 */
public class RCTApi {
    private static Supplier<RCTApi> instanceSupplier = () -> {
        var instance = new RCTApi(new TrainerRegistry(), new BattleManager());
        instanceSupplier = () -> instance;
        return instance;
    };

    private TrainerRegistry trainerRegistry;
    private BattleManager battleManager;

    private RCTApi(TrainerRegistry trainerRegistry, BattleManager battleManager) {
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
     * Initializes a new singleton instance that can be accessed with {@link
     * RCTApi#getInstance()}.
     * 
     * @param trainerRegistry {@link TrainerRegistry} to use.
     * @param battleManager {@link BattleManager} to use.
     */
    public static void init(TrainerRegistry trainerRegistry, BattleManager battleManager) {
        var instance = new RCTApi(trainerRegistry, battleManager);
        instanceSupplier = () -> instance;
    }

    /**
     * Retrieves the singleton instance of the {@link RCTApi} service.
     * 
     * @return {@link RCTApi} singleton.
     */
    public static RCTApi getInstance() {
        return instanceSupplier.get();
    }
}
