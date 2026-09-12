package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Applies the MazClient 1.4 light-panel visual language to normal
 * Minecraft menus without replacing the underlying vanilla screen instances.
 * Vanilla keeps ownership of widgets, focus, narration, input and transitions.
 */
public final class MazGlobalScreenTheme implements ClientModInitializer {
    // Exact core palette from MazClient v1.4.0's MazMenuScreen.
    private static final int BG = 0xFFF1F5F9;
    private static final int BG_TOP = 0xFFE2E8F0;
    private static final int PANEL = 0xFFFFFFFF;
    private static final int PANEL_HOVER = 0xFFE2E8F0;
    private static final int BORDER = 0xFFCBD5E1;
    private static final int TEXT = 0xFF0F172A;
    private static final int MUTED = 0xFF475569;
    private static final int ACCENT = 0xFF5865F2;
    private static final int ACCENT_HOVER = 0xFF6875FF;
    private static final int SUCCESS = 0xFF16A34A;
    private static final int DISABLED = 0xFFE2E8F0;

    private static boolean titleRepairQueued;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (shouldTheme(screen)) {
                if (screen instanceof TitleScreen) {
                    shiftTitleWidgetsDown(screen, scaledHeight);
                }
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

        int cardWidth = Math.min(500, Math.max(360, width - 32));
        int cardHeight = Math.min(320, Math.max(260, height - 32));
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

        int statusRight = right - 16;
        int statusLeft = Math.max(left + 250, statusRight - 150);
        boolean modulesHover = inside(mouseX, mouseY, statusLeft, top + 20, statusRight, top + 48);
        graphics.fill(statusLeft, top + 20, statusRight, top + 48, modulesHover ? BG_TOP : PANEL);
        graphics.fill(statusLeft, top + 20, statusLeft + 3, top + 48, SUCCESS);
        graphics.text(client.font, enabled + "/" + total + " modules active", statusLeft + 10, top + 30, TEXT, false);

        // Keep the original Maz-home identity while leaving vanilla title widgets untouched.
        if (cardHeight >= 300) {
            graphics.text(client.font, "Your Minecraft, your setup.", left + 20, top + 82, TEXT, false);
            graphics.text(client.font, "Performance, HUD tools and client controls in one place.",
                    left + 20, top + 99, MUTED, false);
        }

        drawFooter(graphics, client, left, right, bottom);
    }

    private static void renderMenuTheme(Screen screen, GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        drawPageBackground(graphics, width, height);

        WidgetBounds widgets = widgetBounds(screen);
        int desiredWidth = widgets.empty ? 500 : Math.max(500, widgets.right - widgets.left + 40);
        int desiredHeight = widgets.empty ? 320 : Math.max(320, widgets.bottom - widgets.top + 96);
        int cardWidth = Math.min(Math.max(320, width - 24), Math.min(660, desiredWidth));
        int cardHeight = Math.min(Math.max(220, height - 24), Math.min(450, desiredHeight));

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

        drawFooter(graphics, client, left, right, bottom);
    }

    private static void drawPageBackground(GuiGraphicsExtractor graphics, int width, int height) {
        graphics.fill(0, 0, width, height, BG);
    }

    private static void drawCard(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, BORDER);
        graphics.fill(left, top, right, bottom, PANEL);
    }

    private static void drawBrandHeader(GuiGraphicsExtractor graphics, Minecraft client,
                                        int left, int top, int right, String title, String subtitle) {
        graphics.fill(left + 16, top + 16, left + 52, top + 52, ACCENT);
        graphics.centeredText(client.font, "M", left + 34, top + 29, 0xFFFFFFFF);
        graphics.text(client.font, title, left + 64, top + 20, TEXT, false);
        graphics.text(client.font, subtitle, left + 64, top + 37, MUTED, false);
        graphics.fill(left, top + 68, right, top + 69, BORDER);

    }

    private static void drawFooter(GuiGraphicsExtractor graphics, Minecraft client,
                                   int left, int right, int bottom) {
        graphics.fill(left, bottom - 27, right, bottom - 26, BORDER);
        graphics.text(client.font, "MazClient " + MazClient.getVersion(), left + 16, bottom - 17, MUTED, false);
        String hint = "Made by awnkr_par";
        int hintWidth = client.font.width(hint);
        if (right - 16 - hintWidth > left + 150) {
            graphics.text(client.font, hint, right - 16 - hintWidth, bottom - 17, MUTED, false);
        }
    }

    private static void renderForegroundChrome(Screen screen, GuiGraphicsExtractor graphics,
                                               int mouseX, int mouseY, float tickProgress) {
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible) continue;

            // Keep Minecraft's compact utility sprites (Language, Accessibility, Mods,
            // etc.) visible. Minecraft also keeps their native hover names and narration.
            if (isUtilityIconButton(widget)) {
                renderUtilityIconFrame(graphics, widget, mouseX, mouseY);
                continue;
            }

            // Keep Minecraft's real behavior while replacing its visible button treatment
            // with the flat white/pale-slate controls used by MazClient v1.4.
            if (widget instanceof AbstractButton) {
                renderMazButton(graphics, widget, mouseX, mouseY);
                continue;
            }

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

    private static boolean isUtilityIconButton(AbstractWidget widget) {
        return widget instanceof AbstractButton
                && widget.getWidth() <= 40
                && widget.getHeight() <= 40;
    }

    private static void renderUtilityIconFrame(GuiGraphicsExtractor graphics, AbstractWidget widget,
                                               int mouseX, int mouseY) {
        int left = widget.getX() - 1;
        int top = widget.getY() - 1;
        int right = widget.getX() + widget.getWidth() + 1;
        int bottom = widget.getY() + widget.getHeight() + 1;
        int border = widget.active && widget.isMouseOver(mouseX, mouseY) ? ACCENT_HOVER : BORDER;
        graphics.fill(left, top, right, top + 1, border);
        graphics.fill(left, bottom - 1, right, bottom, border);
        graphics.fill(left, top, left + 1, bottom, border);
        graphics.fill(right - 1, top, right, bottom, border);
    }

    private static void renderMazButton(GuiGraphicsExtractor graphics, AbstractWidget widget,
                                        int mouseX, int mouseY) {
        int left = widget.getX();
        int top = widget.getY();
        int right = left + widget.getWidth();
        int bottom = top + widget.getHeight();
        boolean hovered = widget.active && widget.isMouseOver(mouseX, mouseY);

        int fill = widget.active ? (hovered ? PANEL_HOVER : PANEL) : DISABLED;
        int border = hovered ? ACCENT_HOVER : BORDER;
        int text = widget.active ? (hovered ? ACCENT : TEXT) : MUTED;

        graphics.fill(left, top, right, bottom, fill);
        graphics.fill(left, top, right, top + 1, border);
        graphics.fill(left, bottom - 1, right, bottom, border);
        graphics.fill(left, top, left + 1, bottom, border);
        graphics.fill(right - 1, top, right, bottom, border);
        if (hovered && widget.getWidth() >= 40) {
            graphics.fill(left, top, left + 3, bottom, ACCENT);
        }

        Minecraft client = Minecraft.getInstance();
        int textY = top + Math.max(1, (widget.getHeight() - 8) / 2);
        graphics.centeredText(client.font, widget.getMessage().getString(), (left + right) / 2, textY, text);
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

    private static void shiftTitleWidgetsDown(Screen screen, int screenHeight) {
        int maxBottom = 0;
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (widget.visible) maxBottom = Math.max(maxBottom, widget.getY() + widget.getHeight());
        }

        int offset = Math.min(12, Math.max(0, screenHeight - 12 - maxBottom));
        if (offset == 0) return;
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (widget.visible) widget.setY(widget.getY() + offset);
        }
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
