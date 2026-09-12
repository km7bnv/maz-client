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

    /**
     * Stable per-module layout handle for hot render paths. The binding is created once,
     * then position/opacity changes are pushed into it by the editor/config mutators so
     * render code can read plain fields instead of hashing module-name strings every frame.
     */
    public static final class Binding {
        private final String moduleName;
        private final Position defaultPosition;
        private Position position;
        private int opacity;

        private Binding(String moduleName, Position defaultPosition) {
            this.moduleName = moduleName;
            this.defaultPosition = defaultPosition;
            sync();
        }

        public Position position() {
            return position;
        }

        public int x() {
            return position.x();
        }

        public int y() {
            return position.y();
        }

        public int opacity() {
            return opacity;
        }

        private void sync() {
            position = POSITIONS.getOrDefault(moduleName, defaultPosition);
            opacity = OPACITY.getOrDefault(moduleName, 239);
        }

        private void setPosition(Position next) {
            position = next;
        }

        private void setOpacity(int next) {
            opacity = next;
        }
    }

    private static final Map<String, Position> POSITIONS = new HashMap<>();
    private static final Map<String, Position> DEFAULT_POSITIONS = new HashMap<>();
    private static final Map<String, Integer> OPACITY = new HashMap<>();
    private static final Map<String, Binding> BINDINGS = new HashMap<>();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("maz-client-hud.properties");

    private static boolean loaded;

    private HudLayout() {
    }

    public static Binding bind(String moduleName, int defaultX, int defaultY) {
        ensureLoaded();
        Binding existing = BINDINGS.get(moduleName);
        if (existing != null) return existing;

        Position defaultPosition = DEFAULT_POSITIONS.computeIfAbsent(
                moduleName,
                ignored -> new Position(defaultX, defaultY)
        );
        Binding created = new Binding(moduleName, defaultPosition);
        BINDINGS.put(moduleName, created);
        return created;
    }

    public static Position getPosition(String moduleName, int defaultX, int defaultY) {
        return bind(moduleName, defaultX, defaultY).position();
    }

    public static void setPosition(String moduleName, int x, int y) {
        ensureLoaded();
        Position next = new Position(x, y);
        POSITIONS.put(moduleName, next);
        Binding binding = BINDINGS.get(moduleName);
        if (binding != null) binding.setPosition(next);
    }

    public static int getOpacity(String moduleName) {
        ensureLoaded();
        Binding binding = BINDINGS.get(moduleName);
        if (binding != null) return binding.opacity();
        return OPACITY.getOrDefault(moduleName, 239);
    }

    public static void setOpacity(String moduleName, int alpha) {
        ensureLoaded();
        int next = Math.max(0, Math.min(255, alpha));
        OPACITY.put(moduleName, next);
        Binding binding = BINDINGS.get(moduleName);
        if (binding != null) binding.setOpacity(next);
    }

    public static void reset() {
        ensureLoaded();
        POSITIONS.clear();
        OPACITY.clear();
        syncBindings();
        save();
    }

    public static void save() {
        ensureLoaded();

        Properties properties = exportProperties();

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "MazClient HUD layout");
            }
        } catch (IOException exception) {
            System.err.println("MazClient: failed to save HUD layout: " + exception.getMessage());
        }
    }

    public static Properties exportProperties() {
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
        return properties;
    }

    public static void importProperties(Properties properties) {
        ensureLoaded();
        POSITIONS.clear();
        OPACITY.clear();
        applyProperties(properties);
        syncBindings();
        save();
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
            System.err.println("MazClient: failed to load HUD layout: " + exception.getMessage());
            return;
        }

        applyProperties(properties);
    }

    private static void applyProperties(Properties properties) {
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
                    OPACITY.put(moduleName, Math.max(0, Math.min(255, Integer.parseInt(properties.getProperty(key)))));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private static void syncBindings() {
        for (Binding binding : BINDINGS.values()) binding.sync();
    }
}
