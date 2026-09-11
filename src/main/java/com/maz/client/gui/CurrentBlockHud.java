package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public final class CurrentBlockHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static final long REFRESH_INTERVAL_MS = 100L;

    private static Module currentBlockModule;
    private static long nextRefreshMs;
    private static String displayText = "Block: --";
    private static int displayWidth;

    private CurrentBlockHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (currentBlockModule == null) {
            currentBlockModule = MazClient.MODULE_MANAGER.getModule("Current Block");
        }
        if (currentBlockModule == null || !currentBlockModule.isEnabled() || client.level == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now >= nextRefreshMs) {
            refresh(client);
            nextRefreshMs = now + REFRESH_INTERVAL_MS;
        }

        HudLayout.Position p = HudLayout.getPosition("Current Block", 8, 624);
        int alpha = HudLayout.getOpacity("Current Block");
        graphics.fill(p.x(), p.y(), p.x() + displayWidth, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
    }

    private static void refresh(Minecraft client) {
        String nextText = "Block: --";
        if (client.hitResult instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            String blockName = client.level.getBlockState(pos).getBlock().getName().getString();
            nextText = "Block: " + blockName + " | " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        }

        if (!nextText.equals(displayText) || displayWidth == 0) {
            displayText = nextText;
            displayWidth = client.font.width(displayText) + 12;
        }
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
