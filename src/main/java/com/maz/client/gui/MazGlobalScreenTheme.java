package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.ModuleCategory;
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
 * Applies the original MazClient v1.4 shell geometry to normal Minecraft menus while
 * keeping Minecraft's real screen instances and widgets in control of behavior.
 */
public final class MazGlobalScreenTheme implements ClientModInitializer {
    // Exact v1.4.0 palette. Do not reinterpret this as the later dark theme.
    private static final int BG = 0xFFF1F5F9;
    private static final int PANEL = 0xFFFFFFFF;
    private static final int PANEL_2 = 0xFFE2E8F0;
    private static final int BORDER = 0xFFCBD5E1;
    private static final int TEXT = 0xFF0F172A;
    private static final int MUTED = 0xFF475569;
    private static final int ACCENT = 0xFF5865F2;
    private static final int DISABLED = 0xFFCBD5E1;

    // Exact values from v1.4.0 MazMenuScreen.
    private static final int MENU_WIDTH = 500;
    private static final int MENU_HEIGHT = 320;
    private static final int SIDEBAR_WIDTH = 125;
    private static final int HEADER_HEIGHT = 69;
    private static final int FOOTER_HEIGHT = 27;
    private static final int SIDEBAR_ROW_HEIGHT = 26;
    private static final int SIDEBAR_ROW_STEP = 34;

    private static boolean titleRepairQueued;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (shouldTheme(screen)) {
                applyV14WidgetLayout(screen, scaledWidth, scaledHeight);
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
        Shell shell = shell(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
        drawPageBackground(graphics, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
        drawV14Shell(screen, graphics, client, shell, mouseX, mouseY);
    }

    private static void drawV14Shell(Screen screen, GuiGraphicsExtractor graphics, Minecraft client, Shell s,
                                     int mouseX, int mouseY) {
        graphics.fill(s.left - 1, s.top - 1, s.right + 1, s.bottom + 1, BORDER);
        graphics.fill(s.left, s.top, s.right, s.bottom, PANEL);

        // v1.4 header: square M mark, two-line title block, then a full-width divider.
        graphics.fill(s.left + 16, s.top + 16, s.left + 52, s.top + 52, ACCENT);
        graphics.text(client.font, "M", s.left + 30, s.top + 30, 0xFFFFFFFF, true);
        graphics.text(client.font, "MazClient", s.left + 64, s.top + 20, TEXT, false);
        graphics.text(client.font, "Performance & client settings", s.left + 64, s.top + 37, MUTED, false);
        graphics.fill(s.left, s.top + 68, s.right, s.top + 69, BORDER);

        // Exact v1.4 header action, including its original label.
        int actionLeft = s.right - 112;
        graphics.fill(actionLeft, s.top + 20, s.right - 16, s.top + 48, ACCENT);
        graphics.centeredText(client.font, "HUD Editor", actionLeft + 48, s.top + 30, 0xFFFFFFFF);

        // v1.4 sidebar/content split. The rows are informational so native Minecraft widgets
        // remain the only interactive controls on third-party/vanilla screens.
        int sidebarRight = s.left + s.sidebarWidth;
        graphics.fill(sidebarRight, s.top + HEADER_HEIGHT, sidebarRight + 1, s.bottom, BORDER);
        int rowY = s.top + 82;
        boolean first = true;
        for (ModuleCategory category : ModuleCategory.values()) {
            drawSidebarRow(graphics, client, s.left, sidebarRight, rowY,
                    category.getDisplayName(), first, mouseX, mouseY);
            first = false;
            rowY += SIDEBAR_ROW_STEP;
        }

        String screenTitle = screen.getTitle().getString();
        if (screenTitle.isBlank()) screenTitle = screen instanceof TitleScreen ? "Minecraft" : "Menu";
        int contentLeft = sidebarRight + 20;
        graphics.text(client.font, screenTitle, contentLeft, s.top + 86, TEXT, false);
        graphics.text(client.font, "Modules", contentLeft, s.top + 103, MUTED, false);

        // Exact v1.4 footer split.
        graphics.fill(s.left, s.bottom - FOOTER_HEIGHT, s.right, s.bottom - FOOTER_HEIGHT + 1, BORDER);
        graphics.text(client.font, "MazClient " + MazClient.getVersion(), s.left + 16, s.bottom - 17, MUTED, false);
        String credit = "Made by awnkr_par";
        int creditWidth = client.font.width(credit);
        graphics.text(client.font, credit, s.right - 16 - creditWidth, s.bottom - 17, MUTED, false);
    }

    private static void drawSidebarRow(GuiGraphicsExtractor graphics, Minecraft client,
                                       int left, int sidebarRight, int y, String label, boolean selected,
                                       int mouseX, int mouseY) {
        int rowLeft = left + 10;
        int rowRight = sidebarRight - 10;
        boolean hovered = mouseX >= rowLeft && mouseX <= rowRight
                && mouseY >= y && mouseY <= y + SIDEBAR_ROW_HEIGHT;
        if (selected || hovered) {
            graphics.fill(rowLeft, y, rowRight, y + SIDEBAR_ROW_HEIGHT, PANEL_2);
        }
        if (selected) {
            graphics.fill(rowLeft, y, rowLeft + 3, y + SIDEBAR_ROW_HEIGHT, ACCENT);
        }
        String text = trimToWidth(client, label, Math.max(20, rowRight - rowLeft - 20));
        graphics.text(client.font, text, left + 20, y + 9, selected ? ACCENT : TEXT, false);
    }

    private static void applyV14WidgetLayout(Screen screen, int width, int height) {
        WidgetBounds bounds = widgetBounds(screen);
        if (bounds.empty) return;

        Shell s = shell(width, height);
        int contentLeft = s.left + s.sidebarWidth + 20;
        int contentRight = s.right - 20;
        int contentTop = s.top + 122;
        int contentBottom = s.bottom - FOOTER_HEIGHT - 8;
        int availableWidth = Math.max(1, contentRight - contentLeft);
        int availableHeight = Math.max(1, contentBottom - contentTop);
        int groupWidth = bounds.right - bounds.left;
        int groupHeight = bounds.bottom - bounds.top;

        // Only translate groups that actually fit. Oversized list screens keep Minecraft's
        // layout and simply receive the v1.4 shell/chrome rather than being clipped.
        if (groupWidth > availableWidth || groupHeight > availableHeight) return;

        int targetLeft = contentLeft + (availableWidth - groupWidth) / 2;
        int targetTop = contentTop + Math.max(0, (availableHeight - groupHeight) / 2);
        int dx = targetLeft - bounds.left;
        int dy = targetTop - bounds.top;

        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible) continue;
            widget.setX(widget.getX() + dx);
            widget.setY(widget.getY() + dy);
        }
    }

    private static void drawPageBackground(GuiGraphicsExtractor graphics, int width, int height) {
        graphics.fill(0, 0, width, height, BG);
    }

    private static void renderForegroundChrome(Screen screen, GuiGraphicsExtractor graphics,
                                               int mouseX, int mouseY, float tickProgress) {
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (!widget.visible) continue;
            if (isUtilityIconButton(widget)) {
                renderUtilityIconFrame(graphics, widget, mouseX, mouseY);
                continue;
            }
            if (widget instanceof AbstractButton) {
                renderMazButton(graphics, widget, mouseX, mouseY);
                continue;
            }

            int left = widget.getX() - 1;
            int top = widget.getY() - 1;
            int right = widget.getX() + widget.getWidth() + 1;
            int bottom = widget.getY() + widget.getHeight() + 1;
            boolean hovered = widget.isMouseOver(mouseX, mouseY);
            int border = hovered ? ACCENT : BORDER;
            graphics.fill(left, top, right, top + 1, border);
            graphics.fill(left, bottom - 1, right, bottom, border);
            graphics.fill(left, top, left + 1, bottom, border);
            graphics.fill(right - 1, top, right, bottom, border);
            if (hovered && widget.getWidth() >= 40) graphics.fill(left, top, left + 2, bottom, ACCENT);
        }
    }

    private static boolean isUtilityIconButton(AbstractWidget widget) {
        return widget instanceof AbstractButton && widget.getWidth() <= 40 && widget.getHeight() <= 40;
    }

    private static void renderUtilityIconFrame(GuiGraphicsExtractor graphics, AbstractWidget widget,
                                               int mouseX, int mouseY) {
        int left = widget.getX() - 1;
        int top = widget.getY() - 1;
        int right = widget.getX() + widget.getWidth() + 1;
        int bottom = widget.getY() + widget.getHeight() + 1;
        int border = widget.active && widget.isMouseOver(mouseX, mouseY) ? ACCENT : BORDER;
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
        int fill = widget.active ? (hovered ? ACCENT : PANEL_2) : DISABLED;
        int text = widget.active ? (hovered ? 0xFFFFFFFF : TEXT) : MUTED;
        graphics.fill(left, top, right, bottom, fill);
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

    private static Shell shell(int width, int height) {
        int shellWidth = Math.min(MENU_WIDTH, Math.max(320, width - 24));
        int shellHeight = Math.min(MENU_HEIGHT, Math.max(220, height - 24));
        int left = (width - shellWidth) / 2;
        int top = (height - shellHeight) / 2;
        int sidebar = Math.min(SIDEBAR_WIDTH, Math.max(100, shellWidth / 4));
        return new Shell(left, top, left + shellWidth, top + shellHeight, sidebar);
    }

    private static String trimToWidth(Minecraft client, String value, int maxWidth) {
        if (client.font.width(value) <= maxWidth) return value;
        String ellipsis = "...";
        int end = value.length();
        while (end > 0 && client.font.width(value.substring(0, end) + ellipsis) > maxWidth) end--;
        return value.substring(0, end) + ellipsis;
    }

    private static final class WidgetBounds {
        private final int left, top, right, bottom;
        private final boolean empty;
        private WidgetBounds(int left, int top, int right, int bottom, boolean empty) {
            this.left = left; this.top = top; this.right = right; this.bottom = bottom; this.empty = empty;
        }
    }

    private static final class Shell {
        private final int left, top, right, bottom, sidebarWidth;
        private Shell(int left, int top, int right, int bottom, int sidebarWidth) {
            this.left = left; this.top = top; this.right = right; this.bottom = bottom; this.sidebarWidth = sidebarWidth;
        }
    }
}
