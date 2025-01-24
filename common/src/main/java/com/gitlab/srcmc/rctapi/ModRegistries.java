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
package com.gitlab.srcmc.rctapi;

import com.gitlab.srcmc.rctapi.commands.arguments.BattleEndCommandMapArgument;
import com.gitlab.srcmc.rctapi.commands.arguments.BattleRulesArgument;
import com.gitlab.srcmc.rctapi.commands.arguments.TrainerIdArgument;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries {
    public class ArgumentTypes {
        public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.COMMAND_ARGUMENT_TYPE);
        public static final RegistrySupplier<ArgumentTypeInfo<BattleEndCommandMapArgument, ?>> BATTLE_END_COMMAND_MAP;
        public static final RegistrySupplier<ArgumentTypeInfo<BattleRulesArgument, ?>> BATTLE_RULES;
        public static final RegistrySupplier<ArgumentTypeInfo<TrainerIdArgument, ?>> TRAINER_ID;

        static {
            BATTLE_END_COMMAND_MAP = REGISTRY.register(location("battle_end_command_map"), () -> SingletonArgumentInfo.contextFree(BattleEndCommandMapArgument::map));
            BATTLE_RULES = REGISTRY.register(location("battle_rules"), () -> SingletonArgumentInfo.contextFree(BattleRulesArgument::battleRules));
            TRAINER_ID = REGISTRY.register(location("trainer_id"), () -> SingletonArgumentInfo.contextFree(TrainerIdArgument::id));
        }
    }

    public static void init() {
        ModRegistries.init(false);
    }

    public static void init(boolean skipArgumentType) {
        if(!ModRegistries.initialized) {
            if(!skipArgumentType) {
                ArgumentTypes.REGISTRY.register();
            }

            ModRegistries.initialized = true;
        }
    }

    public static ResourceLocation location(String key) {
        return ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, key);
    }

    private static boolean initialized;
    private ModRegistries() {}
}
