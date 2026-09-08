package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Arrays;
import java.util.Locale;

/**
 * Lightweight client-side frame pacing diagnostics.
 *
 * Samples HUD render intervals only. It does not change rendering, graphics
 * settings, render distance, simulation distance, networking, or gameplay.
 */
public final class FrameStatsHud {
    private static final int SAMPLE_COUNT = 180;
    private static final int RECALCULATE_EVERY = 15;
    private static final long MAX_VALID_FRAME_NANOS = 1_000_000_000L;

    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;

    private static final double[] FRAME_MS = new double[SAMPLE_COUNT];
    private static int sampleSize;
    private static int sampleIndex;
    private static int framesSinceRecalculation;
    private static long previousFrameNanos;
    private static double smoothedFrameMs;
    private static int onePercentLowFps;

    private FrameStatsHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        long now = System.nanoTime();
        sample(now);

        Module module = MazClient.MODULE_MANAGER.getModule("Frame Stats");
        if (module == null || !module.isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (sampleSize == 0) {
            drawBox(graphics, client, "Frame Stats", "Frame: -- ms | 1% low: -- FPS");
            return;
        }

        String text = String.format(
                Locale.ROOT,
                "Frame: %.1f ms | 1%% low: %d FPS",
                smoothedFrameMs,
                onePercentLowFps
        );
        drawBox(graphics, client, "Frame Stats", text);
    }

    private static void sample(long now) {
        if (previousFrameNanos != 0L) {
            long elapsed = now - previousFrameNanos;
            if (elapsed > 0L && elapsed <= MAX_VALID_FRAME_NANOS) {
                double frameMs = elapsed / 1_000_000.0;
                FRAME_MS[sampleIndex] = frameMs;
                sampleIndex = (sampleIndex + 1) % SAMPLE_COUNT;
                if (sampleSize < SAMPLE_COUNT) {
                    sampleSize++;
                }

                framesSinceRecalculation++;
                if (sampleSize == 1 || framesSinceRecalculation >= RECALCULATE_EVERY) {
                    recalculate();
                    framesSinceRecalculation = 0;
                }
            }
        }
        previousFrameNanos = now;
    }

    private static void recalculate() {
        double[] sorted = Arrays.copyOf(FRAME_MS, sampleSize);
        Arrays.sort(sorted);

        double total = 0.0;
        for (double frameMs : sorted) {
            total += frameMs;
        }
        smoothedFrameMs = total / sorted.length;

        int p99Index = Math.min(sorted.length - 1, Math.max(0, (int) Math.ceil(sorted.length * 0.99) - 1));
        double p99FrameMs = sorted[p99Index];
        onePercentLowFps = p99FrameMs > 0.0 ? Math.max(0, (int) Math.round(1000.0 / p99FrameMs)) : 0;
    }

    private static void drawBox(GuiGraphicsExtractor graphics, Minecraft client, String moduleName, String text) {
        HudLayout.Position p = HudLayout.getPosition(moduleName, 8, 492);
        int alpha = HudLayout.getOpacity(moduleName);
        int width = client.font.width(text) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, text, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
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
