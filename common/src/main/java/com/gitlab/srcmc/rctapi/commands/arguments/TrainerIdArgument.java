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
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.resources.ResourceLocation;

public class TrainerIdArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "foo:bar/baz");

    private TrainerIdArgument() {
    }

    public static TrainerIdArgument id() {
        return new TrainerIdArgument();
    }

    public String parse(StringReader stringReader) throws CommandSyntaxException {
        return readId(stringReader);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static String readId(StringReader stringReader) {
        int i = stringReader.getCursor();

        while(stringReader.canRead() && ResourceLocation.isAllowedInResourceLocation(stringReader.peek())) {
            stringReader.skip();
        }

        return stringReader.getString().substring(i, stringReader.getCursor());
    }
}
