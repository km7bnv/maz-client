package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Applies the original MazClient compact dark-card visual language to normal
 * Minecraft menus without replacing the underlying vanilla screen instances.
 * Vanilla keeps ownership of widgets, focus, narration, input and transitions.
 */
public final class MazGlobalScreenTheme implements ClientModInitializer {
    // Keep these in sync with the original MazHomeScreen / MazPauseScreen palette.
    private static final int BG = 0xFF090E1A;
    private static final int BG_TOP = 0xFF11192A;
    private static final int PANEL = 0xFF141E31;
    private static final int PANEL_HOVER = 0xFF1C2942;
    private static final int BORDER = 0xFF2A3958;
    private static final int TEXT = 0xFFF8FAFC;
    private static final int MUTED = 0xFF94A3B8;
    private static final int ACCENT = 0xFF5865F2;
    private static final int ACCENT_HOVER = 0xFF6875FF;
    private static final int SUCCESS = 0xFF22C55E;

    private static boolean titleRepairQueued;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (shouldTheme(screen)) {
                ScreenEvents.afterBackground(screen).register(MazGlobalScreenTheme::renderTheme);
                ScreenEvents.afterExtract(screen).register(MazGlobalScreenTheme::renderForegroundChrome);
            }

            // Preserve the proven anomaly-only recovery from 1.7.12. Healthy title
            // screens are never replaced; only a real panorama-only/no-widget state is rebuilt.
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

        // MazClient-owned screens already draw the old Maz card UI themselves.
        if (className.startsWith("com.maz.client.gui.")) return false;

        // Gameplay/container surfaces intentionally stay vanilla.
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
            renderTitleTheme(screen, graphics, mouseX, mouseY);
        } else {
            renderMenuTheme(screen, graphics);
        }
    }

    private static void renderTitleTheme(Screen screen, GuiGraphicsExtractor graphics,
                                         int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        drawPageBackground(graphics, width, height);

        int cardWidth = Math.min(540, Math.max(360, width - 32));
        int cardHeight = Math.min(334, Math.max(260, height - 32));
        int left = (width - cardWidth) / 2;
        int top = Math.max(16, (height - cardHeight) / 2);
        int right = left + cardWidth;
        int bottom = top + cardHeight;

        drawCard(graphics, left, top, right, bottom);
        drawBrandHeader(graphics, client, left, top, right, "MazClient", "Version " + MazClient.getVersion());

        int enabled = 0;
        int total = MazClient.MODULE_MANAGER.getModules().size();
        for (Module module : MazClient.MODULE_MANAGER.getModules()) {
            if (module.isEnabled()) enabled++;
        }

        int statusRight = right - 24;
        int statusLeft = Math.max(left + 240, statusRight - 150);
        boolean modulesHover = inside(mouseX, mouseY, statusLeft, top + 25, statusRight, top + 68);
        graphics.fill(statusLeft, top + 25, statusRight, top + 68, modulesHover ? PANEL_HOVER : BG_TOP);
        graphics.fill(statusLeft, top + 25, statusLeft + 3, top + 68, SUCCESS);
        graphics.text(client.font, enabled + "/" + total + " modules active", statusLeft + 10, top + 33, TEXT, false);
        graphics.text(client.font, "Right Shift  •  Modules", statusLeft + 10, top + 52,
                modulesHover ? TEXT : MUTED, false);

        // Keep the original Maz-home identity while leaving vanilla title widgets untouched.
        if (cardHeight >= 300) {
            graphics.text(client.font, "Your Minecraft, your setup.", left + 24, top + 88, TEXT, false);
            graphics.text(client.font, "Performance, HUD tools and client controls in one place.",
                    left + 24, top + 105, MUTED, false);
        }

        drawFooter(graphics, client, left, right, bottom);
    }

    private static void renderMenuTheme(Screen screen, GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        drawPageBackground(graphics, width, height);

        WidgetBounds widgets = widgetBounds(screen);
        int desiredWidth = widgets.empty ? 500 : Math.max(360, widgets.right - widgets.left + 56);
        int desiredHeight = widgets.empty ? 290 : Math.max(230, widgets.bottom - widgets.top + 118);
        int cardWidth = Math.min(Math.max(320, width - 24), Math.min(620, desiredWidth));
        int cardHeight = Math.min(Math.max(220, height - 24), Math.min(430, desiredHeight));

        int centerX = widgets.empty ? width / 2 : (widgets.left + widgets.right) / 2;
        int centerY = widgets.empty ? height / 2 : (widgets.top + widgets.bottom) / 2 + 8;
        int left = clamp(centerX - cardWidth / 2, 12, Math.max(12, width - cardWidth - 12));
        int top = clamp(centerY - cardHeight / 2, 12, Math.max(12, height - cardHeight - 12));
        int right = left + cardWidth;
        int bottom = top + cardHeight;

        drawCard(graphics, left, top, right, bottom);

        String title = screen.getTitle().getString();
        if (title.isBlank()) title = "Minecraft";
        drawBrandHeader(graphics, client, left, top, right, title, "MazClient " + MazClient.getVersion());

        // Subtle content well behind the vanilla-owned controls. This recreates the old
        // Maz card feeling without drawing over, moving, replacing, or reimplementing widgets.
        int wellLeft = left + 20;
        int wellTop = top + 82;
        int wellRight = right - 20;
        int wellBottom = bottom - 42;
        if (wellBottom > wellTop) {
            graphics.fill(wellLeft, wellTop, wellRight, wellBottom, BG_TOP);
            graphics.fill(wellLeft, wellTop, wellLeft + 3, wellBottom, ACCENT);
        }

        drawFooter(graphics, client, left, right, bottom);
    }

    private static void drawPageBackground(GuiGraphicsExtractor graphics, int width, int height) {
        graphics.fill(0, 0, width, height, BG);
        graphics.fill(0, 0, width, Math.max(120, height / 3), BG_TOP);
    }

    private static void drawCard(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, BORDER);
        graphics.fill(left, top, right, bottom, PANEL);
    }

    private static void drawBrandHeader(GuiGraphicsExtractor graphics, Minecraft client,
                                        int left, int top, int right, String title, String subtitle) {
        graphics.fill(left + 24, top + 24, left + 72, top + 72, ACCENT);
        graphics.centeredText(client.font, "M", left + 48, top + 42, 0xFFFFFFFF);
        graphics.text(client.font, title, left + 88, top + 28, TEXT, false);
        graphics.text(client.font, subtitle, left + 88, top + 48, MUTED, false);

        // Small top-right identity treatment used instead of the newer full-width header bar.
        int badgeRight = right - 24;
        int badgeWidth = Math.min(132, Math.max(84, client.font.width("MAZCLIENT") + 24));
        int badgeLeft = badgeRight - badgeWidth;
        if (badgeLeft > left + 210) {
            graphics.fill(badgeLeft, top + 29, badgeRight, top + 60, BG_TOP);
            graphics.fill(badgeLeft, top + 29, badgeLeft + 3, top + 60, ACCENT);
            graphics.centeredText(client.font, "MAZCLIENT", (badgeLeft + badgeRight) / 2, top + 40, MUTED);
        }
    }

    private static void drawFooter(GuiGraphicsExtractor graphics, Minecraft client,
                                   int left, int right, int bottom) {
        graphics.fill(left + 24, bottom - 43, right - 24, bottom - 42, BORDER);
        graphics.text(client.font, "MazClient " + MazClient.getVersion(), left + 24, bottom - 27, MUTED, false);
        String hint = "Vanilla controls • Maz style";
        int hintWidth = client.font.width(hint);
        if (right - 24 - hintWidth > left + 150) {
            graphics.text(client.font, hint, right - 24 - hintWidth, bottom - 27, MUTED, false);
        }
    }

    private static void renderForegroundChrome(Screen screen, GuiGraphicsExtractor graphics,
                                               int mouseX, int mouseY, float tickProgress) {
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible) continue;

            int left = widget.getX() - 1;
            int top = widget.getY() - 1;
            int right = widget.getX() + widget.getWidth() + 1;
            int bottom = widget.getY() + widget.getHeight() + 1;
            boolean hovered = widget.isMouseOver(mouseX, mouseY);
            int border = hovered ? ACCENT_HOVER : BORDER;

            graphics.fill(left, top, right, top + 1, border);
            graphics.fill(left, bottom - 1, right, bottom, border);
            graphics.fill(left, top, left + 1, bottom, border);
            graphics.fill(right - 1, top, right, bottom, border);

            // Match the old Maz screens' accent language without covering vanilla text/content.
            if (hovered && widget.getWidth() >= 40) {
                graphics.fill(left, top, left + 2, bottom, ACCENT);
            }
        }
    }

    private static WidgetBounds widgetBounds(Screen screen) {
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        boolean found = false;

        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible || widget.getWidth() <= 0 || widget.getHeight() <= 0) continue;
            left = Math.min(left, widget.getX());
            top = Math.min(top, widget.getY());
            right = Math.max(right, widget.getX() + widget.getWidth());
            bottom = Math.max(bottom, widget.getY() + widget.getHeight());
            found = true;
        }

        return found ? new WidgetBounds(left, top, right, bottom, false)
                : new WidgetBounds(0, 0, 0, 0, true);
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }

    private static boolean inside(double x, double y, int left, int top, int right, int bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private static final class WidgetBounds {
        private final int left;
        private final int top;
        private final int right;
        private final int bottom;
        private final boolean empty;

        private WidgetBounds(int left, int top, int right, int bottom, boolean empty) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.empty = empty;
        }
    }
}
