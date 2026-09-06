package com.maz.client;

import com.maz.client.gui.MazHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class MazClient implements ClientModInitializer {

    public static final String NAME = "Maz Client";
    public static final String VERSION = "1.0.0";

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(MazHud::render);

        System.out.println("================================");
        System.out.println("       " + NAME);
        System.out.println("       Version " + VERSION);
        System.out.println("================================");
        System.out.println("Maz Client initialized!");
    }
}