package net.ctengine.api.battle;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;

/**
 * Additional rules that can be imposed on trainer battles.
 */
public class BattleRules {
    protected int maxItemUses = -1;

    /**
     * Retrieves the max amount of items a {@link BattleActor} may use per battle. A
     * negative values implies that there is no limit.
     * 
     * @return Max number of item uses.
     */
    public int getMaxItemUses() {
        return this.maxItemUses;
    }
}
