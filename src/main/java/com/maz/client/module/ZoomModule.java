package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class ZoomModule extends Module {

    private Integer previousFov;

    public ZoomModule() {
        super("Zoom", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousFov = client.options.fov().get();
        client.options.fov().set(30);
    }

    @Override
    protected void onDisable() {
        if (previousFov == null) {
            return;
        }
        Minecraft.getInstance().options.fov().set(previousFov);
        previousFov = null;
    }
}
