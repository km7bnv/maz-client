package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Locale;

public final class BiomeHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static Module biomeModule;
    private static long lastRefreshMs;
    private static String biomeText = "Biome: --";

    private BiomeHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (biomeModule == null) biomeModule = MazClient.MODULE_MANAGER.getModule("Biome");
        if (biomeModule == null || !biomeModule.isEnabled() || client.player == null || client.level == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= 250L) {
            lastRefreshMs = now;
            biomeText = "Biome: " + client.level.getBiome(client.player.blockPosition())
                    .unwrapKey()
                    .map(key -> titleCase(key.identifier().getPath()))
                    .orElse("Unknown");
        }

        HudLayout.Position p = HudLayout.getPosition("Biome", 8, 492);
        int alpha = HudLayout.getOpacity("Biome");
        int width = client.font.width(biomeText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, biomeText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
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
