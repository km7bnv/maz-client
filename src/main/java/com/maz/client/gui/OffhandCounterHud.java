package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Lightweight client-side offhand inventory counter.
 *
 * Reads only the already-loaded local player inventory on a short cache interval.
 * It does not send packets, poll servers, automate item use, or alter gameplay.
 */
public final class OffhandCounterHud {
    private static final long REFRESH_INTERVAL_MS = 250L;
    private static final long REFRESH_PHASE_MS = 83L;
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF5865F2;

    private static Module offhandCounterModule;
    private static long nextRefreshMs = Long.MIN_VALUE;
    private static String displayText = "Offhand: --";
    private static int displayWidth = 0;

    private OffhandCounterHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (offhandCounterModule == null) {
            offhandCounterModule = MazClient.MODULE_MANAGER.getModule("Offhand Counter");
        }
        if (offhandCounterModule == null || !offhandCounterModule.isEnabled() || client.player == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now >= nextRefreshMs) {
            refresh(client);
            displayWidth = client.font.width(displayText) + 12;
            scheduleNextRefresh(now);
        }

        HudLayout.Position p = HudLayout.getPosition("Offhand Counter", 8, 646);
        int alpha = HudLayout.getOpacity("Offhand Counter");
        int width = displayWidth > 0 ? displayWidth : client.font.width(displayText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(ACCENT, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
    }

    private static void refresh(Minecraft client) {
        ItemStack offhand = client.player.getOffhandItem();
        if (offhand.isEmpty()) {
            displayText = "Offhand: Empty";
            return;
        }

        int total = 0;
        for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(offhand.getItem())) {
                total += stack.getCount();
            }
        }

        String name = offhand.getHoverName().getString();
        if (offhand.isDamageableItem()) {
            int max = offhand.getMaxDamage();
            int remaining = Math.max(0, max - offhand.getDamageValue());
            int percent = max > 0 ? Math.round((remaining * 100.0F) / max) : 0;
            displayText = String.format(
                    Locale.ROOT,
                    "Offhand: %s | Total: %d | Durability: %d/%d (%d%%)",
                    name,
                    total,
                    remaining,
                    max,
                    percent
            );
        } else {
            displayText = String.format(Locale.ROOT, "Offhand: %s | Total: %d", name, total);
        }
    }

    private static void scheduleNextRefresh(long now) {
        long phasePosition = Math.floorMod(now - REFRESH_PHASE_MS, REFRESH_INTERVAL_MS);
        long delay = REFRESH_INTERVAL_MS - phasePosition;
        nextRefreshMs = now + delay;
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
