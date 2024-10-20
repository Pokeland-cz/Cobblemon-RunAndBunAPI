package net.ctengine.api.ai;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;

/**
 * Available {@link BattleAI} implementations.
 */
public enum AIType {
    /**
     * Completely random AI.
     */
    RNG(new RandomAI()),
    
    /**
     * Selfdots Gen5 AI from CobblemonTrainers.
     */
    SDG5(new SelfdotGen5AI());

    /**
     * BattleAI instance.
     */
    public final BattleAI INSTANCE;

    AIType(BattleAI battleAI) {
        this.INSTANCE = battleAI;
    }
}
