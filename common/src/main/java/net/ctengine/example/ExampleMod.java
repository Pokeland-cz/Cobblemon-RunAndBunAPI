package net.ctengine.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.architectury.event.events.common.PlayerEvent;
import net.ctengine.CTEngineMod;
import net.ctengine.api.errors.CTException;
import net.ctengine.api.models.TrainerModel;
import net.ctengine.api.trainer.TrainerPlayer;
import net.ctengine.api.util.Trainers;
import net.minecraft.server.MinecraftServer;
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

    // safety measure
    private static boolean eventsRegistered;

    public static void init(MinecraftServer server) {
        // Initialize (and clear) trainer registry for the server
        Trainers.init(server);

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
                    Trainers.registerNPC(trainerId, GSON.fromJson(rd, TrainerModel.class));
                } catch(CTException errors) {
                    // this will log all issues that the model may has (the trainer will still be registered)
                    CTEngineMod.LOG.error("model validation failure in: " + trainerFile.getPath());
                    errors.getErrors().forEach(error -> CTEngineMod.LOG.error(error.message));
                } catch(IOException e) {
                    CTEngineMod.LOG.error("failed to parse trainer", e);
                }
            }
        }

        // We can easily (un)register players as trainers whenever they log in or out.
        // Note: The TrainerRegistry does not allow to implicitly overwrite an existing
        // trainer id. Using a players display name may be sufficient for this example but
        // in real scenarios a custom resolution of duplicate names would be necessary. It
        // is of course possible to use any other string as id to circumvent this issue
        // (e.g. a players uuid).
        if(!ExampleMod.eventsRegistered) {
            PlayerEvent.PLAYER_JOIN.register(player -> Trainers.registerPlayer(player.getDisplayName().getString(), new TrainerPlayer(player)));
            PlayerEvent.PLAYER_QUIT.register(player -> Trainers.unregisterById(player.getDisplayName().getString()));
            ExampleMod.eventsRegistered = true;
        }
    }
}
