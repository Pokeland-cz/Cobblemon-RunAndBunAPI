package net.ctengine.api.util;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import net.ctengine.api.CTEngine;
import net.ctengine.api.battle.BattleFormat;
import net.ctengine.api.battle.BattleManager;
import net.ctengine.api.battle.BattleRules;
import net.ctengine.api.battle.BattleState;
import net.ctengine.api.trainer.Trainer;

/**
 * Utility class for easy access to the api provided by the {@link BattleManager}
 * instance of the {@link CTEngine} singleton.
 */
public final class Battles {
    private Battles() {
    }

    /**
     * @see {@link BattleManager#start(List, List, BattleFormat)}
     */
    public static boolean start(
        @NotNull List<Trainer> participants1,
        @NotNull List<Trainer> participants2,
        @NotNull BattleFormat battleFormat,
        @NotNull BattleRules battleRules)
    {
        return CTEngine.getInstance().getBattleManager().start(participants1, participants2, battleFormat, battleRules);
    }

    /**
     * Starts a new pokemon battle in the GEN_9_SINGLES format.
     * 
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     * @param participant1 {@link Trainer} participant of one side.
     * @param participant2 {@link Trainer} participant of the other side.
     * @param battleRules {@link BattleRules} enforced on the battle.
     * @return True if the battle was started.
     */
    public static boolean start(
        @NotNull Trainer participant1,
        @NotNull Trainer participant2,
        @NotNull BattleRules battleRules)
    {
        return CTEngine.getInstance().getBattleManager().start(
            List.of(participant1),
            List.of(participant2),
            BattleFormat.GEN_9_SINGLES,
            battleRules);
    }

    /**
     * @see {@link BattleManager#getState(UUID)}.
     */
    public static BattleState getState(UUID battleUUID) {
        return CTEngine.getInstance().getBattleManager().getState(battleUUID);
    }
}
