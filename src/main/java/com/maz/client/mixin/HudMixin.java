package com.maz.client.mixin;

import com.maz.client.MazClient;
import com.maz.client.module.Module;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.scores.Objective;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
    private static Module maz$hideScoreboardModule;

    @Inject(
            method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void maz$hideScoreboard(GuiGraphics graphics, Objective objective, CallbackInfo ci) {
        Module module = maz$hideScoreboardModule;
        if (module == null) {
            module = MazClient.MODULE_MANAGER.getModule("Hide Scoreboard");
            maz$hideScoreboardModule = module;
        }
        if (module != null && module.isEnabled()) ci.cancel();
    }
}
