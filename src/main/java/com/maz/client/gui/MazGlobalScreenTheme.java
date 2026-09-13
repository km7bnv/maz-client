package com.maz.client.gui;

import com.maz.client.MazClient;
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
 * Applies the classic Maz dark-card shell to normal Minecraft menus while keeping
 * Minecraft's real screen instances, widgets, input and lifecycle in control.
 */
public final class MazGlobalScreenTheme implements ClientModInitializer {
    private static final int BG = 0xFF090E1A;
    private static final int BG_TOP = 0xFF11192A;
    private static final int PANEL = 0xFF141E31;
    private static final int PANEL_2 = 0xFF1C2942;
    private static final int BORDER = 0xFF2A3958;
    private static final int TEXT = 0xFFF8FAFC;
    private static final int MUTED = 0xFF94A3B8;
    private static final int ACCENT = 0xFF5865F2;
    private static final int DISABLED = 0xFF334155;

    private static final int MENU_WIDTH = 540;
    private static final int MENU_HEIGHT = 340;
    private static final int FOOTER_HEIGHT = 28;

    private static boolean titleRepairQueued;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (shouldTheme(screen)) {
                applyMazWidgetLayout(screen, scaledWidth, scaledHeight);
                ScreenEvents.afterBackground(screen).register(MazGlobalScreenTheme::renderTheme);
                ScreenEvents.afterExtract(screen).register(MazGlobalScreenTheme::renderForegroundChrome);
            }

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
        if (className.startsWith("com.maz.client.gui.")) return false;
        if (className.contains(".screens.inventory.")) return false;
        if (className.endsWith("ChatScreen") || className.endsWith("InBedChatScreen")) return false;
        if (className.endsWith("DeathScreen")) return false;
        if (className.endsWith("ReceivingLevelScreen") || className.endsWith("LevelLoadingScreen")) return false;
        if (className.endsWith("ProgressScreen")) return false;
        return className.startsWith("net.minecraft.client.gui.screens.");
    }

    private static void renderTheme(Screen screen, GuiGraphicsExtractor graphics,
                                    int mouseX, int mouseY, float tickProgress) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        Shell shell = shell(width, height);
        graphics.fill(0, 0, width, height, BG);
        graphics.fill(0, 0, width, Math.max(110, height / 3), BG_TOP);
        drawShell(screen, graphics, client, shell, mouseX, mouseY);
    }

    private static void drawShell(Screen screen, GuiGraphicsExtractor graphics, Minecraft client, Shell s,
                                  int mouseX, int mouseY) {
        graphics.fill(s.left - 1, s.top - 1, s.right + 1, s.bottom + 1, BORDER);
        graphics.fill(s.left, s.top, s.right, s.bottom, PANEL);

        graphics.fill(s.left + 16, s.top + 16, s.left + 52, s.top + 52, ACCENT);
        graphics.text(client.font, "M", s.left + 30, s.top + 30, 0xFFFFFFFF, true);
        graphics.text(client.font, "MazClient", s.left + 64, s.top + 20, TEXT, false);
        graphics.text(client.font, "Performance & client settings", s.left + 64, s.top + 37, MUTED, false);
        graphics.fill(s.left, s.top + 68, s.right, s.top + 69, BORDER);

        int actionLeft = s.right - 112;
        boolean actionHover = mouseX >= actionLeft && mouseX <= s.right - 16
                && mouseY >= s.top + 20 && mouseY <= s.top + 48;
        graphics.fill(actionLeft, s.top + 20, s.right - 16, s.top + 48, actionHover ? 0xFF6875FF : ACCENT);
        graphics.centeredText(client.font, "HUD Editor", actionLeft + 48, s.top + 30, 0xFFFFFFFF);

        String screenTitle = screen.getTitle().getString();
        if (screenTitle.isBlank()) screenTitle = screen instanceof TitleScreen ? "Minecraft" : "Menu";
        int contentLeft = s.left + 20;
        graphics.text(client.font, screenTitle, contentLeft, s.top + 86, TEXT, false);
        graphics.text(client.font, "Minecraft menu", contentLeft, s.top + 103, MUTED, false);

        graphics.fill(s.left, s.bottom - FOOTER_HEIGHT, s.right, s.bottom - FOOTER_HEIGHT + 1, BORDER);
        graphics.text(client.font, "MazClient " + MazClient.getVersion(), s.left + 16, s.bottom - 18, MUTED, false);
        String credit = "Made by awnkr_par";
        graphics.text(client.font, credit, s.right - 16 - client.font.width(credit), s.bottom - 18, MUTED, false);
    }

    private static void applyMazWidgetLayout(Screen screen, int width, int height) {
        WidgetBounds bounds = widgetBounds(screen);
        if (bounds.empty) return;
        Shell s = shell(width, height);
        int contentLeft = s.left + 20;
        int contentRight = s.right - 20;
        int contentTop = s.top + 122;
        int contentBottom = s.bottom - FOOTER_HEIGHT - 8;
        int availableWidth = Math.max(1, contentRight - contentLeft);
        int availableHeight = Math.max(1, contentBottom - contentTop);
        int groupWidth = bounds.right - bounds.left;
        int groupHeight = bounds.bottom - bounds.top;
        if (groupWidth > availableWidth || groupHeight > availableHeight) return;

        int dx = contentLeft + (availableWidth - groupWidth) / 2 - bounds.left;
        int dy = contentTop + Math.max(0, (availableHeight - groupHeight) / 2) - bounds.top;
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible) continue;
            widget.setX(widget.getX() + dx);
            widget.setY(widget.getY() + dy);
        }
    }

    private static void renderForegroundChrome(Screen screen, GuiGraphicsExtractor graphics,
                                               int mouseX, int mouseY, float tickProgress) {
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible) continue;
            if (isUtilityIconButton(widget)) {
                renderUtilityIconFrame(graphics, widget, mouseX, mouseY);
            } else if (widget instanceof AbstractButton) {
                renderMazButton(graphics, widget, mouseX, mouseY);
            } else {
                int left = widget.getX() - 1, top = widget.getY() - 1;
                int right = widget.getX() + widget.getWidth() + 1, bottom = widget.getY() + widget.getHeight() + 1;
                int border = widget.isMouseOver(mouseX, mouseY) ? ACCENT : BORDER;
                graphics.fill(left, top, right, top + 1, border);
                graphics.fill(left, bottom - 1, right, bottom, border);
                graphics.fill(left, top, left + 1, bottom, border);
                graphics.fill(right - 1, top, right, bottom, border);
            }
        }
    }

    private static boolean isUtilityIconButton(AbstractWidget widget) {
        return widget instanceof AbstractButton && widget.getWidth() <= 40 && widget.getHeight() <= 40;
    }

    private static void renderUtilityIconFrame(GuiGraphicsExtractor graphics, AbstractWidget widget,
                                               int mouseX, int mouseY) {
        int left = widget.getX() - 1, top = widget.getY() - 1;
        int right = widget.getX() + widget.getWidth() + 1, bottom = widget.getY() + widget.getHeight() + 1;
        int border = widget.active && widget.isMouseOver(mouseX, mouseY) ? ACCENT : BORDER;
        graphics.fill(left, top, right, top + 1, border);
        graphics.fill(left, bottom - 1, right, bottom, border);
        graphics.fill(left, top, left + 1, bottom, border);
        graphics.fill(right - 1, top, right, bottom, border);
    }

    private static void renderMazButton(GuiGraphicsExtractor graphics, AbstractWidget widget,
                                        int mouseX, int mouseY) {
        int left = widget.getX(), top = widget.getY();
        int right = left + widget.getWidth(), bottom = top + widget.getHeight();
        boolean hovered = widget.active && widget.isMouseOver(mouseX, mouseY);
        int fill = widget.active ? (hovered ? ACCENT : PANEL_2) : DISABLED;
        int text = widget.active ? TEXT : MUTED;
        graphics.fill(left, top, right, bottom, fill);
        graphics.fill(left, top, right, top + 1, hovered ? ACCENT : BORDER);
        graphics.fill(left, bottom - 1, right, bottom, hovered ? ACCENT : BORDER);
        graphics.fill(left, top, left + 1, bottom, hovered ? ACCENT : BORDER);
        graphics.fill(right - 1, top, right, bottom, hovered ? ACCENT : BORDER);
        Minecraft client = Minecraft.getInstance();
        graphics.centeredText(client.font, widget.getMessage().getString(), (left + right) / 2,
                top + Math.max(1, (widget.getHeight() - 8) / 2), text);
    }

    private static WidgetBounds widgetBounds(Screen screen) {
        int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, right = Integer.MIN_VALUE, bottom = Integer.MIN_VALUE;
        boolean found = false;
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible || widget.getWidth() <= 0 || widget.getHeight() <= 0) continue;
            left = Math.min(left, widget.getX()); top = Math.min(top, widget.getY());
            right = Math.max(right, widget.getX() + widget.getWidth());
            bottom = Math.max(bottom, widget.getY() + widget.getHeight());
            found = true;
        }
        return found ? new WidgetBounds(left, top, right, bottom, false) : new WidgetBounds(0, 0, 0, 0, true);
    }

    private static Shell shell(int width, int height) {
        int shellWidth = Math.min(MENU_WIDTH, Math.max(320, width - 24));
        int shellHeight = Math.min(MENU_HEIGHT, Math.max(220, height - 24));
        int left = (width - shellWidth) / 2;
        int top = (height - shellHeight) / 2;
        return new Shell(left, top, left + shellWidth, top + shellHeight);
    }

    private record WidgetBounds(int left, int top, int right, int bottom, boolean empty) {}
    private record Shell(int left, int top, int right, int bottom) {}
}
