package com.maz.client.module;

import net.minecraft.client.Minecraft;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LivePingProbe {
    private static final long PROBE_INTERVAL_MS = 750L;
    private static final int CONNECT_TIMEOUT_MS = 1000;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "MazClient-LivePing");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    private static final AtomicBoolean PROBE_IN_FLIGHT = new AtomicBoolean();
    private static volatile long lastProbeStartedAt;
    private static volatile int latestPingMs = -1;
    private static volatile InetSocketAddress lastEndpoint;

    private LivePingProbe() {}

    public static void tick(Minecraft client) {
        if (client.getConnection() == null) {
            reset();
            return;
        }

        SocketAddress remote = client.getConnection().getConnection().getRemoteAddress();
        if (!(remote instanceof InetSocketAddress endpoint)) {
            return;
        }

        if (!endpoint.equals(lastEndpoint)) {
            lastEndpoint = endpoint;
            latestPingMs = -1;
            lastProbeStartedAt = 0L;
        }

        long now = System.currentTimeMillis();
        if (now - lastProbeStartedAt < PROBE_INTERVAL_MS || !PROBE_IN_FLIGHT.compareAndSet(false, true)) {
            return;
        }

        lastProbeStartedAt = now;
        EXECUTOR.execute(() -> probe(endpoint));
    }

    public static int getLatestPingMs() {
        return latestPingMs;
    }

    private static void probe(InetSocketAddress endpoint) {
        long start = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.setTcpNoDelay(true);
            socket.connect(endpoint, CONNECT_TIMEOUT_MS);
            long elapsedNs = System.nanoTime() - start;
            latestPingMs = (int) Math.max(1L, Math.round(elapsedNs / 1_000_000.0));
        } catch (Exception ignored) {
            // Keep the last good measurement so a single dropped probe does not make
            // the HUD flicker to an error state. Vanilla PlayerInfo remains fallback.
        } finally {
            PROBE_IN_FLIGHT.set(false);
        }
    }

    private static void reset() {
        lastEndpoint = null;
        latestPingMs = -1;
        lastProbeStartedAt = 0L;
    }
}
