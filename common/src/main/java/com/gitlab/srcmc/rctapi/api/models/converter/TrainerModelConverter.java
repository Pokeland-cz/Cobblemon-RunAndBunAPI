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
package com.gitlab.srcmc.rctapi.api.models.converter;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.gitlab.srcmc.rctapi.api.errors.RCTError;
import com.gitlab.srcmc.rctapi.api.errors.RCTErrors;
import com.gitlab.srcmc.rctapi.api.errors.RCTException;
import com.gitlab.srcmc.rctapi.api.models.TrainerModel;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerBag;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.api.util.Locations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;

/**
 * Defines a conversion from {@link TrainerModel} to {@link Trainer}.
 */
public class TrainerModelConverter implements Converter<TrainerModel, TrainerNPC> {
    private final MinecraftServer server;
    private final PokemonModelConverter pmc;

    /**
     * Creates a new trainer model converter.
     * 
     * @param server Minecraft server this converter is associated to.
     * @param pmc Model converter used for pokemon in trainer teams.
     */
    public TrainerModelConverter(@NotNull MinecraftServer server, @NotNull PokemonModelConverter pmc) {
        this.server = server;
        this.pmc = pmc;
    }

    @Override
    public TrainerNPC toTarget(@NotNull TrainerModel model, @NotNull RCTErrors<RCTException> errors) {
        if(model.getTeam().size() > 6) {
            errors.add(RCTError.of("too many pokemon in party " + model.getTeam().size() + "/6"));
        }

        BattleAI battleAI;

        if(model.getAI() == null) {
            errors.add(RCTError.of("unknown AI type"));
            battleAI = new RandomBattleAI();
        } else {
            battleAI = model.getAI().getInstanceFor(this.server);
        }

        var team = model.getTeam().stream().limit(6)
            .map(pkModel -> this.pmc.toTarget(pkModel, errors))
            .toList().toArray(new Pokemon[0]);

        var bag = new TrainerBag();

        model.getBag().forEach(bim -> {
            try {
                bag.add(Locations.withNamespace("cobblemon", bim.getItem()), bim.getQuantity());
            } catch(IllegalArgumentException e) {
                errors.add(RCTError.of(e));
            }
        });

        return new TrainerNPC(model.getName(), team, bag, battleAI, EntityType.VILLAGER.create(this.server.overworld()));
    }
}
