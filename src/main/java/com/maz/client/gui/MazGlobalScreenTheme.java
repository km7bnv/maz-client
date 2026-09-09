package com.maz.client.gui;

import com.maz.client.MazClient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

/**
 * Applies MazClient visual chrome to normal Minecraft menus without replacing
 * the screen instances or touching their controls/behavior. Inventory/container
 * screens and chat stay untouched so gameplay interactions are never obscured.
 */
public final class MazGlobalScreenTheme implements ClientModInitializer {
    private static final int BG = 0xFF090E1A;
    private static final int HEADER = 0xFF11192A;
    private static final int PANEL = 0xFF141E31;
    private static final int BORDER = 0xFF2A3958;
    private static final int TEXT = 0xFFF8FAFC;
    private static final int MUTED = 0xFF94A3B8;
    private static final int ACCENT = 0xFF5865F2;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!shouldTheme(screen)) return;
            ScreenEvents.afterBackground(screen).register(MazGlobalScreenTheme::renderTheme);
        });
    }

    private static boolean shouldTheme(Screen screen) {
        String className = screen.getClass().getName();

        // MazClient-owned screens already draw their full custom UI.
        if (className.startsWith("com.maz.client.gui.")) return false;

        // Do not cover interactive gameplay surfaces.
        if (className.contains(".screens.inventory.")) return false;
        if (className.endsWith("ChatScreen")) return false;

        return true;
    }

    private static void renderTheme(Screen screen, GuiGraphicsExtractor graphics,
                                    int mouseX, int mouseY, float tickProgress) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        graphics.fill(0, 0, width, height, BG);
        graphics.fill(0, 0, width, 34, HEADER);
        graphics.fill(0, 33, width, 34, ACCENT);
        graphics.fill(8, 42, width - 8, height - 24, PANEL);
        graphics.fill(8, 42, width - 8, 43, BORDER);
        graphics.fill(8, height - 25, width - 8, height - 24, BORDER);

        graphics.text(client.font, "MAZCLIENT", 12, 12, TEXT, false);

        String title = screen.getTitle().getString();
        if (!title.isBlank()) {
            int titleWidth = client.font.width(title);
            int x = Math.max(100, width - 12 - titleWidth);
            graphics.text(client.font, title, x, 12, MUTED, false);
        }

        graphics.text(client.font,
                "Minecraft 26.2  •  MazClient " + MazClient.getVersion(),
                12, height - 17, MUTED, false);
    }
}
