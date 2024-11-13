package net.ctengine.api.ai.learning;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import net.ctengine.api.battle.BattleManager.TrainerEntityBattleActor;

public class LearningBattleAI implements BattleAI {
    private final BattleMemory<ShowdownActionResponse> battleMemory;

    public LearningBattleAI(BattleMemory<ShowdownActionResponse> battleMemory) {
        this.battleMemory = battleMemory;
    }

    public ShowdownActionResponse choose(ActiveBattlePokemon pkmn, ShowdownMoveset moveset, boolean forceSwitch) {
        this.battleMemory.next(pkmn);

        // all possible switches
        var switchCandidates = pkmn.getActor()
            .getPokemonList().stream()
            .filter(BattlePokemon::canBeSentOut).toList();

        for(var candidate : switchCandidates) {
            this.battleMemory.getOrAdd(pkmn, candidate, () -> new SwitchActionResponse(candidate.getUuid()));
        }

        if(!forceSwitch) {
            // all possible item usages
            if(pkmn.getActor().canFitForcedAction() && pkmn.getActor() instanceof TrainerEntityBattleActor actor) {
                for(var candidate : actor.getBag().getItems()) {
                    for(var target : pkmn.getActor().getPokemonList()) {
                        if(candidate.canUse(pkmn.getBattle(), target)) {
                            this.battleMemory.getOrAdd(candidate, target, () -> {
                                actor.forceChoose(new BagItemActionResponse(actor.getBag().use(candidate), pkmn.getBattlePokemon(), pkmn.getBattlePokemon().getUuid().toString()));
                                return new ForcePassActionResponse();
                            });
                        }
                    }
                }
            }
            
            if(moveset != null) {
                // all possible move usages
                var moveCandidates = moveset.moves.stream().filter(InBattleMove::canBeUsed).toList();

                if(moveCandidates.isEmpty()) {
                    return new MoveActionResponse("struggle", null, null);
                }

                for(var move : moveCandidates) {
                    var targets = move.getTargets(pkmn);

                    if(targets == null) {
                        this.battleMemory.getOrAdd(pkmn, move, null, () -> new MoveActionResponse(move.id, null, null));
                    } else {
                        for(var target : targets) {
                            this.battleMemory.getOrAdd(pkmn, move, target, () -> new MoveActionResponse(move.id, target.getPNX(), null));
                        }
                    }
                }
            }
        }

        // retrieve best known action
        return this.battleMemory.getChoice();
    }
}
