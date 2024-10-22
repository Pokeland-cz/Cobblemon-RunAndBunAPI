package net.ctengine.api.util;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static void start(
        @NotNull List<Trainer> participants1,
        @NotNull List<Trainer> participants2,
        @NotNull BattleFormat battleFormat,
        @NotNull BattleRules battleRules)
    {
        CTEngine.getInstance().getBattleManager().start(participants1, participants2, battleFormat, battleRules);
    }

    /**
     * @see {@link BattleManager#getState(UUID)}.
     */
    @Nullable
    public static BattleState getState(UUID battleUUID) {
        return CTEngine.getInstance().getBattleManager().getState(battleUUID);
    }
}
