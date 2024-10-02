package net.ctengine.battle;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import kotlin.Unit;
import net.ctengine.CTEngine;
import net.ctengine.trainer.Trainer;
import net.ctengine.trainer.WinRegistry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

// Tracks who is in what battle, will add to WinRegistry if the player wins
// TODO - implement win/loss commands
public class TrainerBattleListener {
    private static final Map<PokemonBattle, Trainer> onBattleVictoryMapper = new HashMap<>();
    private static final Map<PokemonBattle, Trainer> onBattleLossMapper = new HashMap<>();

    public static void addOnBattleVictory(PokemonBattle battle, Trainer trainer){
        onBattleVictoryMapper.put(battle, trainer);
    }

    public static void addOnBattleLoss(PokemonBattle battle, Trainer trainer){
        onBattleLossMapper.put(battle, trainer);
    }

    public static void registerListeners(){
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, battleVictoryEvent -> {
            PokemonBattle battle = battleVictoryEvent.getBattle();

            // When a battle is won check if it is a player winning before running a win command
            if (onBattleVictoryMapper.containsKey(battle) && CTEngine.runServer != null) {
                Trainer trainer = onBattleVictoryMapper.get(battle);

                battleVictoryEvent.getWinners().forEach(battleActor -> battleActor.getPlayerUUIDs().forEach(uuid -> {
                    WinRegistry.addWin(trainer, uuid);
                    ServerPlayerEntity serverPlayer = CTEngine.runServer.getPlayerManager().getPlayer(uuid);
                    if (serverPlayer == null) return;
                    CTEngine.LOGGER.info(serverPlayer.getName().toString()+" won the battle");
                    // TODO - HERE
                    //String winCommand = trainer.getWinCommand();
                    //if (winCommand != null && !winCommand.isEmpty()) runCommand(winCommand, serverPlayer);
                }));
                onBattleVictoryMapper.remove(battle);
            }

            // When a battle is lost check if it is a player losing before running a loss command
            if (onBattleLossMapper.containsKey(battle) && CTEngine.runServer != null) {
                Trainer trainer = onBattleLossMapper.get(battle);

                battleVictoryEvent.getLosers().forEach(battleActor -> battleActor.getPlayerUUIDs().forEach(uuid -> {
                    ServerPlayerEntity serverPlayer = CTEngine.runServer.getPlayerManager().getPlayer(uuid);
                    if (serverPlayer == null) return;
                    CTEngine.LOGGER.info(serverPlayer.getName().toString()+" lost the battle");
                    // TODO - HERE
                    //String lossCommand = trainer.getWinCommand();
                    //if (lossCommand != null && !lossCommand.isEmpty()) runCommand(lossCommand, serverPlayer);
                }));
                onBattleLossMapper.remove(battle);
            }
            return Unit.INSTANCE;
        });
    }
}
