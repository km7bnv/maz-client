package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;
import com.maz.client.module.ModuleCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudEditorScreen extends Screen {

    private static final int BG = 0xFF0F172A;
    private static final int GRID = 0xFF1E293B;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int MUTED = 0xFF94A3B8;
    private static final int ACCENT = 0xFF5865F2;
    private static final int SELECT = 0x665865F2;

    private String draggingModule;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditorScreen() {
        super(Component.literal("Maz HUD Editor"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, BG);

        for (int x = 0; x < this.width; x += 20) {
            graphics.fill(x, 0, x + 1, this.height, GRID);
        }
        for (int y = 0; y < this.height; y += 20) {
            graphics.fill(0, y, this.width, y + 1, GRID);
        }

        graphics.fill(0, 0, this.width, 38, 0xEE020617);
        graphics.text(this.font, "Maz HUD Editor", 10, 9, TEXT, false);
        graphics.text(this.font, "Drag enabled HUD modules anywhere • ESC to save & exit", 10, 24, MUTED, false);

        int resetLeft = this.width - 70;
        graphics.fill(resetLeft, 8, this.width - 10, 30, ACCENT);
        graphics.centeredText(this.font, "Reset", resetLeft + 30, 15, TEXT);

        Minecraft client = Minecraft.getInstance();
        int defaultY = 48;

        for (Module module : MazClient.MODULE_MANAGER.getModules()) {
            if (module.getCategory() != ModuleCategory.HUD || !module.isEnabled()) {
                continue;
            }

            HudLayout.Position p = HudLayout.getPosition(module.getName(), 8, defaultY);
            int w = MazHud.previewWidth(client, module.getName());
            int h = MazHud.previewHeight(module.getName());

            if (module.getName().equals(draggingModule)) {
                graphics.fill(p.x() - 2, p.y() - 2, p.x() + w + 2, p.y() + h + 2, SELECT);
            } else if (mouseX >= p.x() && mouseX <= p.x() + w && mouseY >= p.y() && mouseY <= p.y() + h) {
                graphics.fill(p.x() - 1, p.y() - 1, p.x() + w + 1, p.y() + h + 1, SELECT);
            }

            MazHud.drawPreview(graphics, client, module.getName(), p.x(), p.y());
            defaultY += 22;
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        int resetLeft = this.width - 70;
        if (event.x() >= resetLeft && event.x() <= this.width - 10
                && event.y() >= 8 && event.y() <= 30) {
            HudLayout.reset();
            return true;
        }

        Minecraft client = Minecraft.getInstance();
        int defaultY = 48;

        for (Module module : MazClient.MODULE_MANAGER.getModules()) {
            if (module.getCategory() != ModuleCategory.HUD || !module.isEnabled()) {
                continue;
            }

            HudLayout.Position p = HudLayout.getPosition(module.getName(), 8, defaultY);
            int w = MazHud.previewWidth(client, module.getName());
            int h = MazHud.previewHeight(module.getName());

            if (event.x() >= p.x() && event.x() <= p.x() + w
                    && event.y() >= p.y() && event.y() <= p.y() + h) {
                draggingModule = module.getName();
                dragOffsetX = (int) event.x() - p.x();
                dragOffsetY = (int) event.y() - p.y();
                return true;
            }

            defaultY += 22;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == 0 && draggingModule != null) {
            Minecraft client = Minecraft.getInstance();
            int w = MazHud.previewWidth(client, draggingModule);
            int h = MazHud.previewHeight(draggingModule);

            int x = (int) event.x() - dragOffsetX;
            int y = (int) event.y() - dragOffsetY;

            x = Math.max(0, Math.min(this.width - w, x));
            y = Math.max(40, Math.min(this.height - h, y));

            HudLayout.setPosition(draggingModule, x, y);
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && draggingModule != null) {
            draggingModule = null;
            HudLayout.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        HudLayout.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
