package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class AutoMineModule extends Module {

    public AutoMineModule() {
        super("AutoMine", ModuleCategory.UTILITY);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.screen == null) {
            client.options.keyAttack.setDown(true);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft.getInstance().options.keyAttack.setDown(false);
    }
}
