package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight local inventory-gain feed.
 *
 * Samples aggregate counts from the already-loaded player inventory and records only
 * positive deltas. This avoids slot-move noise and requires no packets, server polling,
 * telemetry, or gameplay automation. The first snapshot after joining a world is used
 * only as a baseline so an existing inventory is never reported as newly gained.
 */
public final class RecentGainsHud {
    private static final long REFRESH_INTERVAL_MS = 250L;
    private static final long ENTRY_LIFETIME_MS = 6_000L;
    private static final int MAX_ENTRIES = 4;
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;

    private static final Map<Item, Integer> previousTotals = new HashMap<>();
    private static final Deque<GainEntry> entries = new ArrayDeque<>();

    private static Module recentGainsModule;
    private static UUID activePlayerId;
    private static long lastRefreshMs = Long.MIN_VALUE;

    private RecentGainsHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (recentGainsModule == null) {
            recentGainsModule = MazClient.MODULE_MANAGER.getModule("Recent Gains");
        }
        if (recentGainsModule == null || !recentGainsModule.isEnabled()) {
            return;
        }
        if (client.player == null) {
            reset();
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= REFRESH_INTERVAL_MS) {
            lastRefreshMs = now;
            refresh(client, now);
        }
        discardExpired(now);

        HudLayout.Position p = HudLayout.getPosition("Recent Gains", 8, 668);
        int alpha = HudLayout.getOpacity("Recent Gains");

        int width = client.font.width("Recent gains: none") + 12;
        for (GainEntry entry : entries) {
            width = Math.max(width, client.font.width(entry.text()) + 12);
        }
        int lines = Math.max(1, entries.size());
        int height = 10 + lines * 10;

        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + height, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + height, withAlpha(ACCENT, alpha));

        int textColor = adaptiveTextColor(alpha);
        if (entries.isEmpty()) {
            graphics.text(client.font, "Recent gains: none", p.x() + 7, p.y() + 7, textColor, false);
            return;
        }

        int y = p.y() + 7;
        for (GainEntry entry : entries) {
            graphics.text(client.font, entry.text(), p.x() + 7, y, textColor, false);
            y += 10;
        }
    }

    private static void refresh(Minecraft client, long now) {
        UUID playerId = client.player.getUUID();
        Map<Item, Integer> currentTotals = new HashMap<>();
        Map<Item, String> displayNames = new HashMap<>();

        for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            Item item = stack.getItem();
            currentTotals.merge(item, stack.getCount(), Integer::sum);
            displayNames.putIfAbsent(item, stack.getHoverName().getString());
        }

        if (!playerId.equals(activePlayerId)) {
            activePlayerId = playerId;
            previousTotals.clear();
            previousTotals.putAll(currentTotals);
            entries.clear();
            return;
        }

        for (Map.Entry<Item, Integer> current : currentTotals.entrySet()) {
            int before = previousTotals.getOrDefault(current.getKey(), 0);
            int gained = current.getValue() - before;
            if (gained > 0) {
                recordGain(displayNames.getOrDefault(current.getKey(), "Item"), gained, now);
            }
        }

        previousTotals.clear();
        previousTotals.putAll(currentTotals);
        discardExpired(now);
    }

    private static void recordGain(String name, int amount, long now) {
        GainEntry newest = entries.peekFirst();
        if (newest != null && newest.name().equals(name) && now - newest.timestampMs() <= 1_000L) {
            entries.removeFirst();
            entries.addFirst(new GainEntry(name, newest.amount() + amount, now));
        } else {
            entries.addFirst(new GainEntry(name, amount, now));
        }
        while (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
    }

    private static void discardExpired(long now) {
        while (!entries.isEmpty() && now - entries.peekLast().timestampMs() > ENTRY_LIFETIME_MS) {
            entries.removeLast();
        }
    }

    private static void reset() {
        activePlayerId = null;
        previousTotals.clear();
        entries.clear();
        lastRefreshMs = Long.MIN_VALUE;
    }

    private static int adaptiveTextColor(int alpha) {
        int clamped = Math.max(0, Math.min(255, alpha));
        int channel = 255 - clamped;
        return 0xFF000000 | (channel << 16) | (channel << 8) | channel;
    }

    private static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    private record GainEntry(String name, int amount, long timestampMs) {
        private String text() {
            return "+" + amount + " " + name;
        }
    }
}
