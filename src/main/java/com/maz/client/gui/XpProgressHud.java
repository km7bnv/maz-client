package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Locale;

public final class XpProgressHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static final long REFRESH_INTERVAL_MS = 250L;

    private static Module xpProgressModule;
    private static long lastRefreshMs;
    private static String displayText = "XP: Level -- | --%";

    private XpProgressHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (xpProgressModule == null) {
            xpProgressModule = MazClient.MODULE_MANAGER.getModule("XP Progress");
        }
        if (xpProgressModule == null || !xpProgressModule.isEnabled() || client.player == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= REFRESH_INTERVAL_MS) {
            lastRefreshMs = now;
            int level = client.player.experienceLevel;
            int needed = Math.max(1, client.player.getXpNeededForNextLevel());
            float progress = Math.max(0.0F, Math.min(1.0F, client.player.experienceProgress));
            int current = Math.min(needed, Math.round(progress * needed));
            int percent = Math.round(progress * 100.0F);
            displayText = String.format(Locale.ROOT, "XP: Level %d | %d%% (%d/%d)", level, percent, current, needed);
        }

        HudLayout.Position p = HudLayout.getPosition("XP Progress", 8, 602);
        int alpha = HudLayout.getOpacity("XP Progress");
        int width = client.font.width(displayText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
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
