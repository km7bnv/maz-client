package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class SmoothCameraModule extends Module {

    private Boolean previous;

    public SmoothCameraModule() {
        super("Smooth Camera", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previous = client.options.smoothCamera;
        client.options.smoothCamera = true;
    }

    @Override
    protected void onDisable() {
        if (previous != null) {
            Minecraft.getInstance().options.smoothCamera = previous;
            previous = null;
        }
    }
}
