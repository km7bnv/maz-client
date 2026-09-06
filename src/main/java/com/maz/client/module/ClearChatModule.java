package com.maz.client.module;

import net.minecraft.client.Minecraft;

public class ClearChatModule extends Module {

    public ClearChatModule() {
        super("Clear Chat", ModuleCategory.UTILITY);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        client.gui.hud.getChat().clearMessages(true);
        setEnabled(false);
    }
}
