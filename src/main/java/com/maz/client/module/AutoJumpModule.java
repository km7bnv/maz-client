package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class AutoJumpModule extends Module {

    public AutoJumpModule() {
        super("AutoJump", ModuleCategory.UTILITY);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.screen == null) {
            client.options.keyJump.setDown(true);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft.getInstance().options.keyJump.setDown(false);
    }
}
