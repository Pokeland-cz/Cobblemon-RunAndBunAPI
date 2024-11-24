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
package com.gitlab.srcmc.rctapi.api.battle;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor;
import com.cobblemon.mod.common.battles.AlreadyInBattleError;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.BattleStartError;
import com.cobblemon.mod.common.battles.ErroredBattleStart;

import net.minecraft.network.chat.Component;

/**
 * A service that is used by {@link BattleManager}s to validate the technical
 * correctness of battle contexts.
 */
public class BattleContextValidator {
    /**
     * Validates the given battle context and collects any errors that may occur. In
     * addition to checking for the correct amount of actors, party size, etc - in
     * accordance to the defined battle format - there is also the constraint that all
     * {@link BattleActor}s must implement {@link EntityBackedBattleActor} and may not
     * return null when retrieving the attached entity with {@link EntityBackedBattleActor#getEntity()}.
     * 
     * @param errors {@link ErroredBattleStart} to collect errors.
     * @param context {@link BattleContext} to check.
     * @return The provided {@link ErroredBattleStart} instance.
     */
    public ErroredBattleStart validate(ErroredBattleStart errors, BattleContext context) {
        var battleType = context.getBattleFormat().getCobblemonBattleFormat().getBattleType();
        var actorsPersSide = battleType.getActorsPerSide();
        var slotsPerActor = battleType.getSlotsPerActor();
        var actorIds = new HashSet<UUID>();

        for(var side : List.of(context.getBattleSide1(), context.getBattleSide2())) {
            if(side.getActors().length != actorsPersSide) {
                errors.getGeneralErrors().add(BattleStartError.Companion.incorrectActorCount(actorsPersSide, side.getActors().length));
            }

            for(var actor : side.getActors()) {
                if(actor instanceof EntityBackedBattleActor entityBacked && entityBacked.getEntity() != null) {
                    if(actor.getPokemonList().size() < slotsPerActor) {
                        errors.getParticipantErrors().get(actor).add(BattleStartError.Companion.insufficientPokemon(entityBacked.getEntity(), slotsPerActor, actor.getPokemonList().size()));
                    }

                    if(BattleRegistry.INSTANCE.getBattleByParticipatingPlayerId(entityBacked.getEntity().getUUID()) != null) {
                        errors.getParticipantErrors().get(actor).add(AlreadyInBattleError.Companion.alreadyInBattle(actor));
                    }

                    if(actorIds.contains(actor.getUuid())) {
                        errors.getParticipantErrors().get(actor).add(AlreadyInBattleError.Companion.alreadyInBattle(actor));
                    } else {
                        actorIds.add(actor.getUuid());
                    }
                } else {
                    errors.getParticipantErrors().get(actor).add(BattleStartError.Companion.canceledByEvent(Component.literal(String.format("%s is not attached to an entity", actor.getName().getString()))));
                }
            }
        }

        return errors;
    }
}
