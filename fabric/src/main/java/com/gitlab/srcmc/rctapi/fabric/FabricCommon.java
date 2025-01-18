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
package com.gitlab.srcmc.rctapi.fabric;

import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.ModRegistries;
import com.gitlab.srcmc.rctapi.commands.arguments.BattleEndCommandMapArgument;
import com.gitlab.srcmc.rctapi.commands.arguments.BattleRulesArgument;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;

public final class FabricCommon implements ModInitializer {
    public FabricCommon() {
        ModRegistries.init();
    }

    @Override
    public void onInitialize() {
        ModCommon.init();
        // com.gitlab.srcmc.rctapi.example.ExampleMod.init(); // uncomment for example

        ArgumentTypeRegistry.registerArgumentType(
            ModRegistries.ArgumentTypes.BATTLE_RULES.getId(),
            BattleRulesArgument.class,
            ModRegistries.ArgumentTypes.BATTLE_RULES.get());

        ArgumentTypeRegistry.registerArgumentType(
            ModRegistries.ArgumentTypes.BATTLE_END_COMMAND_MAP.getId(),
            BattleEndCommandMapArgument.class,
            ModRegistries.ArgumentTypes.BATTLE_END_COMMAND_MAP.get());
    }
}
