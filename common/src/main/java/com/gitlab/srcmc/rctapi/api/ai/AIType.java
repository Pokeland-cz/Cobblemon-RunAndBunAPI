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
package com.gitlab.srcmc.rctapi.api.ai;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;

import com.gitlab.srcmc.rctapi.api.ai.learning.BattleMemory;
import com.gitlab.srcmc.rctapi.api.ai.learning.LearningBattleAI;
import net.minecraft.server.MinecraftServer;

/**
 * Available {@link BattleAI} implementations.
 */
public enum AIType {
    /**
     * Learning AI (supports item usage).
     */
    LAI(server -> new LearningBattleAI(BattleMemory.load(server))),
    
    /**
     * Selfdots Gen5 AI from CobblemonTrainers.
     */
    SD5(server -> new SelfdotGen5AI()),

    /**
     * Experimental Cobblemon AI (easy)
     */
    CBE(server -> new StrongBattleAI(0)),

    /**
     * Experimental Cobblemon AI (medium)
     */
    CBM(server -> new StrongBattleAI(3)),

    /**
     * Experimental Cobblemon AI (hard)
     */
    CBH(server -> new StrongBattleAI(5)),

    /**
     * Completely random AI.
     */
    RNG(server -> new RandomBattleAI());

    private final Function<MinecraftServer, BattleAI> supplier;
    private MinecraftServer server;
    private BattleAI instance;

    AIType(Function<MinecraftServer, BattleAI> supplier) {
        this.supplier = supplier;
    }

    /**
     * Retrieves a {@link BattleAI} instance associated to the given server. Subsequent
     * calls will return the same instance if the same server is provided otherwise a
     * new instance will be created.
     * 
     * @param server Associated {@link MinecraftServer}.
     * @return {@link BattleAI} instance.
     */
    @NotNull
    public BattleAI getInstanceFor(@NotNull MinecraftServer server) {
        if(server != this.server) {
            this.instance = supplier.apply(server);
            this.server = server;
        }
        
        return this.instance;
    }
}
