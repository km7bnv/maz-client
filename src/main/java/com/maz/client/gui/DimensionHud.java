package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Locale;

public final class DimensionHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static Module dimensionModule;
    private static long lastRefreshMs;
    private static String dimensionText = "Dimension: --";

    private DimensionHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (dimensionModule == null) dimensionModule = MazClient.MODULE_MANAGER.getModule("Dimension");
        if (dimensionModule == null || !dimensionModule.isEnabled() || client.level == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= 500L) {
            lastRefreshMs = now;
            dimensionText = "Dimension: " + titleCase(client.level.dimension().identifier().getPath());
        }

        HudLayout.Position p = HudLayout.getPosition("Dimension", 8, 514);
        int alpha = HudLayout.getOpacity("Dimension");
        int width = client.font.width(dimensionText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, dimensionText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
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
