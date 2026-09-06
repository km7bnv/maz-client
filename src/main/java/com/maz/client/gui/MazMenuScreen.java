package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MazMenuScreen extends Screen {

    // Light palette
    private static final int BG = 0xFFF1F5F9;
    private static final int PANEL = 0xFFFFFFFF;
    private static final int PANEL_2 = 0xFFE2E8F0;
    private static final int BORDER = 0xFFCBD5E1;

    private static final int TEXT = 0xFF0F172A;
    private static final int MUTED = 0xFF475569;

    private static final int ACCENT = 0xFF5865F2;
    private static final int SUCCESS = 0xFF16A34A;

    private static final int MENU_WIDTH = 340;
    private static final int ROW_HEIGHT = 34;

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
        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                BG
        );

        int left = (this.width - MENU_WIDTH) / 2;
        int top = 30;
        int right = left + MENU_WIDTH;

        int moduleCount = MazClient.MODULE_MANAGER.getModules().size();
        int bottom = 120 + (moduleCount * ROW_HEIGHT);

        // Card border
        graphics.fill(
                left - 1,
                top - 1,
                right + 1,
                bottom + 1,
                BORDER
        );

        // Main card
        graphics.fill(
                left,
                top,
                right,
                bottom,
                PANEL
        );

        // Logo
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
                "Performance & client settings",
                left + 72,
                top + 40,
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

        graphics.text(
                this.font,
                "MODULES",
                left + 20,
                top + 84,
                MUTED,
                false
        );

        int y = top + 100;

        for (Module module : MazClient.MODULE_MANAGER.getModules()) {

            boolean hovered =
                    mouseX >= left + 18 &&
                    mouseX <= right - 18 &&
                    mouseY >= y &&
                    mouseY <= y + 26;

            graphics.fill(
                    left + 18,
                    y,
                    right - 18,
                    y + 26,
                    hovered ? PANEL_2 : PANEL
            );

            graphics.text(
                    this.font,
                    module.getName(),
                    left + 28,
                    y + 9,
                    TEXT,
                    false
            );

            // Toggle pill
            int toggleLeft = right - 65;
            int toggleRight = right - 28;

            graphics.fill(
                    toggleLeft,
                    y + 5,
                    toggleRight,
                    y + 21,
                    module.isEnabled() ? SUCCESS : PANEL_2
            );

            // Toggle knob
            int knobX = module.isEnabled()
                    ? toggleRight - 14
                    : toggleLeft + 2;

            graphics.fill(
                    knobX,
                    y + 7,
                    knobX + 12,
                    y + 19,
                    PANEL
            );

            y += ROW_HEIGHT;
        }

        graphics.text(
                this.font,
                "Right Shift • Open menu",
                left + 18,
                bottom - 17,
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
        int top = 30;
        int right = left + MENU_WIDTH;

        int y = top + 100;

        for (Module module : MazClient.MODULE_MANAGER.getModules()) {

            if (
                    event.x() >= left + 18 &&
                    event.x() <= right - 18 &&
                    event.y() >= y &&
                    event.y() <= y + 26
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