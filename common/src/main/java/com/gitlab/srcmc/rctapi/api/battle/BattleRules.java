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

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;

/**
 * Additional rules that are enforced during trainer battles.
 */
public class BattleRules {
    protected int maxItemUses = -1;

    /**
     * Retrieves the max amount of items a {@link BattleActor} may use per battle. A
     * negative values implies that there is no limit.
     * 
     * @return Max number of item uses.
     */
    public int getMaxItemUses() {
        return this.maxItemUses;
    }
}
