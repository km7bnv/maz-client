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

    private static Module xpProgressModule;
    private static String displayText = "XP: Level -- | --%";
    private static int displayWidth;

    private XpProgressHud() {}

    public static void tick(Minecraft client) {
        resolveModule();
        if (xpProgressModule == null || !xpProgressModule.isEnabled() || client.player == null) return;

        int level = client.player.experienceLevel;
        int needed = Math.max(1, client.player.getXpNeededForNextLevel());
        float progress = Math.max(0.0F, Math.min(1.0F, client.player.experienceProgress));
        int current = Math.min(needed, Math.round(progress * needed));
        int percent = Math.round(progress * 100.0F);
        String nextText = String.format(Locale.ROOT, "XP: Level %d | %d%% (%d/%d)", level, percent, current, needed);
        if (!nextText.equals(displayText) || displayWidth == 0) {
            displayText = nextText;
            displayWidth = client.font.width(displayText) + 12;
        }
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        resolveModule();
        if (xpProgressModule == null || !xpProgressModule.isEnabled() || client.player == null) return;

        HudLayout.Position p = HudLayout.getPosition("XP Progress", 8, 602);
        int alpha = HudLayout.getOpacity("XP Progress");
        graphics.fill(p.x(), p.y(), p.x() + displayWidth, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
    }

    private static void resolveModule() {
        if (xpProgressModule == null) xpProgressModule = MazClient.MODULE_MANAGER.getModule("XP Progress");
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
