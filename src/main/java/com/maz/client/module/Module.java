package com.maz.client.module;

public abstract class Module {

    private final String name;
    private final ModuleCategory category;

    private boolean enabled;

    protected Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
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
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick() {
    }
}