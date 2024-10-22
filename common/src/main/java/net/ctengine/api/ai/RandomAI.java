package net.ctengine.api.ai;

import java.util.List;
import java.util.Random;
import java.util.Set;

import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;

/**
 * A completely random AI.
 */
public class RandomAI extends CoreAI {
    private final Random RNG;

    public RandomAI() {
        this(System.currentTimeMillis());
    }

    public RandomAI(long seed) {
        this.RNG = new Random(seed);
    }

    @Override
    public BattlePokemon selectSwitch(List<BattlePokemon> candidates) {
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    @Override
    public InBattleMove selectMove(ActiveBattlePokemon pkmn, List<InBattleMove> candidates) {
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    @Override
    public String selectTarget(InBattleMove move, List<Targetable> candidates) {
        return candidates.isEmpty() ? null : candidates.get(RNG.nextInt(candidates.size())).getPNX();
    }

    @Override
    public BagItem selectItem(ActiveBattlePokemon pkmn, Set<BagItem> candidates) {
        return candidates.stream().skip(RNG.nextInt(candidates.size())).findFirst().get();
    }

    @Override
    public boolean shouldSwitch(ActiveBattlePokemon pkmn) {
        return RNG.nextDouble() < 0.15;
    }

    @Override
    public boolean shouldUseItem(ActiveBattlePokemon pkmn, Set<BagItem> items) {
        return RNG.nextDouble() < 0.15;
    }
}
