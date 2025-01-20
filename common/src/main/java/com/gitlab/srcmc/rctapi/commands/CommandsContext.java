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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.battle.BattleFormat;
import com.gitlab.srcmc.rctapi.api.battle.BattleRules;
import com.gitlab.srcmc.rctapi.api.battle.BattleState;
import com.gitlab.srcmc.rctapi.api.events.EventListener;
import com.gitlab.srcmc.rctapi.api.events.Events;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.commands.arguments.BattleEndCommandMapArgument;
import com.gitlab.srcmc.rctapi.commands.arguments.BattleRulesArgument;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public abstract class CommandsContext {
    private static final String CMD_BATTLE = "battle";
    private static final String ARG_PARTICIPANT = "participant";
    private static final String ARG_VS = "vs";
    private static final String ARG_RULES = "rules";
    private static final String ARG_WIN_COMMANDS = "onwin";
    
    private static final String CMD_ATTACH = "attach";
    private static final String ARG_TRAINER_ID = "trainerId";
    private static final String ARG_TRAINER_ENTITY = "trainerEntity";

    public abstract String getPrefix();
    public abstract int getWinCommandsPermission();

    void onCommandRegistration(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, CommandSelection env) {
        var builder = Commands.literal(CMD_BATTLE);

        for(var format : BattleFormat.values()) {
            builder.then(builderFormat(format));
        }

        dispatcher.register(Commands.literal(this.getPrefix())
            .requires(css -> css.hasPermission(2))
            .then(Commands.literal(CMD_ATTACH)
                .then(Commands
                    .argument(ARG_TRAINER_ID, ResourceLocationArgument.id())
                    .suggests(this::get_trainer_id_suggestions)
                    .then(Commands.argument(ARG_TRAINER_ENTITY, EntityArgument.entity())
                        .executes(this::attach))))
            .then(builder));
    }

    private ArgumentBuilder<CommandSourceStack, ?> builderFormat(BattleFormat format) {
        var battleType = format.getCobblemonBattleFormat().getBattleType();
        var actorsPerSide = battleType.getActorsPerSide();
        var builder = Commands.literal(format.name());
        var p = (long)Math.pow(2, 2*actorsPerSide);

        for(long i = 0; i < p; i++) {
            builder = builder.then(builderParticipants(format, 0, 0, actorsPerSide, i, 0));
        }

        return builder;
    }

    private ArgumentBuilder<CommandSourceStack, ?> builderParticipants(BattleFormat format, int side, int actor, int actorsPerSide, long entityArg, long optionalArgs) {
        if(actor < actorsPerSide) {
            var arg = ((1L<<(side*actorsPerSide + actor)) & entityArg) != 0
                ? Commands.argument(getParticipantEntityId(side, actor), EntityArgument.entity())
                : RequiredArgumentBuilder.<CommandSourceStack, ResourceLocation>argument(getParticipantId(side, actor), ResourceLocationArgument.id()).suggests(this::get_trainer_id_suggestions);

            return actor + 1 < actorsPerSide
                ? arg.then(builderParticipants(format, side, actor + 1, actorsPerSide, entityArg, optionalArgs))
                : side < 1
                    ? arg.then(Commands.literal(ARG_VS).then(builderParticipants(format, side + 1, 0, actorsPerSide, entityArg, optionalArgs)))
                    : builderOptionalArgs(arg, format, optionalArgs);
        }

        throw new IllegalArgumentException("invalid battle actor index: " + actor);
    }

    private ArgumentBuilder<CommandSourceStack, ?> builderOptionalArgs(RequiredArgumentBuilder<CommandSourceStack, ?> arg, BattleFormat format, long optionalArgs) {
        arg = arg.executes(ctx -> this.battle(ctx, format, null, null));

        arg = arg.then(Commands.literal(ARG_RULES)
            .then(Commands.argument(ARG_RULES, BattleRulesArgument.battleRules())
                .executes(ctx -> this.battle(ctx, format, ctx.getArgument(ARG_RULES, BattleRules.class), null))));
        arg = arg.then(Commands.literal(ARG_WIN_COMMANDS)
            .then(Commands.argument(ARG_WIN_COMMANDS, BattleEndCommandMapArgument.map())
                .executes(ctx -> this.battle(ctx, format, null, ctx.getArgument(ARG_WIN_COMMANDS, BattleEndCommand.Map.class)))));

        arg = arg.then(Commands.literal(ARG_RULES)
            .then(Commands.argument(ARG_RULES, BattleRulesArgument.battleRules())
                .then(Commands.literal(ARG_WIN_COMMANDS)
                    .then(Commands.argument(ARG_WIN_COMMANDS, BattleEndCommandMapArgument.map())
                        .executes(ctx -> this.battle(ctx, format, ctx.getArgument(ARG_RULES, BattleRules.class), ctx.getArgument(ARG_WIN_COMMANDS, BattleEndCommand.Map.class)))))));
        arg = arg.then(Commands.literal(ARG_WIN_COMMANDS)
            .then(Commands.argument(ARG_WIN_COMMANDS, BattleEndCommandMapArgument.map())
                .then(Commands.literal(ARG_RULES)
                    .then(Commands.argument(ARG_RULES, BattleRulesArgument.battleRules())
                        .executes(ctx -> this.battle(ctx, format, ctx.getArgument(ARG_RULES, BattleRules.class), ctx.getArgument(ARG_WIN_COMMANDS, BattleEndCommand.Map.class)))))));

        return arg;
    }

    private int attach(CommandContext<CommandSourceStack> context) {
        try {
            var trainerId = ResourceLocationArgument.getId(context, ARG_TRAINER_ID).toString();
            var trainerEntity = (LivingEntity)EntityArgument.getEntity(context, ARG_TRAINER_ENTITY);
            RCTApi.getInstance(this.getPrefix()).getTrainerRegistry().getById(trainerId, TrainerNPC.class).setEntity(trainerEntity);
            context.getSource().sendSystemMessage(Component.nullToEmpty(String.format("Trainer '%s' attached to '%s'", trainerId, trainerEntity.getDisplayName().getString())));
        } catch(Exception e) {
            return handleError(context, e);
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    private int battle(CommandContext<CommandSourceStack> context, BattleFormat format, BattleRules rules, BattleEndCommand.Map commands) {
        try {
            var rct = RCTApi.getInstance(this.getPrefix());
            var registry = rct.getTrainerRegistry();
            var actorsPerSide = format.getCobblemonBattleFormat().getBattleType().getActorsPerSide();
            List<List<Trainer>> participants = List.of(new ArrayList<>(), new ArrayList<>());

            for(int side = 0; side < 2; side++) {
                var list = participants.get(side);

                for(int actor = 0; actor < actorsPerSide; actor++) {
                    String trainerId;

                    try {
                        var trainerEntity = (LivingEntity)EntityArgument.getEntity(context, getParticipantEntityId(side, actor));
                        trainerId = registry.getId(trainerEntity);

                        if(trainerId == null) {
                            throw new Exception(String.format("'%s' has no trainer attached", trainerEntity.getName().getString()));
                        }
                    } catch(IllegalArgumentException e) {
                        // either no argument or wrong type -> try again
                        trainerId = ResourceLocationArgument.getId(context, getParticipantId(side, actor)).toString();

                        try {                            
                            // command syntax for trainer id and entity selector is the same hence minecraft
                            // fails to treat uuids as entity selectors (but rather as trainer ids).
                            var entityUUID = UUID.fromString(trainerId);
                            var trainerEntity = (LivingEntity)context.getSource().getLevel().getEntity(entityUUID);
                            trainerId = registry.getId(trainerEntity);
    
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

            if(rules == null) {
                rules = new BattleRules();
            }

            EventListener<?>[] onEnd = new EventListener[1];

            if(commands != null) {
                commands.values().stream().flatMap(Stream::of).forEach(c -> {
                    c.setPermissionSupplier(this::getWinCommandsPermission);
                    c.setTitleSupplier(this::getPrefix);
                });

                onEnd[0] = e -> {
                    var server = context.getSource().getServer();

                    if(server != null) {
                        var state = (BattleState)e.getValue();
                        var winnersFirst = Stream
                            .concat(state.getWinners().stream(), state.getLosers().stream())
                            .map(Trainer::getEntity).toArray(l -> new LivingEntity[l]);

                        Stream
                            .of(commands.getOrDefault(state.getWinnerSide(), new BattleEndCommand[0]))
                            .forEach(c -> c.execute(server, winnersFirst));
                    }

                    rct.getEventContext().unregister(Events.BATTLE_ENDED, (EventListener<BattleState>)onEnd[0]);
                };

                rct.getEventContext().register(Events.BATTLE_ENDED, (EventListener<BattleState>)onEnd[0]);
            }

            RCTApi.getInstance(this.getPrefix()).getBattleManager().start(participants.get(0), participants.get(1), format, rules);
        } catch(Exception e) {
            return handleError(context, e);
        }

        return 0;
    }

    private CompletableFuture<Suggestions> get_trainer_id_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        RCTApi.getInstance(this.getPrefix()).getTrainerRegistry().getIds().forEach(builder::suggest);
        return builder.buildFuture();
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
}
