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

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.world.entity.LivingEntity;

/**
 * Defines the required functionality of a trainer.
 */
public interface Trainer {
    /**
     * Retrieves the name of this trainer.
     * 
     * @return Trainer name.
     */
    @NotNull String getName();

    /**
     * Retrieves the {@link Pokemon} team of this trainer.
     * 
     * @return Array of {@link Pokemon}.
     */
    @NotNull Pokemon[] getTeam();

    /**
     * Retrieves the {@link LivingEntity} associated with this trainer.
     * 
     * @return {@link LivingEntity} associated with this trainer.
     */
    @NotNull LivingEntity getEntity();
}
