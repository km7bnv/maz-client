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
                // Match the proven 1.6.26 startup behavior: wait for vanilla title setup,
                // then install the legacy Maz home exactly once for this process.
                titleScreenStableTicks++;
                if (titleScreenStableTicks >= STARTUP_TITLE_STABLE_TICKS) {
                    titleScreenStableTicks = 0;
                    homeShownOnce = true;
                    client.gui.setScreen(new MazHomeScreen());
                }
            } else {
                // Once a world/server has existed, Minecraft owns all title/disconnect
                // transitions. This prevents the old panorama-only disconnect softlock.
                titleScreenStableTicks = 0;
            }

            if (client.gui.screen() instanceof MazHomeScreen) {
                homeShownOnce = true;
            }

            // Restore the 1.6.26 pause layout only while gameplay is unquestionably active.
            // If saving/disconnect has begun, the guard fails and Minecraft keeps control.
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
