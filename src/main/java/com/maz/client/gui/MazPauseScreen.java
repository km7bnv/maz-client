package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class MazPauseScreen extends Screen {

    private static final int OVERLAY = 0xA8000000;
    private static final int PANEL = 0xFF111827;
    private static final int PANEL_HOVER = 0xFF1F2937;
    private static final int BORDER = 0xFF334155;
    private static final int TEXT = 0xFFF8FAFC;
    private static final int MUTED = 0xFF94A3B8;
    private static final int ACCENT = 0xFF5865F2;
    private static final int ACCENT_HOVER = 0xFF6875FF;
    private static final int DANGER = 0xFFB91C1C;
    private static final int DANGER_HOVER = 0xFFDC2626;
    private static final int SUCCESS = 0xFF22C55E;

    private static final int WIDTH = 460;
    private static final int HEIGHT = 326;
    private static final int BUTTON_HEIGHT = 38;
    private static final int GAP = 10;

    public MazPauseScreen() {
        super(Component.literal("MazClient Pause"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, this.width, this.height, OVERLAY);

        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;
        int right = left + WIDTH;
        int bottom = top + HEIGHT;

        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, BORDER);
        graphics.fill(left, top, right, bottom, PANEL);

        graphics.fill(left + 22, top + 20, left + 62, top + 60, ACCENT);
        graphics.centeredText(this.font, "M", left + 42, top + 34, 0xFFFFFFFF);
        graphics.text(this.font, "MazClient", left + 76, top + 23, TEXT, false);
        graphics.text(this.font, "Paused • v" + MazClient.getVersion(), left + 76, top + 42, MUTED, false);

        Minecraft client = Minecraft.getInstance();
        String playerName = client.player != null ? client.player.getName().getString() : "Player";
        String location = client.hasSingleplayerServer() ? "Singleplayer world" : "Multiplayer server";

        int totalModules = MazClient.MODULE_MANAGER.getModules().size();
        int enabledModules = 0;
        for (Module module : MazClient.MODULE_MANAGER.getModules()) {
            if (module.isEnabled()) enabledModules++;
        }

        long seconds = Math.max(0L, (System.currentTimeMillis() - MazClient.SESSION_START_MILLIS) / 1000L);
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        String session = hours > 0
                ? String.format("%dh %02dm", hours, minutes % 60L)
                : String.format("%dm %02ds", minutes, seconds % 60L);

        graphics.text(this.font, playerName, left + 22, top + 78, TEXT, false);
        graphics.text(this.font, location, left + 22, top + 95, MUTED, false);
        graphics.text(this.font, "Session " + session, right - 132, top + 78, MUTED, false);
        graphics.text(this.font, enabledModules + "/" + totalModules + " modules active", right - 132, top + 95,
                enabledModules > 0 ? SUCCESS : MUTED, false);

        int buttonLeft = left + 22;
        int buttonRight = right - 22;
        int y = top + 121;

        drawButton(graphics, mouseX, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT, "Resume Game", true, false);
        y += BUTTON_HEIGHT + GAP;

        int half = (buttonRight - buttonLeft - GAP) / 2;
        int middle = buttonLeft + half;
        drawButton(graphics, mouseX, mouseY, buttonLeft, y, middle, y + BUTTON_HEIGHT, "Client Settings", false, false);
        drawButton(graphics, mouseX, mouseY, middle + GAP, y, buttonRight, y + BUTTON_HEIGHT, "HUD Editor", false, false);

        y += BUTTON_HEIGHT + GAP;
        drawButton(graphics, mouseX, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT, "Open Modules", false, false);

        y += BUTTON_HEIGHT + GAP;
        drawButton(graphics, mouseX, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT, "Return to MazClient Home", false, true);

        graphics.text(this.font, "ESC resumes • Right Shift opens modules", left + 22, bottom - 20, MUTED, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                            int left, int top, int right, int bottom,
                            String label, boolean primary, boolean danger) {
        boolean hovered = inside(mouseX, mouseY, left, top, right, bottom);
        int color;
        if (danger) {
            color = hovered ? DANGER_HOVER : DANGER;
        } else if (primary) {
            color = hovered ? ACCENT_HOVER : ACCENT;
        } else {
            color = hovered ? PANEL_HOVER : 0xFF182235;
        }

        graphics.fill(left, top, right, bottom, color);
        if (!primary && !danger) {
            graphics.fill(left, top, right, top + 1, BORDER);
            graphics.fill(left, bottom - 1, right, bottom, BORDER);
            graphics.fill(left, top, left + 1, bottom, BORDER);
            graphics.fill(right - 1, top, right, bottom, BORDER);
        }
        graphics.centeredText(this.font, label, (left + right) / 2, top + 14, TEXT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return super.mouseClicked(event, doubleClick);

        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;
        int right = left + WIDTH;
        int buttonLeft = left + 22;
        int buttonRight = right - 22;
        int y = top + 121;
        double x = event.x();
        double mouseY = event.y();

        Minecraft client = Minecraft.getInstance();

        if (inside(x, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT)) {
            client.gui.setScreen(null);
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

        y += BUTTON_HEIGHT + GAP;
        if (inside(x, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT)) {
            client.gui.setScreen(new MazMenuScreen());
            return true;
        }

        y += BUTTON_HEIGHT + GAP;
        if (inside(x, mouseY, buttonLeft, y, buttonRight, y + BUTTON_HEIGHT)) {
            if (client.hasSingleplayerServer()) {
                client.disconnectWithSavingScreen();
            } else {
                client.disconnectWithProgressScreen();
            }
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    private static boolean inside(double x, double y, int left, int top, int right, int bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().gui.setScreen(null);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
