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
package com.gitlab.srcmc.rctapi.neoforge;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.ModRegistries;
import com.gitlab.srcmc.rctapi.commands.arguments.BattleEndCommandMapArgument;
import com.gitlab.srcmc.rctapi.commands.arguments.BattleRulesArgument;
import com.gitlab.srcmc.rctapi.commands.arguments.TrainerIdArgument;

@Mod(ModCommon.MOD_ID)
public final class NeoForgeCommon {
    public NeoForgeCommon(ModContainer container) {
        ModRegistries.init();
        container.getEventBus().addListener(this::onCommonSetup);
    }

    void onCommonSetup(FMLCommonSetupEvent event) {
        ArgumentTypeInfos.registerByClass(BattleEndCommandMapArgument.class, ModRegistries.ArgumentTypes.BATTLE_END_COMMAND_MAP.get());
        ArgumentTypeInfos.registerByClass(BattleRulesArgument.class, ModRegistries.ArgumentTypes.BATTLE_RULES.get());
        ArgumentTypeInfos.registerByClass(TrainerIdArgument.class, ModRegistries.ArgumentTypes.TRAINER_ID.get());
        ModCommon.init();
        // com.gitlab.srcmc.rctapi.example.ExampleMod.init(); // uncomment for example
    }
}
