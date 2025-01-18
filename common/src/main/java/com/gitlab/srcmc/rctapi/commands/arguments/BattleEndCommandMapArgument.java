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
package com.gitlab.srcmc.rctapi.commands.arguments;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.gitlab.srcmc.rctapi.commands.BattleEndCommand;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.nbt.TagParser;

public class BattleEndCommandMapArgument implements ArgumentType<BattleEndCommand.Map> {
    private static final Collection<String> VALUES = List.of("{1: []}", "{2: []}", "{1: [], 2: []}");
    private static final Collection<String> EXAMPLES = List.of("{1: ['give @1 minecraft:diamond']}", "{1: ['kill @2', 'loot spawn ~ ~ ~ loot minecraft:chests/simple_dungeon'], 2: ['kill @2']}");

    private BattleEndCommandMapArgument() {
    }

    public static BattleEndCommandMapArgument map() {
        return new BattleEndCommandMapArgument();
    }

    public BattleEndCommand.Map parse(StringReader stringReader) throws CommandSyntaxException {
        try {
            var p = new TagParser(stringReader);
            return BattleEndCommand.GSON.fromJson(p.readStruct().getAsString(), BattleEndCommand.Map.class);
        } catch(JsonSyntaxException e) {
            throw ComponentArgument.ERROR_INVALID_JSON.createWithContext(stringReader, e.getMessage());
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return commandContext.getSource() instanceof SharedSuggestionProvider
            ? SharedSuggestionProvider.suggest(VALUES, suggestionsBuilder)
            : Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
