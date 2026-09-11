package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * Lightweight elytra-flight telemetry from client state Minecraft already has loaded.
 * This HUD never sends packets, changes movement, or touches render/simulation distance.
 */
public final class FlightStatusHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT_GOOD = 0xFF57F287;
    private static final int ACCENT_WARN = 0xFFFEE75C;
    private static final int ACCENT_LOW = 0xFFED4245;
    private static final long REFRESH_INTERVAL_MS = 250L;

    private static Module module;
    private static long lastRefreshMs;
    private static String displayText = "Flight: --";
    private static int accent = ACCENT_GOOD;

    private FlightStatusHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (module == null) module = MazClient.MODULE_MANAGER.getModule("Flight Status");
        if (module == null || !module.isEnabled() || client.player == null || !client.player.isFallFlying()) return;

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= REFRESH_INTERVAL_MS) {
            lastRefreshMs = now;

            double dx = client.player.getX() - client.player.xOld;
            double dy = client.player.getY() - client.player.yOld;
            double dz = client.player.getZ() - client.player.zOld;
            double speed = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0;

            ItemStack chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
            String durabilityText = "--";
            int durabilityPercent = 100;
            if (!chest.isEmpty() && chest.is(Items.ELYTRA) && chest.isDamageableItem()) {
                int max = chest.getMaxDamage();
                int remaining = Math.max(0, max - chest.getDamageValue());
                durabilityPercent = max > 0 ? Math.round((remaining * 100.0F) / max) : 0;
                durabilityText = remaining + "/" + max + " (" + durabilityPercent + "%)";
            }

            int rockets = 0;
            for (int slot = 0; slot < client.player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = client.player.getInventory().getItem(slot);
                if (!stack.isEmpty() && stack.is(Items.FIREWORK_ROCKET)) rockets += stack.getCount();
            }

            displayText = String.format(Locale.ROOT, "Flight: %.1f b/s | Elytra: %s | Rockets: %d", speed, durabilityText, rockets);
            accent = durabilityPercent <= 15 || rockets == 0
                    ? ACCENT_LOW
                    : durabilityPercent <= 30 || rockets <= 8 ? ACCENT_WARN : ACCENT_GOOD;
        }

        HudLayout.Position p = HudLayout.getPosition("Flight Status", 8, 668);
        int alpha = HudLayout.getOpacity("Flight Status");
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
