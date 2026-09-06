package com.maz.client;

import com.maz.client.module.FpsModule;
import com.maz.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;

public class MazClient implements ClientModInitializer {

    public static final String MOD_ID = "maz-client";

    public static final ModuleManager MODULE_MANAGER = new ModuleManager();

    @Override
    public void onInitializeClient() {
        MODULE_MANAGER.register(new FpsModule());

        System.out.println("Maz Client initialized!");
        System.out.println("Loaded " + MODULE_MANAGER.getModules().size() + " module(s).");
    }
}