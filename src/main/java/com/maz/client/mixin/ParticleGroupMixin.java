package com.maz.client.mixin;

import com.maz.client.MazClient;
import com.maz.client.module.FpsBoosterModule;
import com.maz.client.module.Module;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleGroup.class)
public class ParticleGroupMixin {

    private static int maz$particleCounter = 0;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void maz$filterParticles(
            Particle particle,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Module noParticles = MazClient.MODULE_MANAGER.getModule("NoParticles");
        if (noParticles != null && noParticles.isEnabled()) {
            cir.setReturnValue(false);
            return;
        }

        Module booster = MazClient.MODULE_MANAGER.getModule("FPS Booster");
        if (!(booster instanceof FpsBoosterModule fpsBooster) || !fpsBooster.isEnabled()) {
            return;
        }

        int keepEvery = fpsBooster.getParticleKeepEvery();
        if (keepEvery <= 1) {
            return;
        }

        maz$particleCounter++;
        if (maz$particleCounter % keepEvery != 0) {
            cir.setReturnValue(false);
        }
    }
}
