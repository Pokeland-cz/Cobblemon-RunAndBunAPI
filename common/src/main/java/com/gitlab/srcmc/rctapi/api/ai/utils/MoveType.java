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
package com.gitlab.srcmc.rctapi.api.ai.utils;

import java.util.Map;

import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.battles.InBattleMove;

public enum MoveType {
    DAMAGE, STATUS, HEAL, CURE, BUFF, MALUS;

    public static MoveType of(InBattleMove move) {
        var mt = MOVE_TYPES.get(move.id);

        return mt == null
            ? (DamageCategories.INSTANCE.getSTATUS().getName().equals(TypeChart.getMove(move).getDamageCategory().getName())
                ? STATUS
                : DAMAGE) : mt;
    }

    // in practice only moves that could either target allies OR opponents matter
    private static final Map<String, MoveType> MOVE_TYPES = Map.<String, MoveType>ofEntries(
        Map.<String, MoveType>entry("healpulse", MoveType.HEAL),
        Map.<String, MoveType>entry("floralhealing", MoveType.HEAL),
        Map.<String, MoveType>entry("decorate", MoveType.BUFF)
    );
}
