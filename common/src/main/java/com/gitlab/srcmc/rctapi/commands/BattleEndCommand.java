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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

import com.gitlab.srcmc.rctapi.ModCommon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class BattleEndCommand {
    private transient Supplier<String> titleSupplier = () -> ModCommon.MOD_ID;
    private transient Supplier<Integer> permissionSupplier = () -> 1;
    private String command = "";

    public void setPermissionSupplier(@NotNull Supplier<Integer> f) {
        this.permissionSupplier = f;
    }

    public void setTitleSupplier(@NotNull Supplier<String> f) {
        this.titleSupplier = f;
    }

    public void execute(@NotNull MinecraftServer server, @NotNull LivingEntity... actorEntities) {
        var c = this.command.trim();
        var pos = new Vec3(0, 0, 0);
        LivingEntity source = null;

        for(int i = 0; i < actorEntities.length; i++) {
            var uuid = actorEntities[i].getUUID();
            var player = server.getPlayerList().getPlayer(uuid);
            var repl = player != null ? player.getName().getString() : uuid.toString();
            var id = "@" + (i + 1);

            if(source == null && c.startsWith(id)) {
                source = actorEntities[i];
                c = c.substring(id.length()).trim();
            }

            c = c.replaceAll(id, repl);
            pos = pos.add(actorEntities[i].getEyePosition());
        }
        
        var cmds = server.getCommands();

        if(actorEntities.length > 0) {
            pos = pos.scale(1.0/actorEntities.length);
        }

        try {
            cmds.getDispatcher().execute(c, this.createCommandSourceStack(server, pos, source));
        } catch (CommandSyntaxException e) {
            ModCommon.LOG.error(e.getMessage(), e);
        }
    }

    private CommandSourceStack createCommandSourceStack(MinecraftServer server, Vec3 pos, LivingEntity source) {
        var level = server.overworld();
        var permission = this.permissionSupplier.get();
        var title = this.titleSupplier.get();

        return new CommandSourceStack(
            source != null ? source : server,
            source != null ? source.position() : pos, Vec2.ZERO, level, permission,
            source != null ? source.getName().getString() : title,
            source != null ? source.getDisplayName() : Component.literal(title), server, source).withSuppressedOutput();
    }

    public static class Map extends HashMap<Integer, BattleEndCommand[]> {}
    
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(BattleEndCommand.class, new BattleEndCommand.Deserializer())
        .create();

    public static class Deserializer implements JsonDeserializer<BattleEndCommand> {
        @Override
        public BattleEndCommand deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var c = new BattleEndCommand();
            c.command = json.getAsString();
            return c;
        }
    }
}
