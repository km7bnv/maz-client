package com.maz.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PotCounterModule extends Module {

    public PotCounterModule() {
        super("PotCounter", ModuleCategory.HUD);
    }

    public static int countPotions(Minecraft client) {
        if (client.player == null) {
            return 0;
        }

        int count = 0;
        var inventory = client.player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
