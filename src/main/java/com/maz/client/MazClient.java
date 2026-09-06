package com.maz.client;

import com.maz.client.gui.MazHud;
import com.maz.client.gui.MazMenuScreen;
import com.maz.client.module.ClockModule;
import com.maz.client.module.CoordinatesModule;
import com.maz.client.module.CpsModule;
import com.maz.client.module.DirectionModule;
import com.maz.client.module.FpsBoosterModule;
import com.maz.client.module.FpsModule;
import com.maz.client.module.KeystrokesModule;
import com.maz.client.module.MemoryModule;
import com.maz.client.module.ModuleManager;
import com.maz.client.module.PingModule;
import com.maz.client.module.RenderSaverModule;
import com.maz.client.module.SpeedModule;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public class MazClient implements ClientModInitializer {

    public static final String MOD_ID = "maz-client";

    public static final ModuleManager MODULE_MANAGER = new ModuleManager();

    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {

        MODULE_MANAGER.register(new FpsModule());
        MODULE_MANAGER.register(new FpsBoosterModule());
        MODULE_MANAGER.register(new RenderSaverModule());
        MODULE_MANAGER.register(new MemoryModule());
        MODULE_MANAGER.register(new CoordinatesModule());
        MODULE_MANAGER.register(new PingModule());
        MODULE_MANAGER.register(new SpeedModule());
        MODULE_MANAGER.register(new DirectionModule());
        MODULE_MANAGER.register(new ClockModule());
        MODULE_MANAGER.register(new CpsModule());
        MODULE_MANAGER.register(new KeystrokesModule());

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(
                        MOD_ID,
                        "maz_hud"
                ),
                MazHud::render
        );

        KeyMapping.Category category =
                KeyMapping.Category.register(
                        Identifier.fromNamespaceAndPath(
                                MOD_ID,
                                "maz_client"
                        )
                );

        openMenuKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.maz-client.open_menu",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        category
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (openMenuKey.consumeClick()) {
                client.gui.setScreen(new MazMenuScreen());
            }

            MODULE_MANAGER.tick();
        });

        System.out.println("Maz Client initialized!");
    }
}
