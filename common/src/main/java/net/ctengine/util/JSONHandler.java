package net.ctengine.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.ctengine.CTEngine;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JSONHandler {

    public static void folderHandler(Path directory){
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                CTEngine.LOGGER.info(e.toString());
            }
        }
    }

    public static List<Path> getAllJsonFiles(Path directory) throws IOException {
        folderHandler(directory);
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());
        }
    }

    public static Map<String, Object> readJSON(Path filePath){
        Gson gson = new Gson();
        folderHandler(filePath.getParent());

        try (FileReader reader = new FileReader(filePath.toString())) {
            Type dataType = new TypeToken<Map<String, Object>>() {}.getType();

            return gson.fromJson(reader, dataType);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    public static void writeJSON(Map<String, Object> jsonContent, Path filePath){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        folderHandler(filePath.getParent());

        try (FileWriter writer = new FileWriter(filePath.toString())) {
            gson.toJson(jsonContent, writer);
        } catch (IOException e) {
            CTEngine.LOGGER.info(e.toString());
        }
    }
}
