package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;
import com.maz.client.module.ModuleCategory;

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
    private static final int SUCCESS = 0xFF16A34A;

    private static final int MENU_WIDTH = 500;
    private static final int MENU_HEIGHT = 300;

    private static final int SIDEBAR_WIDTH = 125;
    private static final int ROW_HEIGHT = 34;

    private ModuleCategory selectedCategory =
            ModuleCategory.PERFORMANCE;

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
        int top = (this.height - MENU_HEIGHT) / 2;

        int right = left + MENU_WIDTH;
        int bottom = top + MENU_HEIGHT;

        // Outer border
        graphics.fill(
                left - 1,
                top - 1,
                right + 1,
                bottom + 1,
                BORDER
        );

        // Main panel
        graphics.fill(
                left,
                top,
                right,
                bottom,
                PANEL
        );

        // ==============================
        // HEADER
        // ==============================

        graphics.fill(
                left + 16,
                top + 16,
                left + 52,
                top + 52,
                ACCENT
        );

        graphics.text(
                this.font,
                "M",
                left + 30,
                top + 30,
                0xFFFFFFFF,
                true
        );

        graphics.text(
                this.font,
                "Maz Client",
                left + 64,
                top + 20,
                TEXT,
                false
        );

        graphics.text(
                this.font,
                "Performance & client settings",
                left + 64,
                top + 37,
                MUTED,
                false
        );

        // Header divider
        graphics.fill(
                left,
                top + 68,
                right,
                top + 69,
                BORDER
        );

        // ==============================
        // SIDEBAR
        // ==============================

        int sidebarRight = left + SIDEBAR_WIDTH;

        graphics.fill(
                sidebarRight,
                top + 69,
                sidebarRight + 1,
                bottom,
                BORDER
        );

        int categoryY = top + 82;

        for (ModuleCategory category : ModuleCategory.values()) {

            boolean selected =
                    category == selectedCategory;

            boolean hovered =
                    mouseX >= left + 10 &&
                    mouseX <= sidebarRight - 10 &&
                    mouseY >= categoryY &&
                    mouseY <= categoryY + 26;

            if (selected) {
                graphics.fill(
                        left + 10,
                        categoryY,
                        sidebarRight - 10,
                        categoryY + 26,
                        PANEL_2
                );

                // Accent bar
                graphics.fill(
                        left + 10,
                        categoryY,
                        left + 13,
                        categoryY + 26,
                        ACCENT
                );
            } else if (hovered) {
                graphics.fill(
                        left + 10,
                        categoryY,
                        sidebarRight - 10,
                        categoryY + 26,
                        PANEL_2
                );
            }

            graphics.text(
                    this.font,
                    category.getDisplayName(),
                    left + 20,
                    categoryY + 9,
                    selected ? ACCENT : TEXT,
                    false
            );

            categoryY += 34;
        }

        // ==============================
        // MODULE AREA
        // ==============================

        int contentLeft = sidebarRight + 20;

        graphics.text(
                this.font,
                selectedCategory.getDisplayName(),
                contentLeft,
                top + 86,
                TEXT,
                false
        );

        graphics.text(
                this.font,
                "Modules",
                contentLeft,
                top + 103,
                MUTED,
                false
        );

        int moduleY = top + 126;

        boolean foundModule = false;

        for (Module module :
                MazClient.MODULE_MANAGER.getModules()) {

            if (module.getCategory() != selectedCategory) {
                continue;
            }

            foundModule = true;

            boolean hovered =
                    mouseX >= contentLeft &&
                    mouseX <= right - 20 &&
                    mouseY >= moduleY &&
                    mouseY <= moduleY + 30;

            graphics.fill(
                    contentLeft,
                    moduleY,
                    right - 20,
                    moduleY + 30,
                    hovered ? PANEL_2 : PANEL
            );

            graphics.text(
                    this.font,
                    module.getName(),
                    contentLeft + 10,
                    moduleY + 11,
                    TEXT,
                    false
            );

            // Toggle
            int toggleLeft = right - 68;
            int toggleRight = right - 30;

            graphics.fill(
                    toggleLeft,
                    moduleY + 7,
                    toggleRight,
                    moduleY + 23,
                    module.isEnabled()
                            ? SUCCESS
                            : PANEL_2
            );

            int knobX =
                    module.isEnabled()
                            ? toggleRight - 14
                            : toggleLeft + 2;

            graphics.fill(
                    knobX,
                    moduleY + 9,
                    knobX + 12,
                    moduleY + 21,
                    PANEL
            );

            moduleY += ROW_HEIGHT;
        }

        if (!foundModule) {
            graphics.text(
                    this.font,
                    "No modules yet.",
                    contentLeft,
                    moduleY,
                    MUTED,
                    false
            );
        }

        // Footer
        graphics.text(
                this.font,
                "Maz Client 1.0",
                left + 16,
                bottom - 20,
                MUTED,
                false
        );

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );
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
        int top = (this.height - MENU_HEIGHT) / 2;

        int right = left + MENU_WIDTH;
        int sidebarRight = left + SIDEBAR_WIDTH;

        // ==============================
        // CATEGORY CLICKS
        // ==============================

        int categoryY = top + 82;

        for (ModuleCategory category :
                ModuleCategory.values()) {

            if (
                    event.x() >= left + 10 &&
                    event.x() <= sidebarRight - 10 &&
                    event.y() >= categoryY &&
                    event.y() <= categoryY + 26
            ) {
                selectedCategory = category;
                return true;
            }

            categoryY += 34;
        }

        // ==============================
        // MODULE CLICKS
        // ==============================

        int contentLeft = sidebarRight + 20;
        int moduleY = top + 126;

        for (Module module :
                MazClient.MODULE_MANAGER.getModules()) {

            if (module.getCategory() != selectedCategory) {
                continue;
            }

            if (
                    event.x() >= contentLeft &&
                    event.x() <= right - 20 &&
                    event.y() >= moduleY &&
                    event.y() <= moduleY + 30
            ) {
                module.toggle();
                return true;
            }

            moduleY += ROW_HEIGHT;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}