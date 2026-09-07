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
    }

    public String getDisplayText() {
        if (samples.isEmpty()) {
            return latestRawPing >= 0 ? "Ping: " + latestRawPing + " ms" : "Ping: -- ms";
        }

        List<Integer> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        int median = sorted.get(sorted.size() / 2);

        // Keep the HUD stable during one-off latency spikes without hiding them.
        if (latestRawPing >= 150 && latestRawPing >= Math.max(150, median * 2)) {
            return "Ping: " + median + " ms (spike " + latestRawPing + ")";
        }

        return "Ping: " + median + " ms";
    }

    @Override
    protected void onDisable() {
        samples.clear();
        latestRawPing = -1;
        lastSampleAt = 0L;
    }
}
