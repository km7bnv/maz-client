package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public final class LightLevelHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static Module lightLevelModule;
    private static long lastRefreshMs = Long.MIN_VALUE;
    private static String lightText = "Light: Block -- | Sky --";

    private LightLevelHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (lightLevelModule == null) lightLevelModule = MazClient.MODULE_MANAGER.getModule("Light Level");
        if (lightLevelModule == null || !lightLevelModule.isEnabled() || client.level == null || client.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= 250L) {
            lastRefreshMs = now;
            BlockPos pos = client.player.blockPosition();
            int block = client.level.getBrightness(LightLayer.BLOCK, pos);
            int sky = client.level.getBrightness(LightLayer.SKY, pos);
            lightText = "Light: Block " + block + " | Sky " + sky;
        }

        HudLayout.Position p = HudLayout.getPosition("Light Level", 8, 536);
        int alpha = HudLayout.getOpacity("Light Level");
        int width = client.font.width(lightText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, lightText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
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
