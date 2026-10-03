package com.maz.client.module;

import java.util.Arrays;

public class PingModule extends Module {

    private static final int MAX_SAMPLES = 7;
    private static final long SAMPLE_INTERVAL_MS = 250L;

    private final int[] samples = new int[MAX_SAMPLES];
    private final int[] sortedScratch = new int[MAX_SAMPLES];
    private int sampleCount;
    private int nextSampleIndex;
    private long lastSampleAt;
    private int latestRawPing = -1;
    private String statsSuffix = "";
    private String displayText = "Ping: -- ms";

    public PingModule() {
        super("Ping", ModuleCategory.HUD);
    }

    public void sample(int rawPing) {
        if (rawPing < 0 || rawPing > 60_000) {
            return;
        }

        long now = System.currentTimeMillis();
        boolean rawChanged = rawPing != latestRawPing;
        latestRawPing = rawPing;

        boolean statsChanged = false;
        if (lastSampleAt == 0L || now - lastSampleAt >= SAMPLE_INTERVAL_MS) {
            lastSampleAt = now;
            samples[nextSampleIndex] = rawPing;
            nextSampleIndex = (nextSampleIndex + 1) % MAX_SAMPLES;
            if (sampleCount < MAX_SAMPLES) sampleCount++;
            statsSuffix = buildStatsSuffix();
            statsChanged = true;
        }

        if (rawChanged || statsChanged) {
            displayText = "Ping: " + latestRawPing + " ms | " + qualityLabel(latestRawPing) + statsSuffix;
        }
    }

    public String getDisplayText() {
        return displayText;
    }

    private String buildStatsSuffix() {
        if (sampleCount == 0) return "";

        int oldestIndex = sampleCount == MAX_SAMPLES ? nextSampleIndex : 0;
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
        long totalDelta = 0L;
        int previous = 0;

        for (int i = 0; i < sampleCount; i++) {
            int value = samples[(oldestIndex + i) % MAX_SAMPLES];
            sortedScratch[i] = value;
            minimum = Math.min(minimum, value);
            maximum = Math.max(maximum, value);
            if (i > 0) {
                totalDelta += Math.abs(value - previous);
            }
            previous = value;
        }

        int jitter = sampleCount < 2 ? 0 : (int) Math.round((double) totalDelta / (sampleCount - 1));
        Arrays.sort(sortedScratch, 0, sampleCount);
        int median = sortedScratch[sampleCount / 2];
        String medianText = " | Median: " + median + " ms";
        String jitterText = sampleCount >= 2 ? " | Jitter: " + jitter + " ms" : "";
        String rangeText = sampleCount >= 2 ? " | Range: " + minimum + "-" + maximum + " ms" : "";
        String stabilityText = sampleCount >= 3 ? " | " + stabilityLabel(median, jitter) : "";
        return medianText + jitterText + rangeText + stabilityText;
    }

    private static String stabilityLabel(int median, int jitter) {
        int baseline = Math.max(1, median);
        double ratio = (double) jitter / baseline;
        if (jitter <= 10 || ratio <= 0.15) return "Stable";
        if (jitter <= 30 || ratio <= 0.35) return "Variable";
        return "Unstable";
    }

    private static String qualityLabel(int ping) {
        if (ping <= 80) return "Good";
        if (ping <= 150) return "Fair";
        if (ping <= 300) return "HIGH";
        if (ping <= 600) return "SEVERE";
        return "CRITICAL";
    }

    @Override
    protected void onDisable() {
        sampleCount = 0;
        nextSampleIndex = 0;
        latestRawPing = -1;
        lastSampleAt = 0L;
        statsSuffix = "";
        displayText = "Ping: -- ms";
    }
}
