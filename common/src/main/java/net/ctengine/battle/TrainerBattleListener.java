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

public class TrainerBattleListener {
    private static Map<PokemonBattle, Trainer> onBattleVictoryMapper = new HashMap<>();
    private static Map<PokemonBattle, Trainer> onBattleLossMapper = new HashMap<>();

    public static void addOnBattleVictory(PokemonBattle battle, Trainer trainer){
        onBattleVictoryMapper.put(battle, trainer);
    }

    public static void addOnBattleLoss(PokemonBattle battle, Trainer trainer){
        onBattleLossMapper.put(battle, trainer);
    }

    public static void registerListeners(){
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, battleVictoryEvent -> {
            PokemonBattle battle = battleVictoryEvent.getBattle();
            if (onBattleVictoryMapper.containsKey(battle) && CTEngine.runServer != null) {
                Trainer trainer = onBattleVictoryMapper.get(battle);

                battleVictoryEvent.getWinners().forEach(battleActor -> battleActor.getPlayerUUIDs().forEach(uuid -> {
                    WinRegistry.addWin(trainer, uuid);
                    ServerPlayerEntity serverPlayer = CTEngine.runServer.getPlayerManager().getPlayer(uuid);
                    if (serverPlayer == null) return;
                    CTEngine.LOGGER.info(serverPlayer.getName().toString()+" won the battle");
                    //String winCommand = trainer.getWinCommand();
                    //if (winCommand != null && !winCommand.isEmpty()) runCommand(winCommand, serverPlayer);
                }));
                onBattleVictoryMapper.remove(battle);
            }
            if (onBattleLossMapper.containsKey(battle) && CTEngine.runServer != null) {
                Trainer trainer = onBattleLossMapper.get(battle);

                battleVictoryEvent.getLosers().forEach(battleActor -> battleActor.getPlayerUUIDs().forEach(uuid -> {
                    ServerPlayerEntity serverPlayer = CTEngine.runServer.getPlayerManager().getPlayer(uuid);
                    if (serverPlayer == null) return;
                    CTEngine.LOGGER.info(serverPlayer.getName().toString()+" lost the battle");
                    //String lossCommand = trainer.getWinCommand();
                    //if (lossCommand != null && !lossCommand.isEmpty()) runCommand(lossCommand, serverPlayer);
                }));
                onBattleLossMapper.remove(battle);
            }
            return Unit.INSTANCE;
        });
    }
}
