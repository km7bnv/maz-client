package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;

public final class InventorySpaceHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT_GOOD = 0xFF57F287;
    private static final int ACCENT_WARN = 0xFFFEE75C;
    private static final int ACCENT_LOW = 0xFFED4245;
    private static final long REFRESH_INTERVAL_MS = 250L;
    private static final int STORAGE_SLOTS = 36;

    private static Module inventorySpaceModule;
    private static long lastRefreshMs = Long.MIN_VALUE;
    private static String displayText = "Inventory: -- free";
    private static int accent = ACCENT_GOOD;

    private InventorySpaceHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (inventorySpaceModule == null) inventorySpaceModule = MazClient.MODULE_MANAGER.getModule("Inventory Space");
        if (inventorySpaceModule == null || !inventorySpaceModule.isEnabled() || client.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= REFRESH_INTERVAL_MS) {
            lastRefreshMs = now;
            Inventory inventory = client.player.getInventory();
            int slotCount = Math.min(STORAGE_SLOTS, inventory.getContainerSize());
            int freeSlots = 0;
            for (int slot = 0; slot < slotCount; slot++) {
                if (inventory.getItem(slot).isEmpty()) freeSlots++;
            }

            displayText = "Inventory: " + freeSlots + " free / " + slotCount;
            accent = freeSlots <= 3 ? ACCENT_LOW : freeSlots <= 9 ? ACCENT_WARN : ACCENT_GOOD;
        }

        HudLayout.Position p = HudLayout.getPosition("Inventory Space", 8, 580);
        int alpha = HudLayout.getOpacity("Inventory Space");
        int width = client.font.width(displayText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(accent, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
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
