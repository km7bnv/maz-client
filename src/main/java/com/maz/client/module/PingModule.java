package com.maz.client.module;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class PingModule extends Module {

    private static final int MAX_SAMPLES = 7;
    private static final long SAMPLE_INTERVAL_MS = 250L;

    private final Deque<Integer> samples = new ArrayDeque<>();
    private long lastSampleAt;
    private int latestRawPing = -1;
    private String displayText = "Ping: -- ms";

    public PingModule() {
        super("Ping", ModuleCategory.HUD);
    }

    public void sample(int rawPing) {
        if (rawPing < 0 || rawPing > 60_000) {
            return;
        }

        long now = System.currentTimeMillis();
        latestRawPing = rawPing;
        if (now - lastSampleAt < SAMPLE_INTERVAL_MS) {
            return;
        }

        lastSampleAt = now;
        samples.addLast(rawPing);
        while (samples.size() > MAX_SAMPLES) {
            samples.removeFirst();
        }
        displayText = buildDisplayText();
    }

    public String getDisplayText() {
        return displayText;
    }

    private String buildDisplayText() {
        if (samples.isEmpty()) {
            return latestRawPing >= 0
                    ? "Ping: " + latestRawPing + " ms | " + qualityLabel(latestRawPing)
                    : "Ping: -- ms";
        }

        List<Integer> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        int median = sorted.get(sorted.size() / 2);
        int jitter = calculateJitter();
        int minimum = sorted.get(0);
        int maximum = sorted.get(sorted.size() - 1);
        String stableQuality = qualityLabel(median);
        String jitterText = samples.size() >= 2 ? " | Jitter: " + jitter + " ms" : "";
        String rangeText = samples.size() >= 2 ? " | Range: " + minimum + "-" + maximum + " ms" : "";
        String stabilityText = samples.size() >= 3 ? " | " + stabilityLabel(median, jitter) : "";

        if (latestRawPing >= 150 && latestRawPing >= Math.max(150, median * 2)) {
            return "Ping: " + median + " ms | " + stableQuality + jitterText + rangeText + stabilityText
                    + " (spike " + latestRawPing + " ms " + qualityLabel(latestRawPing) + ")";
        }

        return "Ping: " + median + " ms | " + stableQuality + jitterText + rangeText + stabilityText;
    }

    private int calculateJitter() {
        if (samples.size() < 2) {
            return 0;
        }

        long totalDelta = 0L;
        int comparisons = 0;
        Integer previous = null;
        for (int sample : samples) {
            if (previous != null) {
                totalDelta += Math.abs(sample - previous);
                comparisons++;
            }
            previous = sample;
        }

        return comparisons == 0 ? 0 : (int) Math.round((double) totalDelta / comparisons);
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
        samples.clear();
        latestRawPing = -1;
        lastSampleAt = 0L;
        displayText = "Ping: -- ms";
    }
}
