package net.ctengine.battle.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ctengine.CTEngine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

// "Borrowed" from selfdot's code
public class CommandExecutionBuilder {

    private static final String PLAYER_PLACEHOLDER = "%player%";

    public static CommandExecutionBuilder execute(String command) {
        return new CommandExecutionBuilder(command);
    }

    private String command;
    private PermissionLevel level = null;

    private CommandExecutionBuilder(String command) {
        this.command = command;
    }

    public CommandExecutionBuilder withPlayer(PlayerEntity player) {
        this.command = command.replaceAll(PLAYER_PLACEHOLDER, player.getGameProfile().getName());
        return this;
    }

    public CommandExecutionBuilder withLevel(PermissionLevel level) {
        this.level = level;
        return this;
    }

    private void executeAs(ServerCommandSource source) {
        try {
            if (level != null) source = source.withLevel(level.ordinal());
            source.getServer().getCommandManager().getDispatcher().execute(command, source);

        } catch (CommandSyntaxException e) {
            CTEngine.LOGGER.info("Could not run: " + command);
            CTEngine.LOGGER.info(e.getMessage());
        }
    }

    public void as(Entity entity) {
        executeAs(entity.getCommandSource());
    }

    public void as(@Nullable MinecraftServer server) {
        if (server == null) throw new IllegalStateException("Server reference is null");
        executeAs(server.getCommandSource());
    }

}