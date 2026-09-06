package com.maz.client.config;

import com.maz.client.module.Module;
import com.maz.client.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ClientConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("maz-client.properties");

    private static boolean loading;

    private ClientConfig() {
    }

    public static void load(ModuleManager manager) {
        if (!Files.exists(CONFIG_PATH)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
        } catch (IOException exception) {
            System.err.println("MazClient: failed to load config: " + exception.getMessage());
            return;
        }

        loading = true;
        try {
            for (Module module : manager.getModules()) {
                String value = properties.getProperty("module." + module.getName() + ".enabled");
                if (value != null) {
                    module.setEnabled(Boolean.parseBoolean(value));
                }
            }
        } finally {
            loading = false;
        }
    }

    public static void save(ModuleManager manager) {
        if (loading) {
            return;
        }

        Properties properties = new Properties();
        for (Module module : manager.getModules()) {
            properties.setProperty(
                    "module." + module.getName() + ".enabled",
                    Boolean.toString(module.isEnabled())
            );
        }

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "MazClient settings");
            }
        } catch (IOException exception) {
            System.err.println("MazClient: failed to save config: " + exception.getMessage());
        }
    }
}
