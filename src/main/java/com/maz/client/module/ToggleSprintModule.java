package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class ToggleSprintModule extends Module {

    public ToggleSprintModule() {
        super("ToggleSprint", ModuleCategory.UTILITY);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.screen == null) {
            boolean movingForward = client.options.keyUp.isDown();
            client.options.keySprint.setDown(movingForward);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft.getInstance().options.keySprint.setDown(false);
    }
}
