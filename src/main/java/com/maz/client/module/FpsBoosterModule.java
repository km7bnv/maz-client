package com.maz.client.module;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;

/**
 * Applies a conservative performance profile while enabled, then restores the
 * exact values the player had before enabling it.
 *
 * This intentionally focuses on settings with a real render/CPU cost instead
 * of pretending to "boost" FPS with JVM tricks or busy-loop tweaks.
 */
public class FpsBoosterModule extends Module {

    private Integer previousRenderDistance;
    private Integer previousSimulationDistance;
    private Double previousEntityDistanceScaling;
    private ParticleStatus previousParticles;
    private Boolean previousEntityShadows;
    private GraphicsStatus previousGraphicsMode;

    public FpsBoosterModule() {
        super("FPS Booster", ModuleCategory.PERFORMANCE);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();

        previousRenderDistance = client.options.renderDistance().get();
        previousSimulationDistance = client.options.simulationDistance().get();
        previousEntityDistanceScaling = client.options.entityDistanceScaling().get();
        previousParticles = client.options.particles().get();
        previousEntityShadows = client.options.entityShadows().get();
        previousGraphicsMode = client.options.graphicsMode().get();

        // Strong enough to help low-end machines without making the game look broken.
        if (previousRenderDistance > 6) {
            client.options.renderDistance().set(6);
        }
        if (previousSimulationDistance > 5) {
            client.options.simulationDistance().set(5);
        }
        if (previousEntityDistanceScaling > 0.75D) {
            client.options.entityDistanceScaling().set(0.75D);
        }

        client.options.particles().set(ParticleStatus.MINIMAL);
        client.options.entityShadows().set(false);
        client.options.graphicsMode().set(GraphicsStatus.FAST);
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
        if (previousParticles != null) {
            client.options.particles().set(previousParticles);
        }
        if (previousEntityShadows != null) {
            client.options.entityShadows().set(previousEntityShadows);
        }
        if (previousGraphicsMode != null) {
            client.options.graphicsMode().set(previousGraphicsMode);
        }

        previousRenderDistance = null;
        previousSimulationDistance = null;
        previousEntityDistanceScaling = null;
        previousParticles = null;
        previousEntityShadows = null;
        previousGraphicsMode = null;
    }
}
