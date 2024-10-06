package net.ctengine.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.ctengine.CTEngineMod;
import net.ctengine.api.CTEngine;
import net.ctengine.api.battle.BattleFormat;
import net.ctengine.api.battle.BattleParticipant;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class CTEngineCommands {
    public static final String CMD_BATTLE = "battle";
    public static final String ARG_BATTLE_FORMAT = "format";
    public static final String ARG_PARTICIPANT = "participant";
    public static final String ARG_VS = "vs";

    private CTEngineCommands() {}

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, access, environment) -> {
            var builder = CommandManager.literal(CMD_BATTLE);

            for(var format : BattleFormat.values()) {
                builder.then(builderFormat(format));
            }

            dispatcher.register(CommandManager.literal(CTEngineMod.MOD_ID)
                .requires(css -> css.hasPermissionLevel(2))
                .then(builder));
        });
    }

    private static ArgumentBuilder<ServerCommandSource, ?> builderFormat(BattleFormat format) {
        var battleType = format.getCobblemonBattleFormat().component2();

        return CommandManager.literal(format.name()).then(
            builderParticipants(format, 0, 0, battleType.getActorsPerSide()));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> builderParticipants(BattleFormat format, int side, int actor, int actorsPerSide) {
        if(actor < actorsPerSide) {
            if(actor + 1 < actorsPerSide) {
                return RequiredArgumentBuilder
                    .<ServerCommandSource, String>argument(getParticipantId(side, actor), StringArgumentType.string())
                    .suggests(CTEngineCommands::get_trainer_id_suggestions)
                    .then(builderParticipants(format, side, actor + 1, actorsPerSide));
            }

            if(side < 1) {
                return RequiredArgumentBuilder
                    .<ServerCommandSource, String>argument(getParticipantId(side, actor), StringArgumentType.string())
                    .suggests(CTEngineCommands::get_trainer_id_suggestions)
                    .then(CommandManager.literal(ARG_VS).then(builderParticipants(format, side + 1, 0, actorsPerSide)));
            }

            return RequiredArgumentBuilder
                .<ServerCommandSource, String>argument(getParticipantId(side, actor), StringArgumentType.string())
                .suggests(CTEngineCommands::get_trainer_id_suggestions)
                .executes(context -> CTEngineCommands.battle(context, format));
        }

        throw new IllegalArgumentException("invalid battle actor index: " + actor);
    }

    private static String getParticipantId(int side, int actor) {
        return String.format("%s_%d_%d", ARG_PARTICIPANT, side, actor);
    }

    private static CompletableFuture<Suggestions> get_trainer_id_suggestions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        CTEngine.getInstance().getTrainerRegistry().getTrainerIds().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int battle(CommandContext<ServerCommandSource> context, BattleFormat format) {
        var trainerRegistry = CTEngine.getInstance().getTrainerRegistry();
        var actorsPerSide = format.getCobblemonBattleFormat().component2().getActorsPerSide();
        List<List<BattleParticipant>> participants = List.of(new ArrayList<>(), new ArrayList<>());

        for(int side = 0; side < 2; side++) {
            var list = participants.get(side);

            try {
                for(int actor = 0; actor < actorsPerSide; actor++) {
                    var trainerId = context.getArgument(getParticipantId(side, actor), String.class);
                    list.add(trainerRegistry.getTrainer(trainerId));
                }
            } catch(IllegalArgumentException e) {
                // ignore (assume mixed battle -> validation in BattleManager)
            }
        }

        CTEngine.getInstance().getBattleManager().startBattle(participants.get(0), participants.get(1), format);
        return 0;
    }
}
