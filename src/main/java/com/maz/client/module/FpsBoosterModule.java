package com.maz.client.module;

import net.minecraft.client.Minecraft;

/**
 * Applies a real performance profile while enabled, then restores the exact
 * values the player had before enabling it.
 */
public class FpsBoosterModule extends Module {

    private Integer previousRenderDistance;
    private Integer previousSimulationDistance;
    private Double previousEntityDistanceScaling;
    private Boolean previousEntityShadows;

    public FpsBoosterModule() {
        super("FPS Booster", ModuleCategory.PERFORMANCE);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();

        previousRenderDistance = client.options.renderDistance().get();
        previousSimulationDistance = client.options.simulationDistance().get();
        previousEntityDistanceScaling = client.options.entityDistanceScaling().get();
        previousEntityShadows = client.options.entityShadows().get();

        // Reduce both GPU draw work and client-side chunk/entity work.
        if (previousRenderDistance > 6) {
            client.options.renderDistance().set(6);
        }
        if (previousSimulationDistance > 5) {
            client.options.simulationDistance().set(5);
        }
        if (previousEntityDistanceScaling > 0.75D) {
            client.options.entityDistanceScaling().set(0.75D);
        }

        client.options.entityShadows().set(false);

        // Particle reduction is handled by ParticleGroupMixin so it remains
        // compatible with Minecraft 26.2's changed option enum mappings.
    }

    @Override
    protected void onDisable() {
        Minecraft client = Minecraft.getInstance();

        if (previousRenderDistance != null) {
            client.options.renderDistance().set(previousRenderDistance);
        }
        if (previousSimulationDistance != null) {
            client.options.simulationDistance().set(previousSimulationDistance);
        }
        if (previousEntityDistanceScaling != null) {
            client.options.entityDistanceScaling().set(previousEntityDistanceScaling);
        }
        if (previousEntityShadows != null) {
            client.options.entityShadows().set(previousEntityShadows);
        }

        previousRenderDistance = null;
        previousSimulationDistance = null;
        previousEntityDistanceScaling = null;
        previousEntityShadows = null;
    }
}
