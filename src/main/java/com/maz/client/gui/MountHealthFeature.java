package com.maz.client.gui;

import com.maz.client.MazClient;
import com.maz.client.module.ModuleCategory;
import com.maz.client.module.SimpleModule;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public final class MountHealthFeature implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MazClient.MODULE_MANAGER.register(new SimpleModule("Mount Health", ModuleCategory.HUD));
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MazClient.MOD_ID, "mount_health_hud"),
                MountHealthHud::render
        );
    }
}
