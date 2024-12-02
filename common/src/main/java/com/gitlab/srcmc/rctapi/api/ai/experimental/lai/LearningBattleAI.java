/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers API. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctapi.api.ai.experimental.lai;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BagItemActionResponse;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager.TrainerEntityBattleActor;

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
            this.battleMemory.getOrAdd(pkmn, candidate, () -> {
                candidate.setWillBeSwitchedIn(true);
                return new SwitchActionResponse(candidate.getUuid());
            });
        }

        if(!forceSwitch && pkmn.hasPokemon()) {
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
            
            // all possible move usages
            if(moveset != null) {
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
                            if(target.hasPokemon()) {
                                this.battleMemory.getOrAdd(pkmn, move, target, () -> new MoveActionResponse(move.id, target.getPNX(), null));
                            }
                        }
                    }
                }
            }
        }

        // retrieve best known action
        return this.battleMemory.getChoice().orElse(PassActionResponse.INSTANCE);
    }
}
