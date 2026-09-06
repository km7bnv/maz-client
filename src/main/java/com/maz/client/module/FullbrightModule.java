package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class FullbrightModule extends Module {

    private Double previousGamma;

    public FullbrightModule() {
        super("Fullbright", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousGamma = client.options.gamma().get();
        client.options.gamma().set(1.0D);
    }

    @Override
    protected void onDisable() {
        if (previousGamma == null) return;
        Minecraft.getInstance().options.gamma().set(previousGamma);
        previousGamma = null;
    }
}
