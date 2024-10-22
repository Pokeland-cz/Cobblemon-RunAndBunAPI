package net.ctengine.api.battle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

/**
 * Contains information about an ongoing trainer battle.
 */
public class BattleState {
    private final Map<UUID, ActorState> actorStates = new HashMap<>();
    private final PokemonBattle battle;
    private final BattleRules rules;

    /**
     * Creates a new battle state for the given {@link PokemonBattle}.
     * 
     * @param battle {@link PokemonBattle} referenced by this battle state.
     * @param rules {@link BattleRules} enforced on the battle.
     */
    public BattleState(PokemonBattle battle, BattleRules rules) {
        this.battle = battle;
        this.rules = rules;
    }

    /**
     * Retrieves an {@link ActorState} from this battle state.
     * 
     * @param actorUUID UUID of the {@link BattleActor}.
     * @return The {@link ActorState} of the {@link BattleActor}.
     */
    @NotNull
    public ActorState getState(UUID actorUUID) {
        return this.actorStates.computeIfAbsent(actorUUID, k -> new ActorState());
    }

    /**
     * Retrieves the {@link PokemonBattle} referenced by this battle state.
     * 
     * @return {@link PokemonBattle} referenced by this battle state.
     */
    public PokemonBattle getBattle() {
        return this.battle;
    }

    /**
     * Retrieves the {@link BattleRules} enforced on the battle.
     * 
     * @return {@link BattleRules} enforced on the battle.
     */
    public BattleRules getRules() {
        return this.rules;
    }

    /**
     * Defines the state of a {@link BattleActor} within an active trainer battle.
     */
    public class ActorState {
        private int itemsUsed;

        /**
         * Sets the amount of items used by the {@link BattleActor} during the battle.
         * 
         * @param itemsUsed Number of items used.
         */
        public void setItemsUsed(int itemsUsed) {
            this.itemsUsed = itemsUsed;
        }

        /**
         * Retrieves the amount of items used by the {@link BattleActor} during the battle.
         * 
         * @return Number of items used.
         */
        public int getItemsUsed() {
            return this.itemsUsed;
        }
    }
}
