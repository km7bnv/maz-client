package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class InventorySpaceHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT_GOOD = 0xFF57F287;
    private static final int ACCENT_WARN = 0xFFFEE75C;
    private static final int ACCENT_LOW = 0xFFED4245;
    private static final long REFRESH_INTERVAL_MS = 250L;
    private static final long REFRESH_PHASE_MS = 0L;
    private static final int STORAGE_SLOTS = 36;

    private static Module inventorySpaceModule;
    private static long nextRefreshMs = Long.MIN_VALUE;
    private static String displayText = "Inventory: -- free | Partial: --";
    private static int displayWidth = 0;
    private static int accent = ACCENT_GOOD;

    private InventorySpaceHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (inventorySpaceModule == null) inventorySpaceModule = MazClient.MODULE_MANAGER.getModule("Inventory Space");
        if (inventorySpaceModule == null || !inventorySpaceModule.isEnabled() || client.player == null) return;

        long now = System.currentTimeMillis();
        if (now >= nextRefreshMs) {
            refresh(client);
            scheduleNextRefresh(now);
        }

        HudLayout.Position p = HudLayout.getPosition("Inventory Space", 8, 580);
        int alpha = HudLayout.getOpacity("Inventory Space");
        int width = displayWidth > 0 ? displayWidth : client.font.width(displayText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(accent, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
    }

    private static void refresh(Minecraft client) {
        Inventory inventory = client.player.getInventory();
        int slotCount = Math.min(STORAGE_SLOTS, inventory.getContainerSize());
        int freeSlots = 0;
        int partialStacks = 0;
        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                freeSlots++;
            } else if (stack.getMaxStackSize() > 1 && stack.getCount() < stack.getMaxStackSize()) {
                partialStacks++;
            }
        }

        int occupiedPercent = slotCount > 0 ? Math.round(((slotCount - freeSlots) * 100.0F) / slotCount) : 0;
        displayText = "Inventory: " + freeSlots + " free / " + slotCount
                + " | Partial: " + partialStacks
                + " | " + occupiedPercent + "% used";
        displayWidth = client.font.width(displayText) + 12;
        accent = freeSlots <= 3 ? ACCENT_LOW : freeSlots <= 9 ? ACCENT_WARN : ACCENT_GOOD;
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
