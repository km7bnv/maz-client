package com.maz.client;

import com.maz.client.config.ClientConfig;
import com.maz.client.gui.MazHomeScreen;
import com.maz.client.gui.MazHud;
import com.maz.client.gui.MazMenuScreen;
import com.maz.client.gui.MazPauseScreen;
import com.maz.client.module.AdvancedTooltipsModule;
import com.maz.client.module.ClearChatModule;
import com.maz.client.module.ClockModule;
import com.maz.client.module.CoordinatesModule;
import com.maz.client.module.CpsModule;
import com.maz.client.module.DirectionModule;
import com.maz.client.module.FpsBoosterModule;
import com.maz.client.module.FpsModule;
import com.maz.client.module.FullbrightModule;
import com.maz.client.module.KeystrokesModule;
import com.maz.client.module.MemoryModule;
import com.maz.client.module.ModuleCategory;
import com.maz.client.module.ModuleHotkeys;
import com.maz.client.module.ModuleManager;
import com.maz.client.module.NoDynamicFovModule;
import com.maz.client.module.NoHurtCamModule;
import com.maz.client.module.NoRainModule;
import com.maz.client.module.PingModule;
import com.maz.client.module.PotCounterModule;
import com.maz.client.module.SessionTimerModule;
import com.maz.client.module.SimpleModule;
import com.maz.client.module.SmoothCameraModule;
import com.maz.client.module.SpeedModule;
import com.maz.client.module.ToggleSneakModule;
import com.maz.client.module.ToggleSprintModule;
import com.maz.client.module.ZoomModule;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public class MazClient implements ClientModInitializer {

    public static final String MOD_ID = "maz-client";

    public static final ModuleManager MODULE_MANAGER = new ModuleManager();
    public static final long SESSION_START_MILLIS = System.currentTimeMillis();

    private static final int STARTUP_TITLE_STABLE_TICKS = 4;
    private static final int POST_WORLD_TITLE_STABLE_TICKS = 40;

    private static KeyMapping openMenuKey;
    private static boolean configLoaded;
    private static boolean brandedWindowTitle;
    private static boolean replacePauseScreen;
    private static boolean wasInWorld;
    private static boolean homeShownOnce;
    private static int titleScreenStableTicks;

    public static String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("dev");
    }

    @Override
    public void onInitializeClient() {

        MODULE_MANAGER.register(new FpsModule());
        MODULE_MANAGER.register(new FpsBoosterModule());

        MODULE_MANAGER.register(new MemoryModule());
        MODULE_MANAGER.register(new CoordinatesModule());
        MODULE_MANAGER.register(new PingModule());
        MODULE_MANAGER.register(new SpeedModule());
        MODULE_MANAGER.register(new DirectionModule());
        MODULE_MANAGER.register(new ClockModule());
        MODULE_MANAGER.register(new SessionTimerModule());
        MODULE_MANAGER.register(new CpsModule());
        MODULE_MANAGER.register(new KeystrokesModule());
        MODULE_MANAGER.register(new PotCounterModule());
        MODULE_MANAGER.register(new SimpleModule("Watermark", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Target Health", ModuleCategory.COMBAT));
        MODULE_MANAGER.register(new SimpleModule("Item Counter", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Armor Durability", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Compass", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Saturation", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Armor HUD", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Combo Counter", ModuleCategory.COMBAT));
        MODULE_MANAGER.register(new SimpleModule("Reach Display", ModuleCategory.COMBAT));
        MODULE_MANAGER.register(new SimpleModule("Potion HUD", ModuleCategory.COMBAT));

        MODULE_MANAGER.register(new ToggleSprintModule());
        MODULE_MANAGER.register(new ToggleSneakModule());
        MODULE_MANAGER.register(new ClearChatModule());
        MODULE_MANAGER.register(new AdvancedTooltipsModule());

        MODULE_MANAGER.register(new FullbrightModule());
        MODULE_MANAGER.register(new NoDynamicFovModule());
        MODULE_MANAGER.register(new NoHurtCamModule());
        MODULE_MANAGER.register(new NoRainModule());
        MODULE_MANAGER.register(new SmoothCameraModule());
        MODULE_MANAGER.register(new ZoomModule());
        MODULE_MANAGER.register(new SimpleModule("Hide Scoreboard", ModuleCategory.VISUAL));
        MODULE_MANAGER.register(new SimpleModule("NoParticles", ModuleCategory.VISUAL));

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "maz_hud"),
                MazHud::render
        );

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "maz_client")
        );

        openMenuKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.maz-client.open_menu",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        category
                )
        );

        ModuleHotkeys.registerAll(MODULE_MANAGER, category);

        // Pause-screen replacement can still use AFTER_INIT because it only applies while a
        // world is active. Title-screen recovery intentionally does NOT depend on AFTER_INIT:
        // disconnects can reach a panorama/title state without the one-shot init event leaving
        // MazClient a usable recovery path.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen) {
                replacePauseScreen = true;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!configLoaded) {
                configLoaded = true;
                ClientConfig.load(MODULE_MANAGER);
            }

            if (!brandedWindowTitle) {
                client.getWindow().setTitle("MazClient " + getVersion());
                brandedWindowTitle = true;
            }

            boolean worldActive = client.player != null
                    || client.level != null
                    || client.getConnection() != null
                    || client.hasSingleplayerServer();

            if (worldActive) {
                wasInWorld = true;
                titleScreenStableTicks = 0;
            } else if (client.gui.screen() instanceof TitleScreen) {
                // Drive title recovery from the live client state every tick instead of a
                // one-time screen-init callback. This recovers the panorama-only state that can
                // appear after disconnect when the vanilla title has no usable widgets.
                titleScreenStableTicks++;
                int requiredTicks = wasInWorld
                        ? POST_WORLD_TITLE_STABLE_TICKS
                        : STARTUP_TITLE_STABLE_TICKS;

                if (titleScreenStableTicks >= requiredTicks) {
                    titleScreenStableTicks = 0;
                    wasInWorld = false;
                    homeShownOnce = true;
                    client.gui.setScreen(new MazHomeScreen());
                }
            } else {
                // Do not count transitional/null/other screens as stable title time.
                titleScreenStableTicks = 0;
            }

            if (client.gui.screen() instanceof MazHomeScreen) {
                homeShownOnce = true;
            }

            if (replacePauseScreen && client.gui.screen() instanceof PauseScreen) {
                replacePauseScreen = false;
                client.gui.setScreen(new MazPauseScreen());
            } else if (!(client.gui.screen() instanceof PauseScreen)) {
                replacePauseScreen = false;
            }

            while (openMenuKey.consumeClick()) {
                client.gui.setScreen(new MazMenuScreen());
            }

            ModuleHotkeys.tick();
            MODULE_MANAGER.tick();
        });

        System.out.println("MazClient initialized!");
    }
}
