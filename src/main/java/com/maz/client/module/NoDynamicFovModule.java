package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class NoDynamicFovModule extends Module {

    private Double previousScale;

    public NoDynamicFovModule() {
        super("NoDynamicFOV", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousScale = client.options.fovEffectScale().get();
        client.options.fovEffectScale().set(0.0D);
    }

    @Override
    protected void onDisable() {
        if (previousScale == null) return;
        Minecraft.getInstance().options.fovEffectScale().set(previousScale);
        previousScale = null;
    }
}
