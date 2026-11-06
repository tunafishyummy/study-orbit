package logic;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.AppData;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BackupService {

    public void export(File file) throws IOException {
        JsonObject root = AppData.store().exportAll();
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(root);
        Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
    }

    public void restore(File file) throws IOException {
        String json;
        try {
            json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IOException("Could not read the backup file", exception);
        }
        JsonObject root;
        try {
            root = JsonParser.parseString(json).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new IOException("This file is not a Study Orbit backup", exception);
        }
        AppData.store().importAll(root);
    }
}
