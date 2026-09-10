package com.maz.client.gui;

import com.maz.client.MazClient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Applies MazClient visual chrome to normal Minecraft menus without replacing
 * vanilla screen instances. Gameplay/container/chat surfaces are deliberately
 * excluded so inventories, chests, containers and active-game interaction stay untouched.
 */
public final class MazGlobalScreenTheme implements ClientModInitializer {
    private static final int BG = 0xFF090E1A;
    private static final int BG_TOP = 0xFF11192A;
    private static final int PANEL = 0xFF141E31;
    private static final int PANEL_2 = 0xFF18243A;
    private static final int BORDER = 0xFF2A3958;
    private static final int TEXT = 0xFFF8FAFC;
    private static final int MUTED = 0xFF94A3B8;
    private static final int ACCENT = 0xFF5865F2;

    private static boolean titleRepairQueued;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (shouldTheme(screen)) {
                ScreenEvents.afterBackground(screen).register(MazGlobalScreenTheme::renderTheme);
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

        // Everything else under Minecraft's normal screen tree is a menu/settings flow and
        // gets MazClient chrome while its original controls, narration and lifecycle remain intact.
        return className.startsWith("net.minecraft.client.gui.screens.");
    }

    private static void renderTheme(Screen screen, GuiGraphicsExtractor graphics,
                                    int mouseX, int mouseY, float tickProgress) {
        if (screen instanceof TitleScreen) {
            renderTitleTheme(screen, graphics);
            return;
        }

        renderMenuTheme(screen, graphics);
    }

    private static void renderTitleTheme(Screen screen, GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        graphics.fill(0, 0, width, height, BG);
        graphics.fill(0, 0, width, Math.max(120, height / 3), BG_TOP);

        int cardWidth = Math.min(560, Math.max(320, width - 48));
        int cardHeight = Math.min(330, Math.max(230, height - 80));
        int left = (width - cardWidth) / 2;
        int top = Math.max(42, (height - cardHeight) / 2);
        int right = left + cardWidth;
        int bottom = top + cardHeight;

        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, BORDER);
        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, left + 5, bottom, ACCENT);
        graphics.fill(left + 22, top + 22, right - 22, top + 78, PANEL_2);

        graphics.text(client.font, "MAZCLIENT", left + 34, top + 34, TEXT, false);
        graphics.text(client.font, "Minecraft 26.2", left + 34, top + 54, MUTED, false);
        graphics.text(client.font, "Performance • HUD • PvP QoL", left + 34, top + 92, MUTED, false);

        // Vanilla owns and renders every real title-screen widget above this card. This keeps
        // Singleplayer/Multiplayer/Options/etc. fully functional without reintroducing a screen swap.
        graphics.fill(left + 22, bottom - 44, right - 22, bottom - 43, BORDER);
        graphics.text(client.font, "MazClient " + MazClient.getVersion(), left + 34, bottom - 28, MUTED, false);
    }

    private static void renderMenuTheme(Screen screen, GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        graphics.fill(0, 0, width, height, BG);
        graphics.fill(0, 0, width, 36, BG_TOP);
        graphics.fill(0, 35, width, 36, ACCENT);
        graphics.fill(10, 46, width - 10, height - 28, PANEL);
        graphics.fill(10, 46, width - 10, 47, BORDER);
        graphics.fill(10, height - 29, width - 10, height - 28, BORDER);

        graphics.text(client.font, "MAZCLIENT", 14, 13, TEXT, false);

        String title = screen.getTitle().getString();
        if (!title.isBlank()) {
            int titleWidth = client.font.width(title);
            int x = Math.max(110, width - 14 - titleWidth);
            graphics.text(client.font, title, x, 13, MUTED, false);
        }

        graphics.text(client.font,
                "Minecraft 26.2  •  MazClient " + MazClient.getVersion(),
                14, height - 20, MUTED, false);
    }
}
