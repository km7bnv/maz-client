package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.CombatStats;
import com.maz.client.module.CpsModule;
import com.maz.client.module.Module;
import com.maz.client.module.PotCounterModule;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MazHud {

    private static final int TEXT = 0xFF0F172A;
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static final int PRESSED_TEXT = 0xFFFFFFFF;
    private static final int KEY_BG = 0xFFFFFFFF;
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();

        Module fps = MazClient.MODULE_MANAGER.getModule("FPS");
        if (enabled(fps)) drawHudBox(graphics, client, "FPS", "FPS: " + client.getFps(), pos("FPS", 8, 8));

        Module memory = MazClient.MODULE_MANAGER.getModule("Memory");
        if (enabled(memory)) {
            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            long max = runtime.maxMemory();
            long usedMb = used / 1024 / 1024;
            long maxMb = max / 1024 / 1024;
            int percent = max > 0 ? (int) ((used * 100) / max) : 0;
            drawHudBox(graphics, client, "Memory",
                    "RAM: " + usedMb + " / " + maxMb + " MB (" + percent + "%)",
                    pos("Memory", 8, 30));
        }

        Module coordinates = MazClient.MODULE_MANAGER.getModule("Coordinates");
        if (enabled(coordinates) && client.player != null) {
            int playerX = (int) Math.floor(client.player.getX());
            int playerY = (int) Math.floor(client.player.getY());
            int playerZ = (int) Math.floor(client.player.getZ());
            drawHudBox(graphics, client, "Coordinates",
                    "XYZ: " + playerX + " / " + playerY + " / " + playerZ,
                    pos("Coordinates", 8, 52));
        }

        Module ping = MazClient.MODULE_MANAGER.getModule("Ping");
        if (enabled(ping) && client.player != null && client.getConnection() != null) {
            PlayerInfo playerInfo = client.getConnection().getPlayerInfo(client.player.getUUID());
            if (playerInfo != null) {
                drawHudBox(graphics, client, "Ping",
                        "Ping: " + playerInfo.getLatency() + " ms",
                        pos("Ping", 8, 74));
            }
        }

        Module speed = MazClient.MODULE_MANAGER.getModule("Speed");
        if (enabled(speed) && client.player != null) {
            double dx = client.player.getX() - client.player.xOld;
            double dz = client.player.getZ() - client.player.zOld;
            double blocksPerSecond = Math.sqrt(dx * dx + dz * dz) * 20.0;
            drawHudBox(graphics, client, "Speed",
                    String.format(Locale.ROOT, "Speed: %.2f b/s", blocksPerSecond),
                    pos("Speed", 8, 96));
        }

        Module direction = MazClient.MODULE_MANAGER.getModule("Direction");
        if (enabled(direction) && client.player != null) {
            drawHudBox(graphics, client, "Direction",
                    "Facing: " + facingName(client.player.getYRot()),
                    pos("Direction", 8, 118));
        }

        Module clock = MazClient.MODULE_MANAGER.getModule("Clock");
        if (enabled(clock)) {
            drawHudBox(graphics, client, "Clock",
                    "Time: " + LocalTime.now().format(CLOCK_FORMAT),
                    pos("Clock", 8, 140));
        }

        Module session = MazClient.MODULE_MANAGER.getModule("Session Timer");
        if (enabled(session)) {
            long elapsed = Math.max(0L, System.currentTimeMillis() - MazClient.SESSION_START_MILLIS);
            long totalSeconds = elapsed / 1000L;
            long hours = totalSeconds / 3600L;
            long minutes = (totalSeconds % 3600L) / 60L;
            long seconds = totalSeconds % 60L;
            String text = hours > 0
                    ? String.format(Locale.ROOT, "Session: %d:%02d:%02d", hours, minutes, seconds)
                    : String.format(Locale.ROOT, "Session: %02d:%02d", minutes, seconds);
            drawHudBox(graphics, client, "Session Timer", text, pos("Session Timer", 8, 162));
        }

        Module cps = MazClient.MODULE_MANAGER.getModule("CPS");
        if (enabled(cps)) {
            drawHudBox(graphics, client, "CPS",
                    "CPS: L " + CpsModule.getLeftCps() + " | R " + CpsModule.getRightCps(),
                    pos("CPS", 8, 184));
        }

        Module keystrokes = MazClient.MODULE_MANAGER.getModule("Keystrokes");
        if (enabled(keystrokes)) {
            HudLayout.Position p = pos("Keystrokes", 8, 206);
            drawKeystrokes(graphics, client, p.x(), p.y());
        }

        Module potCounter = MazClient.MODULE_MANAGER.getModule("PotCounter");
        if (enabled(potCounter)) {
            drawHudBox(graphics, client, "PotCounter",
                    "Pots: " + PotCounterModule.countPotions(client),
                    pos("PotCounter", 8, 272));
        }

        Module watermark = MazClient.MODULE_MANAGER.getModule("Watermark");
        if (enabled(watermark)) {
            drawHudBox(graphics, client, "Watermark", "MazClient", pos("Watermark", 8, 294));
        }

        Module health = MazClient.MODULE_MANAGER.getModule("Health Display");
        if (enabled(health) && client.player != null) {
            drawHudBox(graphics, client, "Health Display",
                    String.format(Locale.ROOT, "Health: %.1f", client.player.getHealth()),
                    pos("Health Display", 8, 316));
        }

        Module armor = MazClient.MODULE_MANAGER.getModule("Armor HUD");
        if (enabled(armor) && client.player != null) {
            drawHudBox(graphics, client, "Armor HUD",
                    "Armor: " + client.player.getArmorValue(),
                    pos("Armor HUD", 8, 338));
        }

        Module combo = MazClient.MODULE_MANAGER.getModule("Combo Counter");
        if (enabled(combo)) {
            drawHudBox(graphics, client, "Combo Counter", "Combo: " + CombatStats.getCombo(),
                    pos("Combo Counter", 8, 360));
        }

        Module reach = MazClient.MODULE_MANAGER.getModule("Reach Display");
        if (enabled(reach)) {
            drawHudBox(graphics, client, "Reach Display",
                    String.format(Locale.ROOT, "Reach: %.2f", CombatStats.getLastReach()),
                    pos("Reach Display", 8, 382));
        }

        Module potionHud = MazClient.MODULE_MANAGER.getModule("Potion HUD");
        if (enabled(potionHud) && client.player != null) {
            drawHudBox(graphics, client, "Potion HUD",
                    "Effects: " + client.player.getActiveEffects().size(),
                    pos("Potion HUD", 8, 404));
        }

        Module fakeHack = MazClient.MODULE_MANAGER.getModule("Fake Hack Overlay");
        if (enabled(fakeHack)) {
            drawFakeHackOverlay(graphics, client);
        }
    }

    private static void drawFakeHackOverlay(GuiGraphicsExtractor graphics, Minecraft client) {
        String[] lines = {"[Maz] KillAura", "[Maz] Speed", "[Maz] Fly"};
        int y = 10;
        for (String line : lines) {
            int w = client.font.width(line) + 10;
            int x = Math.max(4, client.getWindow().getGuiScaledWidth() - w - 8);
            graphics.fill(x, y, x + w, y + 16, 0xAA020617);
            graphics.text(client.font, line, x + 5, y + 5, 0xFFEF4444, false);
            y += 18;
        }
    }

    private static boolean enabled(Module module) {
        return module != null && module.isEnabled();
    }

    private static HudLayout.Position pos(String module, int defaultX, int defaultY) {
        return HudLayout.getPosition(module, defaultX, defaultY);
    }

    public static int previewWidth(Minecraft client, String moduleName) {
        if (moduleName.equalsIgnoreCase("Keystrokes")) return 64;
        return client.font.width(previewText(moduleName)) + 12;
    }

    public static int previewHeight(String moduleName) {
        return moduleName.equalsIgnoreCase("Keystrokes") ? 64 : 18;
    }

    public static void drawPreview(GuiGraphicsExtractor graphics, Minecraft client, String moduleName, int x, int y) {
        if (moduleName.equalsIgnoreCase("Keystrokes")) {
            int alpha = HudLayout.getOpacity("Keystrokes");
            drawKey(graphics, client, "W", x + 22, y, false, 20, 20, alpha);
            drawKey(graphics, client, "A", x, y + 22, false, 20, 20, alpha);
            drawKey(graphics, client, "S", x + 22, y + 22, true, 20, 20, alpha);
            drawKey(graphics, client, "D", x + 44, y + 22, false, 20, 20, alpha);
            drawKey(graphics, client, "LMB", x, y + 44, false, 30, 20, alpha);
            drawKey(graphics, client, "RMB", x + 32, y + 44, false, 30, 20, alpha);
            return;
        }
        drawHudBox(graphics, client, moduleName, previewText(moduleName), new HudLayout.Position(x, y));
    }

    private static String previewText(String moduleName) {
        return switch (moduleName) {
            case "FPS" -> "FPS: 120";
            case "Memory" -> "RAM: 1024 / 4096 MB (25%)";
            case "Coordinates" -> "XYZ: 100 / 64 / -100";
            case "Ping" -> "Ping: 42 ms";
            case "Speed" -> "Speed: 4.20 b/s";
            case "Direction" -> "Facing: North";
            case "Clock" -> "Time: 12:34 PM";
            case "Session Timer" -> "Session: 12:34";
            case "CPS" -> "CPS: L 8 | R 5";
            case "PotCounter" -> "Pots: 6";
            case "Watermark" -> "MazClient";
            case "Health Display" -> "Health: 20.0";
            case "Armor HUD" -> "Armor: 20";
            case "Combo Counter" -> "Combo: 4";
            case "Reach Display" -> "Reach: 3.12";
            case "Potion HUD" -> "Effects: 2";
            default -> moduleName;
        };
    }

    private static String facingName(float yaw) {
        float normalized = ((yaw % 360.0F) + 360.0F) % 360.0F;
        if (normalized >= 315.0F || normalized < 45.0F) return "South";
        if (normalized < 135.0F) return "West";
        if (normalized < 225.0F) return "North";
        return "East";
    }

    private static void drawKeystrokes(GuiGraphicsExtractor graphics, Minecraft client, int x, int y) {
        int key = 20;
        int gap = 2;
        int alpha = HudLayout.getOpacity("Keystrokes");
        drawKey(graphics, client, "W", x + key + gap, y, client.options.keyUp.isDown(), key, key, alpha);
        int rowY = y + key + gap;
        drawKey(graphics, client, "A", x, rowY, client.options.keyLeft.isDown(), key, key, alpha);
        drawKey(graphics, client, "S", x + key + gap, rowY, client.options.keyDown.isDown(), key, key, alpha);
        drawKey(graphics, client, "D", x + (key + gap) * 2, rowY, client.options.keyRight.isDown(), key, key, alpha);
        int mouseY = rowY + key + gap;
        int mouseWidth = key + 10;
        drawKey(graphics, client, "LMB", x, mouseY, client.options.keyAttack.isDown(), mouseWidth, key, alpha);
        drawKey(graphics, client, "RMB", x + mouseWidth + gap, mouseY, client.options.keyUse.isDown(), mouseWidth, key, alpha);
    }

    private static void drawKey(GuiGraphicsExtractor graphics, Minecraft client, String label,
                                int x, int y, boolean pressed, int width, int height, int alpha) {
        int background = withAlpha(pressed ? ACCENT : KEY_BG, alpha);
        int text = withAlpha(pressed ? PRESSED_TEXT : TEXT, alpha);
        graphics.fill(x, y, x + width, y + height, background);
        int textX = x + (width - client.font.width(label)) / 2;
        int textY = y + (height - 8) / 2;
        graphics.text(client.font, label, textX, textY, text, false);
    }

    private static void drawHudBox(GuiGraphicsExtractor graphics, Minecraft client,
                                   String moduleName, String text, HudLayout.Position p) {
        int alpha = HudLayout.getOpacity(moduleName);
        int width = client.font.width(text) + 12;
        int height = 18;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + height, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + height, withAlpha(ACCENT, alpha));
        graphics.text(client.font, text, p.x() + 7, p.y() + 6, withAlpha(TEXT, alpha), false);
    }

    private static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }
}
