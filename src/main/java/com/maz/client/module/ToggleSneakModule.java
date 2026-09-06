package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class ToggleSneakModule extends Module {

    public ToggleSneakModule() {
        super("ToggleSneak", ModuleCategory.UTILITY);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.options.keyShift.setDown(true);
        }
    }

    @Override
    protected void onDisable() {
        Minecraft.getInstance().options.keyShift.setDown(false);
    }
}
