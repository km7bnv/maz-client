package com.maz.client.gui;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Restores the MazClient 1.6.26 home and pause menu layouts while keeping the
 * lifecycle guards that prevent title-screen replacement during disconnect.
 */
public final class LegacyMenuLifecycle implements ClientModInitializer {
    private static final int STARTUP_TITLE_STABLE_TICKS = 4;

    private static boolean replacePauseScreen;
    private static boolean wasInWorld;
    private static boolean homeShownOnce;
    private static int titleScreenStableTicks;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen && worldActive(client)) {
                replacePauseScreen = true;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean worldActive = worldActive(client);

            if (worldActive) {
                wasInWorld = true;
                titleScreenStableTicks = 0;
            } else if (!homeShownOnce && !wasInWorld && client.gui.screen() instanceof TitleScreen) {
                titleScreenStableTicks++;
                if (titleScreenStableTicks >= STARTUP_TITLE_STABLE_TICKS) {
                    titleScreenStableTicks = 0;
                    homeShownOnce = true;
                    client.gui.setScreen(new MazHomeScreen());
                }
            } else {
                titleScreenStableTicks = 0;
            }

            if (client.gui.screen() instanceof MazHomeScreen) {
                homeShownOnce = true;
            }

            if (replacePauseScreen && client.gui.screen() instanceof PauseScreen && worldActive) {
                replacePauseScreen = false;
                client.gui.setScreen(new MazPauseScreen());
            } else if (!(client.gui.screen() instanceof PauseScreen)) {
                replacePauseScreen = false;
            }
        });
    }

    private static boolean worldActive(net.minecraft.client.Minecraft client) {
        return client.player != null
                || client.level != null
                || client.getConnection() != null
                || client.hasSingleplayerServer();
    }
}
