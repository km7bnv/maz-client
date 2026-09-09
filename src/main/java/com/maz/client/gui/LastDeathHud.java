package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Locale;

public final class LastDeathHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFFE74C3C;

    private static Module module;
    private static boolean wasDead;
    private static boolean hasDeath;
    private static int deathX;
    private static int deathY;
    private static int deathZ;
    private static String deathDimension = "Unknown";
    private static String displayText = "Last Death: --";

    private LastDeathHud() {}

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            wasDead = false;
            return;
        }

        boolean dead = client.player.isDeadOrDying();
        if (dead && !wasDead) {
            deathX = (int) Math.floor(client.player.getX());
            deathY = (int) Math.floor(client.player.getY());
            deathZ = (int) Math.floor(client.player.getZ());
            deathDimension = titleCase(client.level.dimension().identifier().getPath());
            displayText = String.format(
                    Locale.ROOT,
                    "Last Death: %d, %d, %d | %s",
                    deathX,
                    deathY,
                    deathZ,
                    deathDimension
            );
            hasDeath = true;
        }
        wasDead = dead;
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (module == null) {
            module = MazClient.MODULE_MANAGER.getModule("Last Death");
        }
        if (module == null || !module.isEnabled() || !hasDeath || client.player == null) {
            return;
        }

        HudLayout.Position p = HudLayout.getPosition("Last Death", 8, 624);
        int alpha = HudLayout.getOpacity("Last Death");
        int width = client.font.width(displayText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
    }

    private static String titleCase(String path) {
        String[] parts = path.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) result.append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return result.isEmpty() ? "Unknown" : result.toString();
    }

    private static int adaptiveTextColor(int alpha) {
        int clamped = Math.max(0, Math.min(255, alpha));
        int channel = 255 - clamped;
        return 0xFF000000 | (channel << 16) | (channel << 8) | channel;
    }

    private static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }
}
