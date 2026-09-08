package com.maz.client.config;

import com.maz.client.MazClient;
import com.maz.client.gui.HudLayout;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ConfigBundle {
    private static final String OPTIONS_ENTRY = "minecraft/options.txt";
    private static final String CLIENT_ENTRY = "mazclient/maz-client.properties";
    private static final String HUD_ENTRY = "mazclient/maz-client-hud.properties";
    private static final String META_ENTRY = "bundle.properties";
    private static final long MAX_ENTRY_BYTES = 4L * 1024L * 1024L;
    private static final long MAX_BUNDLE_BYTES = 12L * 1024L * 1024L;
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private ConfigBundle() {
    }

    public static Path chooseAndExport() throws IOException {
        String suggested = defaultExportPath().toString();
        String selected = TinyFileDialogs.tinyfd_saveFileDialog(
                "Export MazClient config",
                suggested,
                null,
                "MazClient config bundle (*.mazconfig)"
        );
        if (selected == null || selected.isBlank()) return null;
        Path target = Path.of(selected);
        if (!target.getFileName().toString().toLowerCase().endsWith(".mazconfig")) {
            target = target.resolveSibling(target.getFileName() + ".mazconfig");
        }
        exportTo(target);
        return target;
    }

    public static Path chooseAndImport() throws IOException {
        String selected = TinyFileDialogs.tinyfd_openFileDialog(
                "Import MazClient config",
                defaultExportDirectory().toString(),
                null,
                "MazClient config bundle (*.mazconfig)",
                false
        );
        if (selected == null || selected.isBlank()) return null;
        Path source = Path.of(selected);
        if (!source.getFileName().toString().toLowerCase().endsWith(".mazconfig")) {
            throw new IOException("Choose a .mazconfig file.");
        }
        importFrom(source);
        return source;
    }

    public static Path profilePath(int slot) {
        validateSlot(slot);
        return FabricLoader.getInstance().getConfigDir()
                .resolve("mazclient")
                .resolve("profiles")
                .resolve("profile-" + slot + ".mazconfig");
    }

    public static boolean profileExists(int slot) {
        return Files.isRegularFile(profilePath(slot));
    }

    public static void saveProfile(int slot) throws IOException {
        exportTo(profilePath(slot));
    }

    public static void loadProfile(int slot) throws IOException {
        importFrom(profilePath(slot));
    }

    public static boolean deleteProfile(int slot) throws IOException {
        return Files.deleteIfExists(profilePath(slot));
    }

    public static void exportTo(Path target) throws IOException {
        if (target.getParent() != null) Files.createDirectories(target.getParent());

        Properties meta = new Properties();
        meta.setProperty("format", "1");
        meta.setProperty("mazClientVersion", MazClient.getVersion());
        meta.setProperty("createdAt", LocalDateTime.now().toString());

        Properties client = ClientConfig.exportProperties(MazClient.MODULE_MANAGER);
        Properties hud = HudLayout.exportProperties();
        Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(target))) {
            putProperties(zip, META_ENTRY, meta, "MazClient portable config bundle");
            putProperties(zip, CLIENT_ENTRY, client, "MazClient module settings");
            putProperties(zip, HUD_ENTRY, hud, "MazClient HUD layout");
            if (Files.exists(options)) {
                zip.putNextEntry(new ZipEntry(OPTIONS_ENTRY));
                Files.copy(options, zip);
                zip.closeEntry();
            }
        }
    }

    public static void importFrom(Path source) throws IOException {
        if (!Files.isRegularFile(source)) throw new IOException("Config bundle not found: " + source);
        long bundleSize = Files.size(source);
        if (bundleSize > MAX_BUNDLE_BYTES) {
            throw new IOException("Config bundle is too large (maximum 12 MiB).");
        }

        Properties client = null;
        Properties hud = null;
        byte[] optionsBytes = null;
        boolean validBundle = false;
        Set<String> seenEntries = new HashSet<>();

        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(source))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!isKnownEntry(name)) {
                    zip.closeEntry();
                    continue;
                }
                if (!seenEntries.add(name)) {
                    throw new IOException("Config bundle contains duplicate entry: " + name);
                }

                byte[] entryBytes = readEntryLimited(zip, MAX_ENTRY_BYTES);
                switch (name) {
                    case META_ENTRY -> {
                        Properties meta = loadProperties(entryBytes);
                        validBundle = "1".equals(meta.getProperty("format"));
                    }
                    case CLIENT_ENTRY -> client = loadProperties(entryBytes);
                    case HUD_ENTRY -> hud = loadProperties(entryBytes);
                    case OPTIONS_ENTRY -> optionsBytes = entryBytes;
                    default -> { }
                }
                zip.closeEntry();
            }
        }

        if (!validBundle) throw new IOException("That file is not a supported MazClient config bundle.");
        if (client == null) throw new IOException("The bundle is missing MazClient module settings.");
        if (hud == null) throw new IOException("The bundle is missing HUD layout settings.");

        if (optionsBytes != null) {
            writeOptionsAtomically(optionsBytes);
        }

        ClientConfig.importProperties(MazClient.MODULE_MANAGER, client);
        HudLayout.importProperties(hud);
    }

    private static boolean isKnownEntry(String name) {
        return META_ENTRY.equals(name)
                || CLIENT_ENTRY.equals(name)
                || HUD_ENTRY.equals(name)
                || OPTIONS_ENTRY.equals(name);
    }

    private static byte[] readEntryLimited(ZipInputStream zip, long maxBytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = zip.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new IOException("Config bundle entry is too large (maximum 4 MiB per entry).");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static Properties loadProperties(byte[] bytes) throws IOException {
        Properties properties = new Properties();
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            properties.load(input);
        }
        return properties;
    }

    private static void writeOptionsAtomically(byte[] optionsBytes) throws IOException {
        Path options = FabricLoader.getInstance().getGameDir().resolve("options.txt");
        Path backup = options.resolveSibling("options.txt.mazclient-backup");
        Path temp = Files.createTempFile(options.getParent(), "options.txt.mazclient-import-", ".tmp");
        try {
            Files.write(temp, optionsBytes);
            if (Files.exists(options)) {
                Files.copy(options, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temp, options, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, options, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static Path defaultExportPath() {
        return defaultExportDirectory().resolve("MazClient-Config-" + FILE_TIME.format(LocalDateTime.now()) + ".mazconfig");
    }

    private static Path defaultExportDirectory() {
        Path downloads = Path.of(System.getProperty("user.home", "."), "Downloads");
        return Files.isDirectory(downloads) ? downloads : FabricLoader.getInstance().getGameDir();
    }

    private static void validateSlot(int slot) {
        if (slot < 1 || slot > 3) throw new IllegalArgumentException("Profile slot must be 1-3.");
    }

    private static void putProperties(ZipOutputStream zip, String name, Properties properties, String comment) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        properties.store(zip, comment);
        zip.closeEntry();
    }
}
