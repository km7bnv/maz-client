package com.maz.client.gui;

import com.maz.client.MazClient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Applies full MazClient visual chrome to normal Minecraft menus without replacing
 * vanilla screen instances. Gameplay/container/chat surfaces are deliberately
 * excluded so inventories, chests, containers and active-game interaction stay untouched.
 */
public final class MazGlobalScreenTheme implements ClientModInitializer {
    private static final int BG = 0xFF070B14;
    private static final int BG_TOP = 0xFF0E1626;
    private static final int PANEL = 0xFF111B2D;
    private static final int PANEL_2 = 0xFF17243A;
    private static final int BORDER = 0xFF314464;
    private static final int TEXT = 0xFFF8FAFC;
    private static final int MUTED = 0xFF94A3B8;
    private static final int ACCENT = 0xFF5865F2;
    private static final int ACCENT_SOFT = 0xFF26315C;

    private static boolean titleRepairQueued;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (shouldTheme(screen)) {
                ScreenEvents.afterBackground(screen).register(MazGlobalScreenTheme::renderTheme);
                ScreenEvents.afterExtract(screen).register(MazGlobalScreenTheme::renderForegroundChrome);
            }

            // Keep the proven 1.7.12 anomaly-only recovery. This never replaces a healthy
            // title screen; it only repairs the rare panorama-only TitleScreen with no widgets.
            if (screen instanceof TitleScreen && Screens.getWidgets(screen).isEmpty() && !titleRepairQueued) {
                titleRepairQueued = true;
                client.execute(() -> {
                    try {
                        Screen current = client.gui.screen();
                        if (current == screen && current instanceof TitleScreen && Screens.getWidgets(current).isEmpty()) {
                            System.err.println("MazClient detected an empty TitleScreen; rebuilding vanilla title UI.");
                            client.gui.setScreen(new TitleScreen());
                        }
                    } finally {
                        titleRepairQueued = false;
                    }
                });
            }
        });
    }

    private static boolean shouldTheme(Screen screen) {
        String className = screen.getClass().getName();

        // MazClient-owned screens already render their own full UI.
        if (className.startsWith("com.maz.client.gui.")) return false;

        // Never skin gameplay/container surfaces. These exclusions intentionally cover
        // inventory, chest, crafting, furnace, horse, beacon, merchant, anvil, etc.
        if (className.contains(".screens.inventory.")) return false;
        if (className.endsWith("ChatScreen") || className.endsWith("InBedChatScreen")) return false;
        if (className.endsWith("DeathScreen")) return false;
        if (className.endsWith("ReceivingLevelScreen") || className.endsWith("LevelLoadingScreen")) return false;
        if (className.endsWith("ProgressScreen")) return false;

        return className.startsWith("net.minecraft.client.gui.screens.");
    }

    private static void renderTheme(Screen screen, GuiGraphicsExtractor graphics,
                                    int mouseX, int mouseY, float tickProgress) {
        if (screen instanceof TitleScreen) {
            renderTitleTheme(graphics);
            return;
        }

        renderMenuTheme(screen, graphics);
    }

    private static void renderTitleTheme(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        graphics.fill(0, 0, width, height, BG);
        graphics.fill(0, 0, width, Math.max(132, height / 3), BG_TOP);
        graphics.fill(0, Math.max(132, height / 3) - 2, width, Math.max(132, height / 3), ACCENT);

        int shellWidth = Math.min(620, Math.max(360, width - 36));
        int shellHeight = Math.min(360, Math.max(250, height - 56));
        int left = (width - shellWidth) / 2;
        int top = Math.max(28, (height - shellHeight) / 2);
        int right = left + shellWidth;
        int bottom = top + shellHeight;

        graphics.fill(left - 2, top - 2, right + 2, bottom + 2, BORDER);
        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, left + 6, bottom, ACCENT);

        int brandRight = Math.min(right - 24, left + 196);
        graphics.fill(left + 22, top + 22, brandRight, bottom - 22, PANEL_2);
        graphics.fill(left + 22, top + 22, brandRight, top + 25, ACCENT_SOFT);
        graphics.fill(left + 38, top + 42, left + 84, top + 88, ACCENT);
        graphics.centeredText(client.font, "M", left + 61, top + 59, 0xFFFFFFFF);
        graphics.text(client.font, "MAZCLIENT", left + 38, top + 105, TEXT, false);
        graphics.text(client.font, "Minecraft 26.2", left + 38, top + 124, MUTED, false);
        graphics.text(client.font, "Performance", left + 38, top + 160, TEXT, false);
        graphics.text(client.font, "HUD tools", left + 38, top + 179, TEXT, false);
        graphics.text(client.font, "PvP QoL", left + 38, top + 198, TEXT, false);
        graphics.text(client.font, "Right Shift  •  Modules", left + 38, bottom - 54, MUTED, false);
        graphics.text(client.font, "v" + MazClient.getVersion(), left + 38, bottom - 35, MUTED, false);

        // Vanilla still owns the actual title controls. The right side is deliberately a Maz shell
        // behind those widgets instead of a replacement TitleScreen, preserving the fixed lifecycle.
        int controlLeft = brandRight + 18;
        graphics.fill(controlLeft, top + 22, right - 22, bottom - 22, BG_TOP);
        graphics.fill(controlLeft, top + 22, controlLeft + 3, bottom - 22, ACCENT_SOFT);
        graphics.text(client.font, "PLAY", controlLeft + 18, top + 38, TEXT, false);
        graphics.text(client.font, "Vanilla controls • MazClient shell", controlLeft + 18, top + 57, MUTED, false);
    }

    private static void renderMenuTheme(Screen screen, GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        graphics.fill(0, 0, width, height, BG);
        graphics.fill(0, 0, width, 42, BG_TOP);
        graphics.fill(0, 40, width, 42, ACCENT);

        int left = 12;
        int top = 54;
        int right = width - 12;
        int bottom = height - 32;
        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, BORDER);
        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, left + 5, bottom, ACCENT_SOFT);
        graphics.fill(left + 18, top + 18, right - 18, top + 46, PANEL_2);

        graphics.text(client.font, "MAZCLIENT", 16, 15, TEXT, false);

        String title = screen.getTitle().getString();
        if (!title.isBlank()) {
            int titleWidth = client.font.width(title);
            int x = Math.max(118, width - 16 - titleWidth);
            graphics.text(client.font, title, x, 15, MUTED, false);
        }

        graphics.text(client.font,
                "Minecraft 26.2  •  MazClient " + MazClient.getVersion(),
                16, height - 22, MUTED, false);
    }

    private static void renderForegroundChrome(Screen screen, GuiGraphicsExtractor graphics,
                                               int mouseX, int mouseY, float tickProgress) {
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible) continue;
            int left = widget.getX() - 1;
            int top = widget.getY() - 1;
            int right = widget.getX() + widget.getWidth() + 1;
            int bottom = widget.getY() + widget.getHeight() + 1;

            int border = widget.isMouseOver(mouseX, mouseY) ? ACCENT : BORDER;
            graphics.fill(left, top, right, top + 1, border);
            graphics.fill(left, bottom - 1, right, bottom, border);
            graphics.fill(left, top, left + 1, bottom, border);
            graphics.fill(right - 1, top, right, bottom, border);
        }
    }
}
