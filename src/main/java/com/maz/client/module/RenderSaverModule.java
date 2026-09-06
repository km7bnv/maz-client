package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class RenderSaverModule extends Module {

    private Integer previousRenderDistance;

    public RenderSaverModule() {
        super("Render Saver", ModuleCategory.PERFORMANCE);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();

        int current = client.options.renderDistance().get();
        previousRenderDistance = current;

        if (current > 8) {
            client.options.renderDistance().set(8);
        }
    }

    @Override
    protected void onDisable() {
        if (previousRenderDistance == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        client.options.renderDistance().set(previousRenderDistance);
        previousRenderDistance = null;
    }
}
