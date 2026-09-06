package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class NoHurtCamModule extends Module {

    private Double previousStrength;

    public NoHurtCamModule() {
        super("NoHurtCam", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousStrength = client.options.damageTiltStrength().get();
        client.options.damageTiltStrength().set(0.0D);
    }

    @Override
    protected void onDisable() {
        if (previousStrength == null) {
            return;
        }
        Minecraft.getInstance().options.damageTiltStrength().set(previousStrength);
        previousStrength = null;
    }
}
