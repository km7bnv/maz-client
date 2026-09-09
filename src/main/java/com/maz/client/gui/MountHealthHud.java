package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

public final class MountHealthHud {
    private static final int BACKGROUND = 0xFFFFFFFF;
    private static final int ACCENT_GOOD = 0xFF57F287;
    private static final int ACCENT_WARN = 0xFFFEE75C;
    private static final int ACCENT_LOW = 0xFFED4245;
    private static final long REFRESH_INTERVAL_MS = 250L;

    private static Module module;
    private static long lastRefreshMs = Long.MIN_VALUE;
    private static LivingEntity lastMount;
    private static String displayText = "Mount: --";
    private static int accent = ACCENT_GOOD;

    private MountHealthHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (module == null) module = MazClient.MODULE_MANAGER.getModule("Mount Health");
        if (module == null || !module.isEnabled() || client.player == null) return;
        if (!(client.player.getVehicle() instanceof LivingEntity mount)) {
            lastMount = null;
            return;
        }

        long now = System.currentTimeMillis();
        if (mount != lastMount || now - lastRefreshMs >= REFRESH_INTERVAL_MS) {
            lastMount = mount;
            lastRefreshMs = now;
            float health = Math.max(0.0F, mount.getHealth());
            float maxHealth = Math.max(0.0F, mount.getMaxHealth());
            int percent = maxHealth > 0.0F ? Math.round((health * 100.0F) / maxHealth) : 0;
            displayText = String.format(Locale.ROOT, "%s: %.1f / %.1f HP (%d%%)", mount.getName().getString(), health, maxHealth, percent);
            accent = percent <= 25 ? ACCENT_LOW : percent <= 50 ? ACCENT_WARN : ACCENT_GOOD;
        }

        HudLayout.Position p = HudLayout.getPosition("Mount Health", 8, 646);
        int alpha = HudLayout.getOpacity("Mount Health");
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
