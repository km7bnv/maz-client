package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;

/**
 * Lightweight local chunk-position HUD derived only from the player's client-side position.
 * It never sends packets, changes movement, or touches render/simulation distance.
 */
public final class ChunkPositionHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;
    private static final long REFRESH_INTERVAL_MS = 250L;

    private static Module chunkPositionModule;
    private static long nextRefreshMs;
    private static String displayText = "Chunk: -- -- | In-chunk: -- --";
    private static int displayWidth;

    private ChunkPositionHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (chunkPositionModule == null) {
            chunkPositionModule = MazClient.MODULE_MANAGER.getModule("Chunk Position");
        }
        if (chunkPositionModule == null || !chunkPositionModule.isEnabled() || client.player == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now >= nextRefreshMs) {
            refresh(client);
            nextRefreshMs = now + REFRESH_INTERVAL_MS;
        }

        HudLayout.Position p = HudLayout.getPosition("Chunk Position", 8, 646);
        int alpha = HudLayout.getOpacity("Chunk Position");
        graphics.fill(p.x(), p.y(), p.x() + displayWidth, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
    }

    private static void refresh(Minecraft client) {
        BlockPos pos = client.player.blockPosition();
        int chunkX = Math.floorDiv(pos.getX(), 16);
        int chunkZ = Math.floorDiv(pos.getZ(), 16);
        int inChunkX = Math.floorMod(pos.getX(), 16);
        int inChunkZ = Math.floorMod(pos.getZ(), 16);
        String nextText = "Chunk: " + chunkX + " " + chunkZ + " | In-chunk: " + inChunkX + " " + inChunkZ;

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
