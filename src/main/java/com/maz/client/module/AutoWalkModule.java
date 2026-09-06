package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class AutoWalkModule extends Module {

    public AutoWalkModule() {
        super("AutoWalk", ModuleCategory.UTILITY);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.options.keyUp.setDown(true);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft.getInstance().options.keyUp.setDown(false);
    }
}
