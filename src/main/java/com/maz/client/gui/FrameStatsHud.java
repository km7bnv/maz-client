package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Lightweight client-side frame pacing diagnostics.
 *
 * Samples active-window HUD render intervals and JVM garbage-collector counters
 * only. Intentional background throttling is excluded so Dynamic FPS and normal
 * alt-tab behavior do not masquerade as gameplay stutter. This does not change
 * rendering, graphics settings, render distance, simulation distance,
 * networking, JVM settings, or gameplay.
 */
public final class FrameStatsHud {
    private static final int SAMPLE_COUNT = 180;
    private static final long RECALCULATE_INTERVAL_NANOS = 200_000_000L;
    private static final long GC_SAMPLE_INTERVAL_NANOS = 1_000_000_000L;
    private static final long MAX_VALID_FRAME_NANOS = 1_000_000_000L;
    private static final double STUTTER_THRESHOLD_MS = 50.0;

    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT_STABLE = 0xFF22C55E;
    private static final int ACCENT_WARNING = 0xFFF59E0B;
    private static final int ACCENT_STUTTER = 0xFFEF4444;

    private static final double[] FRAME_MS = new double[SAMPLE_COUNT];
    private static final double[] SORT_BUFFER = new double[SAMPLE_COUNT];
    private static final List<GarbageCollectorMXBean> GC_BEANS = ManagementFactory.getGarbageCollectorMXBeans();
    private static int sampleSize;
    private static int sampleIndex;
    private static long previousFrameNanos;
    private static long lastRecalculationNanos;
    private static long lastGcSampleNanos;
    private static long previousGcCollections = -1L;
    private static long previousGcTimeMs = -1L;
    private static long recentGcCollections;
    private static long recentGcTimeMs;
    private static double smoothedFrameMs;
    private static double p99FrameMs;
    private static int onePercentLowFps;
    private static int recentStutters;
    private static String displayText = initialDisplayText();
    private static int displayAccent = ACCENT_WARNING;
    private static int cachedWidth;
    private static boolean widthDirty = true;

    private FrameStatsHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        Module module = MazClient.MODULE_MANAGER.getModule("Frame Stats");
        if (module == null || !module.isEnabled()) {
            resetSamplingWindow();
            return;
        }

        if (!client.isWindowActive()) {
            // Dynamic FPS and vanilla focus handling can intentionally reduce the
            // render rate while Minecraft is in the background. Reset the active
            // sample window instead of recording those expected gaps as stutters.
            resetSamplingWindow();
            return;
        }

        sample(System.nanoTime());
        drawBox(graphics, client, "Frame Stats", displayText, displayAccent);
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

                if (sampleSize == 1 || lastRecalculationNanos == 0L
                        || now - lastRecalculationNanos >= RECALCULATE_INTERVAL_NANOS) {
                    recalculate(now);
                    lastRecalculationNanos = now;
                }
            }
        }
        previousFrameNanos = now;
    }

    private static void recalculate(long now) {
        System.arraycopy(FRAME_MS, 0, SORT_BUFFER, 0, sampleSize);
        Arrays.sort(SORT_BUFFER, 0, sampleSize);

        double total = 0.0;
        recentStutters = 0;
        for (int i = 0; i < sampleSize; i++) {
            double frameMs = SORT_BUFFER[i];
            total += frameMs;
            if (frameMs >= STUTTER_THRESHOLD_MS) {
                recentStutters++;
            }
        }
        smoothedFrameMs = total / sampleSize;

        int p99Index = Math.min(sampleSize - 1, Math.max(0, (int) Math.ceil(sampleSize * 0.99) - 1));
        p99FrameMs = SORT_BUFFER[p99Index];
        onePercentLowFps = p99FrameMs > 0.0 ? Math.max(0, (int) Math.round(1000.0 / p99FrameMs)) : 0;
        if (lastGcSampleNanos == 0L || now - lastGcSampleNanos >= GC_SAMPLE_INTERVAL_NANOS) {
            sampleGarbageCollection();
            lastGcSampleNanos = now;
        }
        displayText = String.format(
                Locale.ROOT,
                "Frame: %.1f ms | p99: %.1f ms | 1%% low: %d FPS | stutters: %d | GC: +%d / %d ms",
                smoothedFrameMs,
                p99FrameMs,
                onePercentLowFps,
                recentStutters,
                recentGcCollections,
                recentGcTimeMs
        );
        displayAccent = frameHealthAccent();
        widthDirty = true;
    }

    private static void sampleGarbageCollection() {
        long totalCollections = 0L;
        long totalTimeMs = 0L;
        for (GarbageCollectorMXBean bean : GC_BEANS) {
            long collections = bean.getCollectionCount();
            long timeMs = bean.getCollectionTime();
            if (collections >= 0L) {
                totalCollections += collections;
            }
            if (timeMs >= 0L) {
                totalTimeMs += timeMs;
            }
        }

        if (previousGcCollections < 0L || previousGcTimeMs < 0L) {
            recentGcCollections = 0L;
            recentGcTimeMs = 0L;
        } else {
            recentGcCollections = Math.max(0L, totalCollections - previousGcCollections);
            recentGcTimeMs = Math.max(0L, totalTimeMs - previousGcTimeMs);
        }
        previousGcCollections = totalCollections;
        previousGcTimeMs = totalTimeMs;
    }

    private static int frameHealthAccent() {
        if (sampleSize < 30 || smoothedFrameMs <= 0.0 || onePercentLowFps <= 0) {
            return ACCENT_WARNING;
        }

        if (recentStutters >= 3 || p99FrameMs >= STUTTER_THRESHOLD_MS) {
            return ACCENT_STUTTER;
        }

        double averageFps = 1000.0 / smoothedFrameMs;
        double lowRatio = onePercentLowFps / averageFps;
        if (lowRatio >= 0.80) {
            return ACCENT_STABLE;
        }
        if (lowRatio >= 0.60) {
            return ACCENT_WARNING;
        }
        return ACCENT_STUTTER;
    }

    private static void resetSamplingWindow() {
        sampleSize = 0;
        sampleIndex = 0;
        previousFrameNanos = 0L;
        lastRecalculationNanos = 0L;
        lastGcSampleNanos = 0L;
        previousGcCollections = -1L;
        previousGcTimeMs = -1L;
        recentGcCollections = 0L;
        recentGcTimeMs = 0L;
        smoothedFrameMs = 0.0;
        p99FrameMs = 0.0;
        onePercentLowFps = 0;
        recentStutters = 0;
        displayText = initialDisplayText();
        displayAccent = ACCENT_WARNING;
        widthDirty = true;
    }

    private static String initialDisplayText() {
        return "Frame: warming up | p99: -- ms | 1% low: -- FPS | stutters: -- | GC: --";
    }

    private static void drawBox(GuiGraphicsExtractor graphics, Minecraft client, String moduleName, String text, int accent) {
        HudLayout.Position p = HudLayout.getPosition(moduleName, 8, 492);
        int alpha = HudLayout.getOpacity(moduleName);
        if (widthDirty) {
            cachedWidth = client.font.width(text) + 12;
            widthDirty = false;
        }
        graphics.fill(p.x(), p.y(), p.x() + cachedWidth, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(accent, alpha));
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
