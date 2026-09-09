package com.maz.client.module;

import com.maz.client.MazClient;
import com.maz.client.config.ClientConfig;

import java.util.Properties;

public abstract class Module {

    private final String name;
    private final String description;
    private final ModuleCategory category;

    private boolean enabled;

    protected Module(String name, ModuleCategory category) {
        this(name, defaultDescription(name), category);
    }

    protected Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description == null || description.isBlank()
                ? "MazClient module."
                : description;
        this.category = category;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAction() {
        return false;
    }

    public void runAction() {
        toggle();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }

        ClientConfig.save(MazClient.MODULE_MANAGER);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick() {
    }

    public void loadConfig(Properties properties) {
    }

    public void saveConfig(Properties properties) {
    }

    private static String defaultDescription(String name) {
        return switch (name) {
            case "FPS" -> "Shows your current frames per second on the HUD.";
            case "FPS Booster" -> "Configurable simulation, entity and particle tuning for higher FPS while render distance stays manual.";
            case "Memory" -> "Shows current Java memory usage on the HUD.";
            case "Coordinates" -> "Displays your current X, Y and Z coordinates.";
            case "Ping" -> "Displays your current multiplayer latency.";
            case "Speed" -> "Shows your current movement speed.";
            case "Direction" -> "Shows the direction you are currently facing.";
            case "Clock" -> "Displays the current clock time on the HUD.";
            case "Session Timer" -> "Tracks how long the current MazClient session has been running.";
            case "CPS" -> "Shows your current clicks per second.";
            case "Keystrokes" -> "Displays live movement and mouse key presses.";
            case "Pot Counter" -> "Counts healing or potion items in your inventory.";
            case "Watermark" -> "Displays MazClient branding on the HUD.";
            case "Target Health" -> "Shows the name and health of the entity under your crosshair.";
            case "Item Counter" -> "Counts the selected item across your inventory.";
            case "Armor Durability" -> "Shows the remaining durability of your equipped armor.";
            case "Compass" -> "Displays an 8-direction compass with live yaw.";
            case "Saturation" -> "Shows saturation information alongside the hunger bar.";
            case "Armor HUD" -> "Displays your equipped armor directly on the HUD.";
            case "XP Progress" -> "Shows your experience level and progress toward the next level.";
            case "Last Death" -> "Shows the coordinates and dimension of your most recent death in this game session.";
            case "Inventory Space" -> "Shows how many of your 36 main inventory slots are still empty.";
            case "Offhand Counter" -> "Shows the current offhand item and how many matching items are in your inventory.";
            case "Combo Counter" -> "Tracks consecutive combat hits before your combo breaks.";
            case "Reach Display" -> "Displays recent combat reach distance information.";
            case "Potion HUD" -> "Shows active potion and status effects on the HUD.";
            case "ToggleSprint" -> "Keeps sprint enabled without holding the sprint key.";
            case "ToggleSneak" -> "Keeps sneak enabled without holding the sneak key.";
            case "Clear Chat" -> "Instantly clears the visible chat history when clicked.";
            case "Advanced Item Tooltips" -> "Enables extra technical information in item tooltips.";
            case "Fullbright" -> "Raises client brightness to make dark areas easier to see.";
            case "NoDynamicFOV" -> "Prevents movement effects from changing your field of view.";
            case "NoHurtCam" -> "Disables the camera shake shown when taking damage.";
            case "NoRain" -> "Hides rain rendering on the client for a cleaner view.";
            case "Smooth Camera" -> "Enables smoother camera movement.";
            case "Zoom" -> "Provides a temporary zoomed-in field of view.";
            case "Hide Scoreboard" -> "Hides the vanilla sidebar scoreboard.";
            case "NoParticles" -> "Stops client particle rendering for maximum visual performance.";
            default -> "MazClient module.";
        };
    }
}
