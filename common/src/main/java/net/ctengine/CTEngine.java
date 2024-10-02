package net.ctengine;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.ctengine.battle.TrainerBattleListener;
import net.ctengine.registry.ModEntityRegistry;
import net.ctengine.trainer.Trainer;
import net.ctengine.trainer.TrainerInitialiser;
import net.ctengine.trainer.TrainerRegistry;
import net.ctengine.trainer.WinRegistry;
import net.ctengine.trainer.ai.Gen5AI;
import net.ctengine.util.JSONHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ctengine.command.BattleTestCommand;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class CTEngine {
    public static final String MOD_ID = "ctengine";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static MinecraftServer runServer = null;

    public static void init() {
        // Write common init code here.
        LifecycleEvent.SERVER_BEFORE_START.register(server -> {
            server.execute(() -> {
                runServer = server;
                TrainerBattleListener.registerListeners();
                Gen5AI.initialiseTypeChart();
                TrainerInitialiser.initialiseTrainersFromJSON();
                TrainerInitialiser.initialiseTrainersFromDatapack();

                // Initialise WinRegistry
                Path winRegistryPath = Paths.get(CTEngine.runServer.getSavePath(WorldSavePath.PLAYERDATA).getParent().toString(),
                        "ctengine", "winRegistry.json");
                WinRegistry.initFromJSONContent(JSONHandler.readJSON(winRegistryPath));
            });
        });

        LifecycleEvent.SERVER_STOPPING.register(server -> {
            server.execute(() -> {
                for (Trainer trainer : TrainerRegistry.getTrainers()){
                    trainer.save();
                    WinRegistry.save();
                }
            });
        });

        BattleTestCommand.register();
        ModEntityRegistry.initialize();

        EnvExecutor.runInEnv(Env.CLIENT, () -> CTEngine.Client::initializeClient);
    }


    @Environment(EnvType.CLIENT)
    public static class Client {
        @Environment(EnvType.CLIENT)
        public static void initializeClient() {
            EntityRendererRegistry.register(ModEntityRegistry.TRAINER_VILLAGER, VillagerEntityRenderer::new);
        }
    }
}
