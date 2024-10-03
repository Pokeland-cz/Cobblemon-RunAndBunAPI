package net.ctengine.battle;

import net.ctengine.CTEngine;
import net.ctengine.battle.command.CommandExecutionBuilder;
import net.ctengine.battle.command.CommandExecutor;
import net.ctengine.battle.command.PermissionLevel;
import net.minecraft.server.network.ServerPlayerEntity;

// "Borrowed" from selfdot's code
public class CommandHandler {
    protected static void runCommand(String command, ServerPlayerEntity player) {
        // Because Mohist doesn't allow executing commands as console, this option is needed.
        CommandExecutionBuilder executeCommand = CommandExecutionBuilder.execute(command).withPlayer(player);
        switch (CTEngine.COMMAND_EXECUTOR) {
            case CommandExecutor.PLAYER -> executeCommand.withLevel(PermissionLevel.ALL_COMMANDS).as(player);
            case CommandExecutor.CONSOLE -> executeCommand.as(player.getServer());
        }
    }
}
