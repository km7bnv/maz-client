package com.maz.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.DeltaTracker;

public class MazHud {

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        graphics.drawString(
                mc.font,
                "MAZ CLIENT",
                8,
                8,
                0xFFFFFF
        );

        graphics.drawString(
                mc.font,
                "FPS: " + mc.getFps(),
                8,
                20,
                0xFFFFFF
        );
    }
}