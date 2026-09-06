package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class AdvancedTooltipsModule extends Module {

    private Boolean previous;

    public AdvancedTooltipsModule() {
        super("Advanced Item Tooltips", ModuleCategory.UTILITY);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previous = client.options.advancedItemTooltips;
        client.options.advancedItemTooltips = true;
    }

    @Override
    protected void onDisable() {
        if (previous != null) {
            Minecraft.getInstance().options.advancedItemTooltips = previous;
            previous = null;
        }
    }
}
