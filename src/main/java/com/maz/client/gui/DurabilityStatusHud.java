package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class DurabilityStatusHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT_OK = 0xFF43A047;
    private static final int ACCENT_WARN = 0xFFFFB300;
    private static final int ACCENT_DANGER = 0xFFE53935;
    private static final long REFRESH_INTERVAL_MS = 250L;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static Module durabilityStatusModule;
    private static long lastRefreshMs = Long.MIN_VALUE;
    private static String displayText = "Durability: no damageable gear";
    private static int accent = ACCENT_OK;

    private DurabilityStatusHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (durabilityStatusModule == null) {
            durabilityStatusModule = MazClient.MODULE_MANAGER.getModule("Durability Status");
        }
        if (durabilityStatusModule == null || !durabilityStatusModule.isEnabled() || client.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefreshMs >= REFRESH_INTERVAL_MS) {
            lastRefreshMs = now;
            refresh(client);
        }

        HudLayout.Position p = HudLayout.getPosition("Durability Status", 8, 580);
        int alpha = HudLayout.getOpacity("Durability Status");
        int width = client.font.width(displayText) + 12;
        graphics.fill(p.x(), p.y(), p.x() + width, p.y() + 18, withAlpha(BACKGROUND, alpha));
        graphics.fill(p.x(), p.y(), p.x() + 3, p.y() + 18, withAlpha(accent, alpha));
        graphics.text(client.font, displayText, p.x() + 7, p.y() + 6, adaptiveTextColor(alpha), false);
    }

    private static void refresh(Minecraft client) {
        ItemStack weakest = ItemStack.EMPTY;
        int weakestPercent = 101;

        ItemStack held = client.player.getMainHandItem();
        if (held.isDamageableItem()) {
            weakest = held;
            weakestPercent = percentRemaining(held);
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int percent = percentRemaining(stack);
                if (percent < weakestPercent) {
                    weakest = stack;
                    weakestPercent = percent;
                }
            }
        }

        if (weakest.isEmpty()) {
            displayText = "Durability: no damageable gear";
            accent = ACCENT_OK;
            return;
        }

        int remaining = Math.max(0, weakest.getMaxDamage() - weakest.getDamageValue());
        displayText = "Durability: " + weakest.getHoverName().getString() + " " + remaining + "/" + weakest.getMaxDamage()
                + " (" + weakestPercent + "%)";
        accent = weakestPercent <= 15 ? ACCENT_DANGER : weakestPercent <= 30 ? ACCENT_WARN : ACCENT_OK;
    }

    private static int percentRemaining(ItemStack stack) {
        int max = stack.getMaxDamage();
        if (max <= 0) return 100;
        int remaining = Math.max(0, max - stack.getDamageValue());
        return Math.round((remaining * 100.0F) / max);
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
