package com.maz.client;

import com.maz.client.gui.MazHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class MazClient implements ClientModInitializer {

    public static final String MOD_ID = "maz-client";

    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "maz_hud"),
                MazHud::render
        );

        System.out.println("Maz Client initialized!");
    }
}