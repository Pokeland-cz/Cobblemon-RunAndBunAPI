package net.ctengine.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.ctengine.CTEngineMod;
import net.ctengine.api.battle.BattleFormat;
import net.ctengine.api.battle.BattleRules;
import net.ctengine.api.trainer.Trainer;
import net.ctengine.api.trainer.TrainerNPC;
import net.ctengine.api.util.Battles;
import net.ctengine.api.util.Trainers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public final class CTEngineCommands {
    private static Gson GSON = new Gson();

    public static final String CMD_BATTLE = "battle";
    public static final String ARG_BATTLE_FORMAT = "format";
    public static final String ARG_PARTICIPANT = "participant";
    public static final String ARG_VS = "vs";
    public static final String ARG_RULES = "rules";
    
    public static final String CMD_ATTACH = "attach";
    public static final String ARG_TRAINER_ID = "trainerId";
    public static final String ARG_TRAINER_ENTITY = "trainerEntity";

    private CTEngineCommands() {}

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, access, environment) -> {
            var builder = Commands.literal(CMD_BATTLE);

            for(var format : BattleFormat.values()) {
                builder.then(builderFormat(format));
            }

            dispatcher.register(Commands.literal(CTEngineMod.MOD_ID)
                .requires(css -> css.hasPermission(2))
                .then(Commands.literal(CMD_ATTACH)
                    .then(Commands
                        .argument(ARG_TRAINER_ID, StringArgumentType.string())
                        .suggests(CTEngineCommands::get_trainer_id_suggestions)
                        .then(Commands.argument(ARG_TRAINER_ENTITY, EntityArgument.entity())
                            .executes(CTEngineCommands::attach))))
                .then(builder));
        });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> builderFormat(BattleFormat format) {
        var battleType = format.getCobblemonBattleFormat().component2();

        return Commands.literal(format.name())
            .then(builderParticipants(format, 0, 0, battleType.getActorsPerSide(), false))
            .then(builderParticipants(format, 0, 0, battleType.getActorsPerSide(), true));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> builderParticipants(BattleFormat format, int side, int actor, int actorsPerSide, boolean withRules) {
        if(actor < actorsPerSide) {
            var arg = RequiredArgumentBuilder
                .<CommandSourceStack, String>argument(getParticipantId(side, actor), StringArgumentType.string())
                .suggests(CTEngineCommands::get_trainer_id_suggestions);

            return actor + 1 < actorsPerSide
                ? arg.then(builderParticipants(format, side, actor + 1, actorsPerSide, withRules)) : side < 1
                ? arg.then(Commands.literal(ARG_VS).then(builderParticipants(format, side + 1, 0, actorsPerSide, withRules)))
                : withRules
                    ? arg.then(Commands.argument(ARG_RULES, NbtTagArgument.nbtTag()).executes(context -> CTEngineCommands.battle(context, format, context.getArgument(ARG_RULES, Tag.class))))
                    : arg.executes(context -> CTEngineCommands.battle(context, format, null));
        }

        throw new IllegalArgumentException("invalid battle actor index: " + actor);
    }

    private static String getParticipantId(int side, int actor) {
        return String.format("%s_%d_%d", ARG_PARTICIPANT, side, actor);
    }

    private static int handleError(CommandContext<CommandSourceStack> context, Exception e) {
        CTEngineMod.LOG.error(e.getMessage(), e);
        context.getSource().sendFailure(Component.nullToEmpty(e.getMessage()));
        return 1;
    }

    private static CompletableFuture<Suggestions> get_trainer_id_suggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        Trainers.getIds().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int attach(CommandContext<CommandSourceStack> context) {
        try {
            var trainerId = context.getArgument(ARG_TRAINER_ID, String.class);
            var trainerEntity = (LivingEntity)EntityArgument.getEntity(context, ARG_TRAINER_ENTITY);
            Trainers.getById(trainerId, TrainerNPC.class).setEntity(trainerEntity);
            context.getSource().sendSystemMessage(Component.nullToEmpty(String.format("Trainer '%s' attached to '%s'", trainerId, trainerEntity.getDisplayName().getString())));
        } catch(Exception e) {
            return handleError(context, e);
        }

        return 0;
    }

    private static int battle(CommandContext<CommandSourceStack> context, BattleFormat format, Tag rulesTag) {
        try {
            var actorsPerSide = format.getCobblemonBattleFormat().component2().getActorsPerSide();
            List<List<Trainer>> participants = List.of(new ArrayList<>(), new ArrayList<>());

            for(int side = 0; side < 2; side++) {
                var list = participants.get(side);

                try {
                    for(int actor = 0; actor < actorsPerSide; actor++) {
                        var trainerId = context.getArgument(getParticipantId(side, actor), String.class);
                        list.add(Trainers.getById(trainerId));
                    }
                } catch(IllegalArgumentException e) {
                    // ignore (assume mixed battle -> validation in BattleManager)
                }
            }

            var rules = rulesTag != null ? GSON.fromJson(rulesTag.getAsString(), BattleRules.class) : new BattleRules();
            Battles.start(participants.get(0), participants.get(1), format, rules);
        } catch(Exception e) {
            return handleError(context, e);
        }

        return 0;
    }
}
