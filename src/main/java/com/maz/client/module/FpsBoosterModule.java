package com.maz.client.module;

import com.maz.client.MazClient;
import com.maz.client.config.ClientConfig;
import com.maz.client.gui.FpsBoosterScreen;
import net.minecraft.client.Minecraft;

import java.util.Properties;

/**
 * Applies a configurable performance profile while enabled, then restores the
 * exact player settings that were active before enabling it.
 */
public class FpsBoosterModule extends Module {

    public enum Preset {
        BALANCED,
        AGGRESSIVE,
        EXTREME,
        CUSTOM
    }

    private Integer previousSimulationDistance;
    private Double previousEntityDistanceScaling;
    private Boolean previousEntityShadows;

    private Preset preset = Preset.AGGRESSIVE;
    private int simulationDistance = 5;
    private int entityDistancePercent = 50;
    private int particleKeepEvery = 8;
    private boolean entityShadows = false;

    public FpsBoosterModule() {
        super("FPS Booster", ModuleCategory.PERFORMANCE);
    }

    @Override
    public void toggle() {
        Minecraft client = Minecraft.getInstance();
        client.gui.setScreen(new FpsBoosterScreen(client.gui.screen()));
    }

    public Preset getPreset() {
        return preset;
    }

    public int getSimulationDistance() {
        return simulationDistance;
    }

    public int getEntityDistancePercent() {
        return entityDistancePercent;
    }

    public int getParticleKeepEvery() {
        return particleKeepEvery;
    }

    public boolean getEntityShadows() {
        return entityShadows;
    }

    public void applyPreset(Preset preset) {
        switch (preset) {
            case BALANCED -> setValues(Preset.BALANCED, 5, 80, 3, false);
            case AGGRESSIVE -> setValues(Preset.AGGRESSIVE, 5, 50, 8, false);
            case EXTREME -> setValues(Preset.EXTREME, 5, 50, 16, false);
            case CUSTOM -> this.preset = Preset.CUSTOM;
        }
        settingsChanged();
    }

    public void setSimulationDistance(int value) {
        simulationDistance = clamp(value, 5, 12);
        preset = Preset.CUSTOM;
        settingsChanged();
    }

    public void setEntityDistancePercent(int value) {
        entityDistancePercent = clamp(value, 50, 150);
        preset = Preset.CUSTOM;
        settingsChanged();
    }

    public void setParticleKeepEvery(int value) {
        particleKeepEvery = clamp(value, 1, 16);
        preset = Preset.CUSTOM;
        settingsChanged();
    }

    public void setEntityShadows(boolean value) {
        entityShadows = value;
        preset = Preset.CUSTOM;
        settingsChanged();
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();

        previousSimulationDistance = client.options.simulationDistance().get();
        previousEntityDistanceScaling = client.options.entityDistanceScaling().get();
        previousEntityShadows = client.options.entityShadows().get();

        applyProfile();
    }

    @Override
    protected void onDisable() {
        Minecraft client = Minecraft.getInstance();

        if (previousSimulationDistance != null) {
            client.options.simulationDistance().set(previousSimulationDistance);
        }
        if (previousEntityDistanceScaling != null) {
            client.options.entityDistanceScaling().set(previousEntityDistanceScaling);
        }
        if (previousEntityShadows != null) {
            client.options.entityShadows().set(previousEntityShadows);
        }

        previousSimulationDistance = null;
        previousEntityDistanceScaling = null;
        previousEntityShadows = null;
    }

    private void applyProfile() {
        if (!isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();

        int baseSimulation = previousSimulationDistance != null
                ? previousSimulationDistance
                : client.options.simulationDistance().get();
        double baseEntity = previousEntityDistanceScaling != null
                ? previousEntityDistanceScaling
                : client.options.entityDistanceScaling().get();

        client.options.simulationDistance().set(Math.min(baseSimulation, simulationDistance));
        client.options.entityDistanceScaling().set(Math.min(baseEntity, entityDistancePercent / 100.0D));
        client.options.entityShadows().set(entityShadows);
    }

    private void settingsChanged() {
        applyProfile();
        ClientConfig.save(MazClient.MODULE_MANAGER);
    }

    private void setValues(Preset preset, int simulation, int entityPercent,
                           int particles, boolean shadows) {
        this.preset = preset;
        this.simulationDistance = simulation;
        this.entityDistancePercent = entityPercent;
        this.particleKeepEvery = particles;
        this.entityShadows = shadows;
    }

    @Override
    public void loadConfig(Properties properties) {
        String prefix = "fpsBooster.";
        preset = parsePreset(properties.getProperty(prefix + "preset"), preset);
        simulationDistance = parseInt(properties.getProperty(prefix + "simulationDistance"), simulationDistance, 5, 12);
        entityDistancePercent = parseInt(properties.getProperty(prefix + "entityDistancePercent"), entityDistancePercent, 50, 150);
        particleKeepEvery = parseInt(properties.getProperty(prefix + "particleKeepEvery"), particleKeepEvery, 1, 16);
        entityShadows = Boolean.parseBoolean(properties.getProperty(prefix + "entityShadows", Boolean.toString(entityShadows)));
    }

    @Override
    public void saveConfig(Properties properties) {
        String prefix = "fpsBooster.";
        properties.setProperty(prefix + "preset", preset.name());
        properties.setProperty(prefix + "simulationDistance", Integer.toString(simulationDistance));
        properties.setProperty(prefix + "entityDistancePercent", Integer.toString(entityDistancePercent));
        properties.setProperty(prefix + "particleKeepEvery", Integer.toString(particleKeepEvery));
        properties.setProperty(prefix + "entityShadows", Boolean.toString(entityShadows));
    }

    private static Preset parsePreset(String value, Preset fallback) {
        if (value == null) return fallback;
        try {
            return Preset.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback, int min, int max) {
        if (value == null) return fallback;
        try {
            return clamp(Integer.parseInt(value), min, max);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
