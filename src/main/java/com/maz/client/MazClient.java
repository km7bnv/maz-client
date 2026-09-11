package com.maz.client;

import com.maz.client.config.ClientConfig;
import com.maz.client.gui.BiomeHud;
import com.maz.client.gui.CurrentBlockHud;
import com.maz.client.gui.DimensionHud;
import com.maz.client.gui.DurabilityStatusHud;
import com.maz.client.gui.FlightStatusHud;
import com.maz.client.gui.FrameStatsHud;
import com.maz.client.gui.InventorySpaceHud;
import com.maz.client.gui.LastDeathHud;
import com.maz.client.gui.LightLevelHud;
import com.maz.client.gui.MazHud;
import com.maz.client.gui.MazMenuScreen;
import com.maz.client.gui.MountHealthHud;
import com.maz.client.gui.OffhandCounterHud;
import com.maz.client.gui.RecentGainsHud;
import com.maz.client.gui.WorldTimeHud;
import com.maz.client.gui.XpProgressHud;
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
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public class MazClient implements ClientModInitializer {

    public static final String MOD_ID = "maz-client";

    public static final ModuleManager MODULE_MANAGER = new ModuleManager();
    public static final long SESSION_START_MILLIS = System.currentTimeMillis();

    private static KeyMapping openMenuKey;
    private static boolean configLoaded;
    private static boolean brandedWindowTitle;

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
        MODULE_MANAGER.register(new SimpleModule("Frame Stats", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Biome", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Dimension", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Light Level", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("World Time", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Durability Status", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("XP Progress", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Last Death", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Inventory Space", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Offhand Counter", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Recent Gains", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Mount Health", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Flight Status", ModuleCategory.HUD));
        MODULE_MANAGER.register(new SimpleModule("Current Block", ModuleCategory.HUD));
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
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "frame_stats_hud"),
                FrameStatsHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "biome_hud"),
                BiomeHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "dimension_hud"),
                DimensionHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "light_level_hud"),
                LightLevelHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "world_time_hud"),
                WorldTimeHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "durability_status_hud"),
                DurabilityStatusHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "xp_progress_hud"),
                XpProgressHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "last_death_hud"),
                LastDeathHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "inventory_space_hud"),
                InventorySpaceHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "offhand_counter_hud"),
                OffhandCounterHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "recent_gains_hud"),
                RecentGainsHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "mount_health_hud"),
                MountHealthHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "flight_status_hud"),
                FlightStatusHud::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "current_block_hud"),
                CurrentBlockHud::render
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

        // Minecraft owns every vanilla menu instance and lifecycle transition. MazClient
        // customizes those menus through render/init hooks instead of replacing TitleScreen
        // or PauseScreen objects while they are active. This preserves custom styling while
        // avoiding screen-instance swaps during startup, pause, saving, and disconnect.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!configLoaded) {
                configLoaded = true;
                ClientConfig.load(MODULE_MANAGER);
            }

            if (!brandedWindowTitle) {
                client.getWindow().setTitle("MazClient " + getVersion());
                brandedWindowTitle = true;
            }

            LastDeathHud.tick(client);

            while (openMenuKey.consumeClick()) {
                client.gui.setScreen(new MazMenuScreen());
            }

            ModuleHotkeys.tick();
            MODULE_MANAGER.tick();
        });

        System.out.println("MazClient initialized!");
    }
}
