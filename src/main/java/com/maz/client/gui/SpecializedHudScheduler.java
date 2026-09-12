package com.maz.client.gui;

import net.minecraft.client.Minecraft;

/**
 * Central 20 Hz scheduler for HUD telemetry that does not need render-frame cadence.
 *
 * Heavy/formatting refreshes are intentionally phase-staggered so inventory scans,
 * world lookups and text rebuilds do not all land on the same client tick. Render
 * callbacks remain responsible only for drawing their already-cached state.
 */
public final class SpecializedHudScheduler {
    private static long tickCounter;

    private SpecializedHudScheduler() {}

    public static void tick(Minecraft client) {
        long tick = tickCounter++;

        // Current block historically refreshed every 100 ms. At Minecraft's normal
        // 20 Hz client cadence, two ticks preserve that rate without a render-time clock check.
        if ((tick & 1L) == 0L) {
            CurrentBlockHud.tick(client);
        }

        // Mount/flight state can change between scheduled telemetry refreshes. These
        // lightweight wrappers run every tick but only rebuild expensive text on change
        // or on their assigned 250 ms phase.
        MountHealthHud.tick(client, Math.floorMod(tick, 5L) == 3L);
        FlightStatusHud.tick(client, Math.floorMod(tick, 5L) == 4L);

        // 250 ms group, deliberately distributed across five phases.
        switch ((int) Math.floorMod(tick, 5L)) {
            case 0 -> {
                InventorySpaceHud.tick(client);
                BiomeHud.tick(client);
            }
            case 1 -> {
                OffhandCounterHud.tick(client);
                LightLevelHud.tick(client);
            }
            case 2 -> {
                DurabilityStatusHud.tick(client);
                XpProgressHud.tick(client);
            }
            case 3 -> ChunkPositionHud.tick(client);
            case 4 -> RecentGainsHud.tick(client, System.currentTimeMillis());
            default -> { }
        }

        // 500 ms group on opposite half-cycles so both world lookups do not coincide.
        int tenPhase = (int) Math.floorMod(tick, 10L);
        if (tenPhase == 0) {
            DimensionHud.tick(client);
        } else if (tenPhase == 5) {
            WorldTimeHud.tick(client);
        }
    }
}
