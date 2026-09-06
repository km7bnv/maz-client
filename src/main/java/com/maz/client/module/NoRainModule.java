package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class NoRainModule extends Module {

    public NoRainModule() {
        super("NoRain", ModuleCategory.VISUAL);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            client.level.setRainLevel(0.0F);
            client.level.setThunderLevel(0.0F);
        }
    }
}
