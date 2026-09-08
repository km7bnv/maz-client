package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class WorldTimeHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static Module worldTimeModule;
    private static long lastRefreshMs = Long.MIN_VALUE;
    private static String timeText = "World Time: Day -- | --:--";

    private WorldTimeHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (worldTimeModule == null) worldTimeModule = MazClient.MODULE_MANAGER.getModule("World Time");
        if (worldTimeModule == null || !worldTimeModule.isEnabled() || client.level == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= 500L) {
            lastRefreshMs = now;
            long dayTime = client.level.getOverworldClockTime();
            long day = Math.floorDiv(dayTime, 24000L) + 1L;
            long tickOfDay = Math.floorMod(dayTime, 24000L);
            int totalMinutes = (int) ((tickOfDay * 1440L) / 24000L);
            totalMinutes = (totalMinutes + 360) % 1440;
            int hour = totalMinutes / 60;
            int minute = totalMinutes % 60;
            timeText = String.format(java.util.Locale.ROOT, "World Time: Day %d | %02d:%02d", day, hour, minute);
        }

        HudLayout.Position p = HudLayout.getPosition("World Time", 8, 558);
        int alpha = HudLayout.getOpacity("World Time");
        int width = client.font.width(timeText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, timeText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
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
