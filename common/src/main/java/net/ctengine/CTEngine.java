package net.ctengine;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import kotlin.Unit;
import net.ctengine.battle.TrainerBattleListener;
import net.ctengine.battle.command.CommandExecutor;
import net.ctengine.registry.ModEntityRegistry;
import net.ctengine.trainer.*;
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
import java.util.List;

public final class CTEngine {
    public static final String MOD_ID = "ctengine";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Initialise as null so we can make sure it's been initialised properly later
    public static MinecraftServer runServer = null;

    public static CommandExecutor COMMAND_EXECUTOR = CommandExecutor.CONSOLE;

    public static void init() {
        // Write common init code here.
        LifecycleEvent.SERVER_BEFORE_START.register(server -> {
            server.execute(() -> {
                runServer = server;
                TrainerBattleListener.registerListeners();
                Gen5AI.initialiseTypeChart();

                // Ensure registries are empty
                TrainerRegistry.resetRegistry();
                WinRegistry.resetRegistry();

                TrainerInitialiser.initialiseTrainersFromJSON();
                TrainerInitialiser.initialiseTrainersFromDatapack();

                // Initialise WinRegistry
                Path winRegistryPath = Paths.get(CTEngine.runServer.getSavePath(WorldSavePath.PLAYERDATA).getParent().toString(),
                        "ctengine", "winRegistry.json");
                WinRegistry.initFromJSONContent(JSONHandler.readJSON(winRegistryPath));
            });
        });

        // Save the registries
        LifecycleEvent.SERVER_STOPPING.register(server -> {
            server.execute(() -> {
                TrainerRegistry.save();
                WinRegistry.save();
            });
        });

        // Cancel loot drop if the Pokemon is trainer owned
        CobblemonEvents.LOOT_DROPPED.subscribe(Priority.HIGHEST, event -> {
            if (!(event.getEntity() instanceof PokemonEntity pokemonEntity)) return Unit.INSTANCE;
            if (TrainerPokemon.isTrainerOwned.contains(pokemonEntity.getPokemon().getUuid())) event.cancel();
            return Unit.INSTANCE;
        });

        BattleTestCommand.register();
        ModEntityRegistry.initialize();

        EnvExecutor.runInEnv(Env.CLIENT, () -> CTEngine.Client::initializeClient);
    }

    // The following code is in Cobblemon Trainers but unsure why.
    // Surely a sent out trainer owned pokemon is always in battle and therefore
    // cannot be battled by a different player anyway.
    // I haven't made a call to register this listener as I'll ask selfdot the reason first.
    public static void registerPokemonSendOutListener() {
        CobblemonEvents.POKEMON_SENT_POST.subscribe(Priority.NORMAL, event -> {
            if (TrainerPokemon.isTrainerOwned.contains(event.getPokemon().getUuid())) {
                event.getPokemonEntity().getDataTracker().set(PokemonEntity.getUNBATTLEABLE(), true);
            }
            return Unit.INSTANCE;
        });
    }


    @Environment(EnvType.CLIENT)
    public static class Client {
        @Environment(EnvType.CLIENT)
        public static void initializeClient() {
            // temp
            EntityRendererRegistry.register(ModEntityRegistry.TRAINER_VILLAGER, VillagerEntityRenderer::new);
        }
    }
}
