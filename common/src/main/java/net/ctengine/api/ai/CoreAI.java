package net.ctengine.api.ai;

import java.util.List;
import java.util.Set;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.DefaultActionResponse;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import net.ctengine.api.battle.BattleManager.TrainerEntityBattleActor;

/**
 * Abstract BattleAI that implements any forced actions (e.g. 'forced switch' or 'struggle').
 */
public abstract class CoreAI implements BattleAI {
    /**
     * Selects a pokemon to switch to.
     * 
     * @param candidates Non empty list of switch candidates.
     * @return Next pokemon to switch to.
     */
    public abstract BattlePokemon selectSwitch(List<BattlePokemon> candidates);

    /**
     * Selects a move to execute.
     * 
     * @param pkmn Active battle pokemon.
     * @param candidates Non empty list of move candidates.
     * @return Next move to execute.
     */
    public abstract InBattleMove selectMove(ActiveBattlePokemon pkmn, List<InBattleMove> candidates);

    /**
     * Selects the target for the given move.
     * 
     * @param candidates Target pokemon candidates (may be empty).
     * @return Target pokemon PNX.
     */
    public abstract String selectTarget(InBattleMove move, List<ActiveBattlePokemon> candidates);

    /**
     * Selects a bag item to used.
     * 
     * @param pkmn Active battle pokemon.
     * @param candidates Non empty set of usable bag items.
     * @return Next item to use.
     */
    public abstract BagItem selectItem(ActiveBattlePokemon pkmn, Set<BagItem> candidates);

    /**
     * Determines if the current active pokemon should be switched.
     * 
     * @param pkmn Active battle pokemon.
     * @return True if pokemon should switch.
     */
    public abstract boolean shouldSwitch(ActiveBattlePokemon pkmn);

    /**
     * Determines if an item from the bag should be used.
     * 
     * @param pkmn Active battle pokemon.
     * @param items Non empty set of usable bag items.
     * @return True if an item should be used.
     */
    public abstract boolean shouldUseItem(ActiveBattlePokemon pkmn, Set<BagItem> items);

    @Override
    public ShowdownActionResponse choose(ActiveBattlePokemon pkmn, ShowdownMoveset moveset, boolean forceSwitch) {
        var switchCandidates = pkmn.getActor()
            .getPokemonList().stream()
            .filter(BattlePokemon::canBeSentOut).toList();

        // (forced/consider) switch
        if(forceSwitch || pkmn.isGone() || (!switchCandidates.isEmpty() && (moveset == null || this.shouldSwitch(pkmn)))) {
            if(switchCandidates.isEmpty()) {
                return new DefaultActionResponse();
            }

            return new SwitchActionResponse(this.selectSwitch(switchCandidates).getUuid());
        }

        // consider item
        if(pkmn.getActor() instanceof TrainerEntityBattleActor actor) {
            var bagItemCandidates = actor.getBag().getItems();

            if(bagItemCandidates.size() > 0 && (moveset == null || this.shouldUseItem(pkmn, bagItemCandidates))) {
                var bagItem = this.selectItem(pkmn, bagItemCandidates);

                if(bagItem.canUse(pkmn.getBattle(), pkmn.getBattlePokemon())) {
                    actor.forceChoose(new BagItemActionResponse(actor.getBag().use(this.selectItem(pkmn, bagItemCandidates)), pkmn.getBattlePokemon(), pkmn.getBattlePokemon().getUuid().toString()));
                    return new ForcePassActionResponse();
                }
            }
        }

        // (forced) move
        if(moveset == null) {
            return PassActionResponse.INSTANCE;
        }

        // forced recharge (e.g. after hyperbeam)
        if(moveset.moves.size() == 1 && moveset.moves.get(0).getId().equals("recharge")) {
            return new MoveActionResponse("recharge", null, null);
        }

        var moveCandidates = moveset.moves.stream()
            .filter(InBattleMove::canBeUsed)
            .filter(move -> {
                var targetList = move.getTarget().getTargetList().invoke(pkmn);
                return move.mustBeUsed() || targetList == null || !targetList.isEmpty();
            }).toList();

        if(moveCandidates.isEmpty()) {
            return new MoveActionResponse("struggle", null, null);
        }

        var move = this.selectMove(pkmn, moveCandidates);
        String target = null;

        switch(move.getTarget()) {
            case normal:
            case foeSide:
                target = this.selectTarget(move, pkmn.getSide().getOppositeSide().getActivePokemon());
                break;
            case allySide:
                target = this.selectTarget(move, pkmn.getSidePokemon());
                break;
            default:
                break;
        }

        return new MoveActionResponse(move.id, target, null);
    }
}
