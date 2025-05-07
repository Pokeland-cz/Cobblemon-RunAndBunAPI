/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2025, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctapi.api.trainer;

import java.util.ArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.api.util.Text;

/**
 * A trainer that is represented by a {@link ServerPlayer}.
 */
public class TrainerPlayer implements Trainer {
    private ServerPlayer player;
    private Text playerName;

    /**
     * Creates a trainer for the given {@link ServerPlayer} instance.
     * 
     * @param player {@link ServerPlayer} instance to associate with the trainer.
     */
    public TrainerPlayer(@NotNull ServerPlayer player) {
        this.player = player;
        this.playerName = new Text().setLiteral(player.getDisplayName().getString());
    }

    /**
     * Retrieves the {@link ServerPlayer} associated with this trainer.
     * 
     * @return {@link ServerPlayer} instance.
     */
    @NotNull
    public ServerPlayer getPlayer() {
        return this.player;
    }

    @Override @NotNull
    public Text getName() {
        return this.playerName;
    }

    @Override @NotNull
    public Pokemon[] getTeam() {
        var party = new ArrayList<Pokemon>();
        Cobblemon.INSTANCE.getStorage().getParty(this.player).forEach(party::add);
        return party.toArray(new Pokemon[party.size()]);
    }

    @Override @NotNull
    public LivingEntity getEntity() {
        return this.getPlayer();
    }
}
