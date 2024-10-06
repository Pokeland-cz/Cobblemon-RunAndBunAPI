package net.ctengine.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ctengine.CTEngineMod;
import net.ctengine.api.CTEngine;
import net.ctengine.example.models.TrainerModel;
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

    public static void init(MinecraftServer server) {        
        var trainerDir = Path.of(server.getSavePath(WorldSavePath.ROOT).toString(), "..", "..", "trainers").toFile();
        var files = trainerDir.listFiles(f -> f.getName().toLowerCase().endsWith(".json"));
        var trainerReg = CTEngine.getInstance().getTrainerRegistry();
        trainerReg.clear();

        CTEngineMod.LOG.info(String.format("loading trainers from '%s'", trainerDir.getAbsolutePath()));        

        if(files != null) {
            for(var trainerFile : files) {
                try(var rd = new BufferedReader(new FileReader(trainerFile))) {
                    var trainerId = fileToId(trainerFile);
                    trainerReg.register(trainerId, GSON.fromJson(rd, TrainerModel.class));
                } catch(IOException e) {
                    CTEngineMod.LOG.error("failed to parse trainer", e);
                }
            }
        }
    }
}
