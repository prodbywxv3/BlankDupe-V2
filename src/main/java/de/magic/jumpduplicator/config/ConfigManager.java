package de.magic.jumpduplicator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.magic.jumpduplicator.JumpDuplicatorMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("jumpduplicator.json");
    private JumpDuplicatorConfig config = new JumpDuplicatorConfig();

    public synchronized JumpDuplicatorConfig get() {
        return config;
    }

    public synchronized void load() {
        JumpDuplicatorConfig loaded = null;
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                loaded = GSON.fromJson(reader, JumpDuplicatorConfig.class);
            } catch (Exception exception) {
                JumpDuplicatorMod.LOGGER.warn("Failed to read {}, using defaults.", configPath, exception);
            }
        }

        config = loaded == null ? new JumpDuplicatorConfig() : loaded;
        config.validate();
        save();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            JumpDuplicatorMod.LOGGER.error("Failed to save {}", configPath, exception);
        }
    }
}
