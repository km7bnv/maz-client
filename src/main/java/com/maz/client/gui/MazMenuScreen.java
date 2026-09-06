package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MazMenuScreen extends Screen {

    // VoiceOver light palette
    private static final int BG = 0xFFF1F5F9;
    private static final int PANEL = 0xFFFFFFFF;
    private static final int PANEL_2 = 0xFFE2E8F0;
    private static final int BORDER = 0xFFCBD5E1;

    private static final int TEXT = 0xFF0F172A;
    private static final int MUTED = 0xFF475569;

    private static final int ACCENT = 0xFF5865F2;
    private static final int ACCENT_HOVER = 0xFF4752C4;

    private static final int SUCCESS = 0xFF16A34A;
    private static final int DANGER = 0xFFDC2626;

    private static final int MENU_WIDTH = 300;
    private static final int ROW_HEIGHT = 32;

    public MazMenuScreen() {
        super(Component.literal("Maz Client"));
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // Full light background
        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                BG
        );

        int left = (this.width - MENU_WIDTH) / 2;
        int top = 35;
        int right = left + MENU_WIDTH;

        int moduleCount = MazClient.MODULE_MANAGER.getModules().size();
        int bottom = 105 + moduleCount * ROW_HEIGHT;

        // Main white card border
        graphics.fill(
                left - 1,
                top - 1,
                right + 1,
                bottom + 1,
                BORDER
        );

        // Main white card
        graphics.fill(
                left,
                top,
                right,
                bottom,
                PANEL
        );

        // Logo block
        graphics.fill(
                left + 18,
                top + 18,
                left + 58,
                top + 58,
                ACCENT
        );

        graphics.text(
                this.font,
                "M",
                left + 34,
                top + 34,
                0xFFFFFFFF,
                true
        );

        // Title
        graphics.text(
                this.font,
                "Maz Client",
                left + 72,
                top + 22,
                TEXT,
                false
        );

        graphics.text(
                this.font,
                "Client modules",
                left + 72,
                top + 39,
                MUTED,
                false
        );

        // Divider
        graphics.fill(
                left + 18,
                top + 72,
                right - 18,
                top + 73,
                BORDER
        );

        int y = top + 84;

        for (Module module : MazClient.MODULE_MANAGER.getModules()) {

            boolean hovered =
                    mouseX >= left + 18 &&
                    mouseX <= right - 18 &&
                    mouseY >= y &&
                    mouseY <= y + 24;

            int rowColor = hovered ? PANEL_2 : PANEL;

            graphics.fill(
                    left + 18,
                    y,
                    right - 18,
                    y + 24,
                    rowColor
            );

            // tiny left accent on hover
            if (hovered) {
                graphics.fill(
                        left + 18,
                        y,
                        left + 21,
                        y + 24,
                        ACCENT_HOVER
                );
            }

            graphics.text(
                    this.font,
                    module.getName(),
                    left + 28,
                    y + 8,
                    TEXT,
                    false
            );

            String state = module.isEnabled() ? "ON" : "OFF";
            int stateColor = module.isEnabled() ? SUCCESS : MUTED;

            graphics.text(
                    this.font,
                    state,
                    right - 48,
                    y + 8,
                    stateColor,
                    false
            );

            y += ROW_HEIGHT;
        }

        graphics.text(
                this.font,
                "Right Shift to open • ESC to close",
                left + 18,
                bottom - 16,
                MUTED,
                false
        );

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        int left = (this.width - MENU_WIDTH) / 2;
        int top = 35;
        int right = left + MENU_WIDTH;

        int y = top + 84;

        for (Module module : MazClient.MODULE_MANAGER.getModules()) {

            if (
                    event.x() >= left + 18 &&
                    event.x() <= right - 18 &&
                    event.y() >= y &&
                    event.y() <= y + 24
            ) {
                module.toggle();
                return true;
            }

            y += ROW_HEIGHT;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}