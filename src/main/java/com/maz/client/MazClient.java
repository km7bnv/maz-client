package com.maz.client;

import com.maz.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;

public class MazClient implements ClientModInitializer {

    public static final String MOD_ID = "maz-client";

    public static final ModuleManager MODULE_MANAGER = new ModuleManager();

    @Override
    public void onInitializeClient() {
        System.out.println("Maz Client initialized!");
    }
}