package com.maz.client;

import net.fabricmc.api.ClientModInitializer;

public class MazClient implements ClientModInitializer {

    public static final String NAME = "Maz Client";
    public static final String VERSION = "1.0.0";

    @Override
    public void onInitializeClient() {
        System.out.println("================================");
        System.out.println("       " + NAME);
        System.out.println("       Version " + VERSION);
        System.out.println("================================");
        System.out.println("Maz Client initialized!");
    }
}