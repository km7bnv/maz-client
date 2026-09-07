package com.maz.client.module;

public enum ModuleCategory {

    PERFORMANCE("Performance"),
    HUD("HUD"),
    COMBAT("Combat"),
    VISUAL("Visual"),
    UTILITY("Utility");

    private final String displayName;

    ModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
