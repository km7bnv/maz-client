package com.maz.client.gui;

import com.maz.client.MazClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MazHomeScreen extends Screen {

    private static final int BG = 0xFF090E1A;
    private static final int BG_TOP = 0xFF11192A;
    private static final int PANEL = 0xFF141E31;
    private static final int PANEL_HOVER = 0xFF1C2942;
    private static final int BORDER = 0xFF2A3958;
    private static final int TEXT = 0xFFF8FAFC;
    private static final int MUTED = 0xFF94A3B8;
    private static final int ACCENT = 0xFF5865F2;
    private static final int ACCENT_HOVER = 0xFF6875FF;
    private static final int DANGER = 0xFFEF4444;

    private static final int CARD_WIDTH = 540;
    private static final int CARD_HEIGHT = 334;
    private static final int BUTTON_HEIGHT = 42;
    private static final int GAP = 10;

    public MazHomeScreen() {
        super(Component.literal("MazClient Home"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, BG);
        graphics.fill(0, 0, this.width, Math.max(120, this.height / 3), BG_TOP);

        int left = (this.width - CARD_WIDTH) / 2;
        int top = (this.height - CARD_HEIGHT) / 2;
        int right = left + CARD_WIDTH;
        int bottom = top + CARD_HEIGHT;

        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, BORDER);
        graphics.fill(left, top, right, bottom, PANEL);

        graphics.fill(left + 24, top + 24, left + 72, top + 72, ACCENT);
        graphics.centeredText(this.font, "M", left + 48, top + 42, 0xFFFFFFFF);
        graphics.text(this.font, "MazClient", left + 88, top + 28, TEXT, false);
        graphics.text(this.font, "Version " + MazClient.getVersion(), left + 88, top + 48, MUTED, false);

        graphics.text(this.font, "Your Minecraft, your setup.", left + 24, top + 88, TEXT, false);
        graphics.text(this.font, "Performance, HUD tools and client controls in one place.", left + 24, top + 105, MUTED, false);

        int buttonLeft = left + 24;
        int buttonRight = right - 24;
        int y = top + 132;

        drawButton(graphics, mouseX, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT, "Singleplayer", true);
        y += BUTTON_HEIGHT + GAP;
        drawButton(graphics, mouseX, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT, "Multiplayer", false);
        y += BUTTON_HEIGHT + GAP;

        int half = (buttonRight - buttonLeft - GAP) / 2;
        int middle = buttonLeft + half;
        drawButton(graphics, mouseX, mouseY, buttonLeft, y, middle, y + BUTTON_HEIGHT, "Client Settings", false);
        drawButton(graphics, mouseX, mouseY, middle + GAP, y, buttonRight, y + BUTTON_HEIGHT, "HUD Editor", false);

        graphics.fill(left + 24, bottom - 43, right - 24, bottom - 42, BORDER);
        graphics.text(this.font, "MazClient " + MazClient.getVersion(), left + 24, bottom - 27, MUTED, false);

        boolean quitHover = inside(mouseX, mouseY, right - 76, bottom - 36, right - 24, bottom - 12);
        graphics.text(this.font, "Quit", right - 52, bottom - 27, quitHover ? 0xFFFF6B6B : DANGER, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                            int left, int top, int right, int bottom,
                            String label, boolean primary) {
        boolean hovered = inside(mouseX, mouseY, left, top, right, bottom);
        int color = primary
                ? (hovered ? ACCENT_HOVER : ACCENT)
                : (hovered ? PANEL_HOVER : BG_TOP);

        graphics.fill(left, top, right, bottom, color);
        if (!primary) {
            graphics.fill(left, top, right, top + 1, BORDER);
            graphics.fill(left, bottom - 1, right, bottom, BORDER);
            graphics.fill(left, top, left + 1, bottom, BORDER);
            graphics.fill(right - 1, top, right, bottom, BORDER);
        }
        graphics.centeredText(this.font, label, (left + right) / 2, top + 16, TEXT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        int left = (this.width - CARD_WIDTH) / 2;
        int top = (this.height - CARD_HEIGHT) / 2;
        int right = left + CARD_WIDTH;
        int bottom = top + CARD_HEIGHT;
        int buttonLeft = left + 24;
        int buttonRight = right - 24;
        int y = top + 132;
        double x = event.x();
        double mouseY = event.y();

        Minecraft client = Minecraft.getInstance();

        if (inside(x, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT)) {
            client.gui.setScreen(new SelectWorldScreen(this));
            return true;
        }

        y += BUTTON_HEIGHT + GAP;
        if (inside(x, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT)) {
            client.gui.setScreen(new JoinMultiplayerScreen(this));
            return true;
        }

        y += BUTTON_HEIGHT + GAP;
        int half = (buttonRight - buttonLeft - GAP) / 2;
        int middle = buttonLeft + half;

        if (inside(x, mouseY, buttonLeft, y, middle, y + BUTTON_HEIGHT)) {
            client.gui.setScreen(new MazMenuScreen());
            return true;
        }

        if (inside(x, mouseY, middle + GAP, y, buttonRight, y + BUTTON_HEIGHT)) {
            client.gui.setScreen(new HudEditorScreen());
            return true;
        }

        if (inside(x, mouseY, right - 76, bottom - 36, right - 24, bottom - 12)) {
            client.stop();
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    private static boolean inside(double x, double y, int left, int top, int right, int bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
