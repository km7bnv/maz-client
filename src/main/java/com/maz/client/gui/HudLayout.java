package com.maz.client.gui;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class HudLayout {

    public record Position(int x, int y) {}

    private static final Map<String, Position> POSITIONS = new HashMap<>();
    private static final Map<String, Integer> OPACITY = new HashMap<>();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("maz-client-hud.properties");

    private static boolean loaded;

    private HudLayout() {
    }

    public static Position getPosition(String moduleName, int defaultX, int defaultY) {
        ensureLoaded();
        return POSITIONS.getOrDefault(moduleName, new Position(defaultX, defaultY));
    }

    public static void setPosition(String moduleName, int x, int y) {
        ensureLoaded();
        POSITIONS.put(moduleName, new Position(x, y));
    }

    public static int getOpacity(String moduleName) {
        ensureLoaded();
        return OPACITY.getOrDefault(moduleName, 239);
    }

    public static void setOpacity(String moduleName, int alpha) {
        ensureLoaded();
        OPACITY.put(moduleName, Math.max(20, Math.min(255, alpha)));
    }

    public static void reset() {
        ensureLoaded();
        POSITIONS.clear();
        OPACITY.clear();
        save();
    }

    public static void save() {
        ensureLoaded();

        Properties properties = new Properties();
        for (Map.Entry<String, Position> entry : POSITIONS.entrySet()) {
            Position position = entry.getValue();
            properties.setProperty(entry.getKey() + ".x", Integer.toString(position.x()));
            properties.setProperty(entry.getKey() + ".y", Integer.toString(position.y()));
        }
        for (Map.Entry<String, Integer> entry : OPACITY.entrySet()) {
            properties.setProperty(entry.getKey() + ".opacity", Integer.toString(entry.getValue()));
        }

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "Maz Client HUD layout");
            }
        } catch (IOException exception) {
            System.err.println("Maz Client: failed to save HUD layout: " + exception.getMessage());
        }
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        if (!Files.exists(CONFIG_PATH)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
        } catch (IOException exception) {
            System.err.println("Maz Client: failed to load HUD layout: " + exception.getMessage());
            return;
        }

        for (String key : properties.stringPropertyNames()) {
            if (key.endsWith(".x")) {
                String moduleName = key.substring(0, key.length() - 2);
                String xValue = properties.getProperty(moduleName + ".x");
                String yValue = properties.getProperty(moduleName + ".y");
                if (xValue != null && yValue != null) {
                    try {
                        POSITIONS.put(moduleName, new Position(
                                Integer.parseInt(xValue),
                                Integer.parseInt(yValue)
                        ));
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if (key.endsWith(".opacity")) {
                String moduleName = key.substring(0, key.length() - ".opacity".length());
                try {
                    setOpacity(moduleName, Integer.parseInt(properties.getProperty(key)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
