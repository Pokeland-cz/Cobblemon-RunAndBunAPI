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
import net.ctengine.api.CTEngine;
import net.ctengine.api.errors.CTException;
import net.ctengine.api.models.TrainerModel;
import net.ctengine.api.trainer.TrainerNPC;
import net.ctengine.api.trainer.TrainerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

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
        // We look for trainer json files in 'minecraft/trainers'
        var trainerDir = Path.of(server.getSavePath(WorldSavePath.ROOT).toString(), "..", "..", "trainers").toFile();
        var files = trainerDir.listFiles(f -> f.getName().toLowerCase().endsWith(".json"));

        // Revert to initial state (safety measure)
        var trainerReg = CTEngine.getInstance().getTrainerRegistry();
        trainerReg.clear();

        if(files != null) {
            for(var trainerFile : files) {
                try(var rd = new BufferedReader(new FileReader(trainerFile))) {
                    // We use the file name as trainer id and parse the content into a TrainerModel
                    // instance, which is then passed to a TrainerNPC that is registered to the
                    // TrainerRegistry (along a server to initialize the default villager entity).
                    var trainerId = fileToId(trainerFile);
                    trainerReg.register(trainerId, new TrainerNPC(server, GSON.fromJson(rd, TrainerModel.class)));
                } catch(CTException errors) {
                    CTEngineMod.LOG.error("model validation failure in: " + trainerFile.getPath());
                    errors.getErrors().forEach(error -> CTEngineMod.LOG.error(error.message)); // this will log all issues that the model may has
                } catch(IOException e) {
                    CTEngineMod.LOG.error("failed to parse trainer", e);
                }
            }
        }

        // We can easily (un)register players as trainers whenever they log in or out.
        // Note: The TrainerRegistry does not allow to implicitly overwrite an existing
        // trainer id. Using a players uuid should be sufficent in most cases.
        if(!eventsRegistered) {
            PlayerEvent.PLAYER_JOIN.register(player -> trainerReg.register(player.getUuidAsString(), new TrainerPlayer(player)));
            PlayerEvent.PLAYER_QUIT.register(player -> trainerReg.unregister(player.getUuidAsString()));
            eventsRegistered = true;
        }
    }
}
