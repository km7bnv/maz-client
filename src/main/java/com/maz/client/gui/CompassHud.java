package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class CompassHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static final int MUTED = 0xFF64748B;
    private static Module compassModule;

    private CompassHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (compassModule == null) compassModule = MazClient.MODULE_MANAGER.getModule("Compass");
        if (compassModule == null || !compassModule.isEnabled() || client.player == null) return;

        float yaw = client.player.getYRot();
        float normalized = ((yaw % 360.0F) + 360.0F) % 360.0F;
        int degrees = Math.round(normalized) % 360;
        String direction = directionFor(normalized);
        String left = directionFor(normalized - 45.0F);
        String right = directionFor(normalized + 45.0F);

        HudLayout.Position p = HudLayout.getPosition("Compass", 8, 214);
        int alpha = HudLayout.getOpacity("Compass");
        int width = 152;
        int height = 24;
        int x = p.x();
        int y = p.y();

        graphics.fill(x, y, x + width, y + height, withAlpha(BACKGROUND, alpha));
        graphics.fill(x, y, x + 3, y + height, withAlpha(ACCENT, alpha));
        graphics.text(client.font, left, x + 12, y + 8, withAlpha(MUTED, 255), false);

        String center = direction + "  " + degrees + "°";
        int centerX = x + width / 2 - client.font.width(center) / 2;
        graphics.text(client.font, center, centerX, y + 8, adaptiveTextColor(alpha), false);

        int rightX = x + width - 12 - client.font.width(right);
        graphics.text(client.font, right, rightX, y + 8, withAlpha(MUTED, 255), false);
    }

    private static String directionFor(float yaw) {
        float normalized = ((yaw % 360.0F) + 360.0F) % 360.0F;
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int index = Math.round(normalized / 45.0F) & 7;
        return directions[index];
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
