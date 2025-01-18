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
package com.gitlab.srcmc.rctapi.commands;

import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.RCTApi;

/**
 * Ingame commands provided by this mod.
 */
public final class RCTApiCommands {
    private static Set<CommandsContext> contexts = new HashSet<>();
    private RCTApiCommands() {}

    /**
     * Registers a default initialized {@link CommandsContext} instance with the 'rctapi' prefix.
     * 
     * @deprecated Use {@link RCTApiCommands#register(CommandsContext)} instead.
     */
    public static void register() {
        RCTApiCommands.register(ModCommon.MOD_ID);
    }
    
    /**
     * Registers a default initialized {@link CommandsContext} instance with the given
     * prefix.
     * 
     * @param prefix Prefix of the commands context.
     * @deprecated Use {@link RCTApiCommands#register(CommandsContext)} instead.
     */
    public static void register(String prefix) {
        RCTApiCommands.register(new CommandsContext() {
            @Override public int getWinCommandsPermission() { return 1; }
            @Override public String getPrefix() { return prefix; }
        });
    }
    
    /**
     * Registers a {@link CommandsContext} instance for the given prefix (should match
     * the id used to register a {@link RCTApi} instance).
     * 
     * @throws IllegalArgumentException If the {@link CommandsContext} instance was already registered.
     * @see {@link RCTApi#initInstance(String)}
     */
    public static void register(@NotNull CommandsContext context) {
        if(!RCTApiCommands.contexts.add(context)) {
            throw new IllegalArgumentException("Commands context '" + context.getPrefix() + "' already registered");
        }

        CommandRegistrationEvent.EVENT.register(context::onCommandRegistration);
    }
}
