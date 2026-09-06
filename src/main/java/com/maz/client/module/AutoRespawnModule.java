package com.maz.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;

public class AutoRespawnModule extends Module {

    private boolean sent;

    public AutoRespawnModule() {
        super("AutoRespawn", ModuleCategory.UTILITY);
    }

    @Override
    public void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) {
            sent = false;
            return;
        }

        if (client.player.isDeadOrDying()) {
            if (!sent) {
                client.getConnection().send(new ServerboundClientCommandPacket(
                        ServerboundClientCommandPacket.Action.PERFORM_RESPAWN
                ));
                sent = true;
            }
        } else {
            sent = false;
        }
    }
}
