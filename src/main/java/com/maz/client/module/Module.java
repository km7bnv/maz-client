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
            case "FPS" -> "Shows your current frames per second so you can monitor game performance at a glance.";
            case "FPS Booster" -> "Applies configurable client-side performance tweaks for entities, particles and visuals without changing render or simulation distance.";
            case "Memory" -> "Shows Java memory usage and pressure so you can spot unusually high RAM use.";
            case "Coordinates" -> "Displays XYZ, chunk and local coordinates, plus Nether coordinate conversion when useful.";
            case "Ping" -> "Shows your current multiplayer latency in milliseconds with lightweight smoothing.";
            case "Speed" -> "Displays your current horizontal movement speed in blocks per second.";
            case "Direction" -> "Shows your facing direction together with live yaw and pitch.";
            case "Clock" -> "Displays your computer's current local time on the HUD.";
            case "Session Timer" -> "Tracks how long the current MazClient session has been running.";
            case "CPS" -> "Displays live left- and right-clicks per second.";
            case "Keystrokes" -> "Shows live WASD and mouse-button input on a compact keystrokes HUD.";
            case "PotCounter" -> "Counts potion, splash-potion and lingering-potion stacks in your inventory.";
            case "Watermark" -> "Displays a compact MazClient watermark on the HUD.";
            case "Target Health" -> "Shows the name and current health of the living entity under your crosshair.";
            case "Item Counter" -> "Shows the selected item and counts matching items across your inventory.";
            case "Compass" -> "Displays an eight-direction compass with your live yaw angle.";
            case "Saturation" -> "Adds client-visible saturation information alongside the vanilla hunger bar.";
            case "Frame Stats" -> "Shows frame-time statistics to help identify FPS drops and stutter.";
            case "Biome" -> "Displays the biome at your current position.";
            case "Dimension" -> "Shows the dimension you are currently in.";
            case "Light Level" -> "Displays the light level at your current position.";
            case "World Time" -> "Shows the current in-game world time on the HUD.";
            case "Durability Status" -> "Summarizes the durability condition of your currently used or equipped gear.";
            case "XP Progress" -> "Shows your experience level and progress toward the next level.";
            case "Last Death" -> "Shows the coordinates and dimension of your most recent death during this game session.";
            case "Inventory Space" -> "Shows how many of your 36 main inventory slots are still empty.";
            case "Offhand Counter" -> "Shows your offhand item and how many matching items you have available.";
            case "Recent Gains" -> "Shows recent experience or inventory gains tracked by MazClient.";
            case "Mount Health" -> "Displays the health of the mount you are currently riding.";
            case "Flight Status" -> "Shows whether flight or gliding-related movement is currently active.";
            case "Current Block" -> "Shows the block under your crosshair together with its coordinates.";
            case "Chunk Position" -> "Displays your current chunk coordinates and position inside the chunk.";
            case "Combo Counter" -> "Tracks consecutive combat hits and resets when the combo is broken.";
            case "Reach Display" -> "Shows the measured distance of your most recent registered combat hit.";
            case "ToggleSprint" -> "Keeps sprint toggled without requiring you to hold the sprint key.";
            case "ToggleSneak" -> "Keeps sneak toggled without requiring you to hold the sneak key.";
            case "Clear Chat" -> "Clears the visible client chat history immediately when activated.";
            case "Advanced Item Tooltips" -> "Enables Minecraft's advanced item tooltip information while the module is active.";
            case "Durability Warnings" -> "Warns you locally when equipped or held gear is getting dangerously low on durability.";
            case "Inventory Full Warning" -> "Sends one local warning when all 36 main inventory slots become full.";
            case "Low Health Warning" -> "Warns locally when your health crosses MazClient's low and critical health thresholds.";
            case "Low Hunger Warning" -> "Warns locally when your hunger falls to low or critical levels without automatically eating.";
            case "Low Air Warning" -> "Warns locally as your remaining underwater air approaches dangerous levels.";
            case "Fullbright" -> "Raises client brightness while enabled and restores your previous gamma setting when disabled.";
            case "NoDynamicFOV" -> "Prevents movement and gameplay effects from dynamically changing your field of view.";
            case "NoHurtCam" -> "Disables the camera shake shown when your player takes damage.";
            case "NoRain" -> "Hides client-side rain rendering for a cleaner view without changing server weather.";
            case "Smooth Camera" -> "Enables Minecraft's smooth-camera movement while the module is active.";
            case "Zoom" -> "Temporarily narrows your field of view for a zoomed-in view.";
            case "Hide Scoreboard" -> "Hides the vanilla sidebar scoreboard from your client.";
            case "NoParticles" -> "Disables client particle rendering to reduce visual clutter and rendering work.";
            default -> "MazClient module: " + name + ".";
        };
    }
}