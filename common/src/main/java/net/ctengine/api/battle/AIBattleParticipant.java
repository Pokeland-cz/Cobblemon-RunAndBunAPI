package net.ctengine.api.battle;

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;

public interface AIBattleParticipant extends BattleParticipant {
    @NotNull BattleAI getBattleAI();
}
