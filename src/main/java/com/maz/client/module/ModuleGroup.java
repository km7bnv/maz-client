package com.maz.client.module;

public enum ModuleGroup {
    PERFORMANCE("Performance"),
    STATS("Stats"),
    INPUT("Input"),
    VISUALS("Visuals"),
    GENERAL("General");

    private final String displayName;

    ModuleGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
