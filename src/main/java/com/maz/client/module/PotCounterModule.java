package com.maz.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PotCounterModule extends Module {

    private static long lastCountTick = Long.MIN_VALUE;
    private static int cachedCount;

    public PotCounterModule() {
        super("PotCounter", ModuleCategory.COMBAT);
    }

    public static int countPotions(Minecraft client) {
        if (client.player == null) {
            cachedCount = 0;
            lastCountTick = Long.MIN_VALUE;
            return 0;
        }

        long tick = client.level != null ? client.level.getGameTime() : Long.MIN_VALUE;
        if (tick == lastCountTick) {
            return cachedCount;
        }

        lastCountTick = tick;
        int count = 0;
        var inventory = client.player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
                count += stack.getCount();
            }
        }
        cachedCount = count;
        return cachedCount;
    }
}
