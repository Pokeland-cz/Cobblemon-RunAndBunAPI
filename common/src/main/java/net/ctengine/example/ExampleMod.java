package net.ctengine.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.ctengine.ModCommon;
import net.ctengine.api.RCTApi;
import net.ctengine.api.errors.RCTException;
import net.ctengine.api.models.TrainerModel;
import net.ctengine.commands.RCTApiCommands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

public class ExampleMod {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private static String fileToId(File file) {
        var name = file.getName().toLowerCase().trim();
        var i = name.lastIndexOf('.');
        return i < 0 ? name : name.substring(0, i);
    }

    // Call this in the common setup phase of the mod. E.g. in onInitialze() of your
    // ModInitializer on Fabric or in the constructor of your @Mod annotated class on
    // Neoforge.
    public static void init() {
        // We may initialize the RCTApi singleton with custom implementations of
        // TrainerRegistry and BattleManager. If not explicitly initialized (i.e. with
        // RCTApi#init(TrainerRegistry, BattleManager)) a RCTApi instance will be lazily
        // instantiated on first retrieval with RCTApi#getInstance() using a default
        // constructed TrainerRegistry and BattleManager.

        RCTApiCommands.register(); // commands are not registered unless explicitly doing so.
        ExampleMod.registerEvents();
    }

    static void registerEvents() {
        // A server instance is required to initialize a TrainerRegistry hence this is the
        // earliest possible point to register trainers (see below).
        LifecycleEvent.SERVER_STARTING.register(ExampleMod::onServerStarting);

        // We can easily (un)register players as trainers whenever they log in or out.
        PlayerEvent.PLAYER_JOIN.register(ExampleMod::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(ExampleMod::onPlayerQuit);
    }

    static void onServerStarting(MinecraftServer server) {
        // Initialize (and clear) the trainer registry for the server
        var trainerRegistry = RCTApi.getInstance().getTrainerRegistry();
        trainerRegistry.init(server); // this is required

        // We look for trainer json files in 'minecraft/trainers'
        var trainerDir = Path.of(server.getWorldPath(LevelResource.ROOT).toString(), "..", "..", "trainers").toFile();
        var files = trainerDir.listFiles(f -> f.getName().toLowerCase().endsWith(".json"));

        if(files != null) {
            for(var trainerFile : files) {
                try(var rd = new BufferedReader(new FileReader(trainerFile))) {
                    // We use the file name as trainer id and parse the content into a TrainerModel
                    // instance, which is then provided to the TrainerRegistry to register a new
                    // TrainerNPC.
                    var trainerId = fileToId(trainerFile);
                    trainerRegistry.registerNPC(trainerId, GSON.fromJson(rd, TrainerModel.class));
                } catch(RCTException errors) {
                    // This will log all issues that the model may has (the trainer was registered regardless)
                    ModCommon.LOG.error("Model validation failure in: " + trainerFile.getPath());
                    errors.getErrors().forEach(error -> ModCommon.LOG.error(error.message));
                } catch(IOException e) {
                    // The trainer was not registered
                    ModCommon.LOG.error("Failed to parse trainer", e);
                }
            }
        }
    }

    // Note: The TrainerRegistry does not allow to implicitly overwrite an existing
    // trainer id. Using a players name may be sufficient for this example but in real
    // scenarios a custom resolution of duplicate ids would be necessary. It is of
    // course possible to use any other string as id to circumvent this issue (e.g. a
    // players uuid).
    static void onPlayerJoin(ServerPlayer player) {
        RCTApi.getInstance().getTrainerRegistry().registerPlayer(player.getName().getString(), player);
    }

    static void onPlayerQuit(Player player) {
        RCTApi.getInstance().getTrainerRegistry().unregisterById(player.getName().getString());
    }
}
