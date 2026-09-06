package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.CpsModule;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

public class MazHud {

    private static final int TEXT = 0xFF0F172A;
    private static final int BACKGROUND = 0xEFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;

    public static void render(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker
    ) {
        Minecraft client = Minecraft.getInstance();

        int x = 8;
        int y = 8;

        Module fpsModule = MazClient.MODULE_MANAGER.getModule("FPS");
        if (fpsModule != null && fpsModule.isEnabled()) {
            drawHudBox(graphics, client, "FPS: " + client.getFps(), x, y);
            y += 22;
        }

        Module memoryModule = MazClient.MODULE_MANAGER.getModule("Memory");
        if (memoryModule != null && memoryModule.isEnabled()) {
            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            long max = runtime.maxMemory();
            long usedMb = used / 1024 / 1024;
            long maxMb = max / 1024 / 1024;
            int percent = max > 0 ? (int) ((used * 100) / max) : 0;

            drawHudBox(
                    graphics,
                    client,
                    "RAM: " + usedMb + " / " + maxMb + " MB (" + percent + "%)",
                    x,
                    y
            );
            y += 22;
        }

        Module coordinatesModule = MazClient.MODULE_MANAGER.getModule("Coordinates");
        if (coordinatesModule != null
                && coordinatesModule.isEnabled()
                && client.player != null) {
            int playerX = (int) Math.floor(client.player.getX());
            int playerY = (int) Math.floor(client.player.getY());
            int playerZ = (int) Math.floor(client.player.getZ());

            drawHudBox(
                    graphics,
                    client,
                    "XYZ: " + playerX + " / " + playerY + " / " + playerZ,
                    x,
                    y
            );
            y += 22;
        }

        Module pingModule = MazClient.MODULE_MANAGER.getModule("Ping");
        if (pingModule != null
                && pingModule.isEnabled()
                && client.player != null
                && client.getConnection() != null) {
            PlayerInfo playerInfo =
                    client.getConnection().getPlayerInfo(client.player.getUUID());

            if (playerInfo != null) {
                drawHudBox(
                        graphics,
                        client,
                        "Ping: " + playerInfo.getLatency() + " ms",
                        x,
                        y
                );
                y += 22;
            }
        }

        Module cpsModule = MazClient.MODULE_MANAGER.getModule("CPS");
        if (cpsModule != null && cpsModule.isEnabled()) {
            drawHudBox(
                    graphics,
                    client,
                    "CPS: " + CpsModule.getCps(),
                    x,
                    y
            );
        }
    }

    private static void drawHudBox(
            GuiGraphicsExtractor graphics,
            Minecraft client,
            String text,
            int x,
            int y
    ) {
        int width = client.font.width(text) + 12;
        int height = 18;

        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        graphics.fill(x, y, x + 3, y + height, ACCENT);
        graphics.text(client.font, text, x + 7, y + 6, TEXT, false);
    }
}
