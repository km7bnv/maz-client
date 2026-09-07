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

    private static KeyMapping openMenuKey;
    private static boolean configLoaded;
    private static boolean brandedWindowTitle;
    private static boolean replaceTitleScreen;
    private static boolean replacePauseScreen;
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

        // Queue vanilla-screen replacements instead of swapping screens from AFTER_INIT.
        // Title screen replacement gets an additional stability/teardown guard so leaving a
        // singleplayer or multiplayer world cannot race Minecraft's disconnect transition.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                replaceTitleScreen = true;
                titleScreenStableTicks = 0;
            } else if (screen instanceof PauseScreen) {
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

            if (replaceTitleScreen && client.gui.screen() instanceof TitleScreen) {
                boolean teardownComplete = client.player == null
                        && client.level == null
                        && client.getConnection() == null
                        && !client.hasSingleplayerServer();
                if (teardownComplete) {
                    titleScreenStableTicks++;
                    if (titleScreenStableTicks >= 2) {
                        replaceTitleScreen = false;
                        titleScreenStableTicks = 0;
                        client.gui.setScreen(new MazHomeScreen());
                    }
                } else {
                    titleScreenStableTicks = 0;
                }
            } else {
                replaceTitleScreen = false;
                titleScreenStableTicks = 0;
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
