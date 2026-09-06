package com.maz.client.module;

public class FpsModule extends Module {

    public FpsModule() {
        super("FPS", ModuleCategory.PERFORMANCE);
    }

    @Override
    protected void onEnable() {
        System.out.println("[Maz] FPS module enabled!");
    }

    @Override
    protected void onDisable() {
        System.out.println("[Maz] FPS module disabled!");
    }
}