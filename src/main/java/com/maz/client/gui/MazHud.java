package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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

        Module fpsModule =
                MazClient.MODULE_MANAGER.getModule("FPS");

        if (fpsModule != null && fpsModule.isEnabled()) {
            String text = "FPS: " + client.getFps();

            drawHudBox(
                    graphics,
                    client,
                    text,
                    x,
                    y
            );

            y += 22;
        }

        Module memoryModule =
                MazClient.MODULE_MANAGER.getModule("Memory");

        if (memoryModule != null && memoryModule.isEnabled()) {
            Runtime runtime = Runtime.getRuntime();

            long used =
                    runtime.totalMemory() - runtime.freeMemory();

            long max = runtime.maxMemory();

            long usedMb = used / 1024 / 1024;
            long maxMb = max / 1024 / 1024;

            int percent =
                    max > 0
                            ? (int) ((used * 100) / max)
                            : 0;

            String text =
                    "RAM: "
                            + usedMb
                            + " / "
                            + maxMb
                            + " MB ("
                            + percent
                            + "%)";

            drawHudBox(
                    graphics,
                    client,
                    text,
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

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                BACKGROUND
        );

        graphics.fill(
                x,
                y,
                x + 3,
                y + height,
                ACCENT
        );

        graphics.text(
                client.font,
                text,
                x + 7,
                y + 6,
                TEXT,
                false
        );
    }
}
