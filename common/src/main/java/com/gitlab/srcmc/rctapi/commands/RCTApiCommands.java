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
package com.gitlab.srcmc.rctapi.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.battle.BattleFormat;
import com.gitlab.srcmc.rctapi.api.battle.BattleRules;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/**
 * Ingame commands provided by this mod.
 */
public final class RCTApiCommands {
    private static Gson GSON = new Gson();

    private static String prefix;

    private static final String CMD_BATTLE = "battle";
    private static final String ARG_PARTICIPANT = "participant";
    private static final String ARG_VS = "vs";
    private static final String ARG_RULES = "rules";
    
    private static final String CMD_ATTACH = "attach";
    private static final String ARG_TRAINER_ID = "trainerId";
    private static final String ARG_TRAINER_ENTITY = "trainerEntity";

    private RCTApiCommands() {}

    /**
     * Prepares commands for registration and sets the command prefix to 'rctapi'.
     */
    public static void register() {
        RCTApiCommands.register(ModCommon.MOD_ID);
    }

    /**
     * Prepares commands for registration and sets the command prefix.
     * 
     * @param prefix Prefix of all commands.
     */
    public static void register(String prefix) {
        RCTApiCommands.prefix = prefix;
        CommandRegistrationEvent.EVENT.register(RCTApiCommands::onCommandRegistration);
    }

    static void onCommandRegistration(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, CommandSelection env) {
        var builder = Commands.literal(CMD_BATTLE);

        for(var format : BattleFormat.values()) {
            builder.then(builderFormat(format, false, false));
            builder.then(builderFormat(format, false, true));
            builder.then(builderFormat(format, true, false));
            builder.then(builderFormat(format, true, true));
        }

        dispatcher.register(Commands.literal(RCTApiCommands.prefix)
            .requires(css -> css.hasPermission(2))
            .then(Commands.literal(CMD_ATTACH)
                .then(Commands
                    .argument(ARG_TRAINER_ID, StringArgumentType.string())
                    .suggests(RCTApiCommands::get_trainer_id_suggestions)
                    .then(Commands.argument(ARG_TRAINER_ENTITY, EntityArgument.entity())
                        .executes(RCTApiCommands::attach))))
            .then(builder));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> builderFormat(BattleFormat format, boolean entities1, boolean entities2) {
        var battleType = format.getCobblemonBattleFormat().getBattleType();

        return Commands.literal(format.name())
            .then(builderParticipants(format, 0, 0, battleType.getActorsPerSide(), false, entities1, entities2))
            .then(builderParticipants(format, 0, 0, battleType.getActorsPerSide(), true, entities1, entities2));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> builderParticipants(BattleFormat format, int side, int actor, int actorsPerSide, boolean withRules, boolean entities1, boolean entities2) {
        if(actor < actorsPerSide) {
            var arg = entities1
                ? Commands.argument(getParticipantEntityId(side, actor), EntityArgument.entity())
                : RequiredArgumentBuilder
                    .<CommandSourceStack, String>argument(getParticipantId(side, actor), StringArgumentType.string())
                    .suggests(RCTApiCommands::get_trainer_id_suggestions);

            return actor + 1 < actorsPerSide
                ? arg.then(builderParticipants(format, side, actor + 1, actorsPerSide, withRules, entities1, entities2)) : side < 1
                ? arg.then(Commands.literal(ARG_VS).then(builderParticipants(format, side + 1, 0, actorsPerSide, withRules, entities2, entities1)))
                : withRules
                    ? arg.then(Commands.argument(ARG_RULES, NbtTagArgument.nbtTag()).executes(context -> RCTApiCommands.battle(context, format, context.getArgument(ARG_RULES, Tag.class))))
                    : arg.executes(context -> RCTApiCommands.battle(context, format, null));
        }

        throw new IllegalArgumentException("invalid battle actor index: " + actor);
    }

    private static String getParticipantId(int side, int actor) {
        return String.format("%s_%d_%d", ARG_PARTICIPANT, side, actor);
    }

    private static String getParticipantEntityId(int side, int actor) {
        return String.format("%s_%d_%d_E", ARG_PARTICIPANT, side, actor);
    }

    private static int handleError(CommandContext<CommandSourceStack> context, Exception e) {
        ModCommon.LOG.error(e.getMessage(), e);
        context.getSource().sendFailure(Component.nullToEmpty(e.getMessage()));
        return 1;
    }

    private static CompletableFuture<Suggestions> get_trainer_id_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        RCTApi.getInstance().getTrainerRegistry().getIds().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int attach(CommandContext<CommandSourceStack> context) {
        try {
            var trainerId = context.getArgument(ARG_TRAINER_ID, String.class);
            var trainerEntity = (LivingEntity)EntityArgument.getEntity(context, ARG_TRAINER_ENTITY);
            RCTApi.getInstance().getTrainerRegistry().getById(trainerId, TrainerNPC.class).setEntity(trainerEntity);
            context.getSource().sendSystemMessage(Component.nullToEmpty(String.format("Trainer '%s' attached to '%s'", trainerId, trainerEntity.getDisplayName().getString())));
        } catch(Exception e) {
            return handleError(context, e);
        }

        return 0;
    }

    private static int battle(CommandContext<CommandSourceStack> context, BattleFormat format, Tag rulesTag) {
        try {
            var registry = RCTApi.getInstance().getTrainerRegistry();
            var actorsPerSide = format.getCobblemonBattleFormat().getBattleType().getActorsPerSide();
            List<List<Trainer>> participants = List.of(new ArrayList<>(), new ArrayList<>());

            for(int side = 0; side < 2; side++) {
                var list = participants.get(side);

                for(int actor = 0; actor < actorsPerSide; actor++) {
                    String trainerId;

                    try {
                        var trainerEntity = (LivingEntity)EntityArgument.getEntity(context, getParticipantEntityId(side, actor));
                        trainerId = RCTApi.getInstance().getTrainerRegistry().getId(trainerEntity);

                        if(trainerId == null) {
                            throw new Exception(String.format("'%s' has no trainer attached", trainerEntity.getName().getString()));
                        }
                    } catch(IllegalArgumentException e) {
                        // either no argument or wrong type -> try again
                        trainerId = context.getArgument(getParticipantId(side, actor), String.class);

                        try {                            
                            // command syntax for trainer id and entity selector is the same hence minecraft
                            // fails to treat uuids as entity selectors (but rather as trainer ids).
                            var entityUUID = UUID.fromString(trainerId);
                            var trainerEntity = (LivingEntity)context.getSource().getLevel().getEntity(entityUUID);
                            trainerId = RCTApi.getInstance().getTrainerRegistry().getId(trainerEntity);
    
                            if(trainerId == null) {
                                throw new Exception(String.format("'%s' has no trainer attached", trainerEntity.getName().getString()));
                            }
                        } catch(IllegalArgumentException _e) {
                            // argument is a trainer id
                        }
                    }

                    var trainer = registry.getById(trainerId);

                    if(trainer == null) {
                        throw new Exception(String.format("No such trainer registered '%s'", trainerId));
                    }

                    list.add(trainer);
                }
            }

            var rules = rulesTag != null ? GSON.fromJson(rulesTag.getAsString(), BattleRules.class) : new BattleRules();
            RCTApi.getInstance().getBattleManager().start(participants.get(0), participants.get(1), format, rules);
        } catch(Exception e) {
            return handleError(context, e);
        }

        return 0;
    }
}
