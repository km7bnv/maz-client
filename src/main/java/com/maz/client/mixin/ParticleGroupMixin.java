package com.maz.client.mixin;

import com.maz.client.MazClient;
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
    private void maz$reduceParticles(
            Particle particle,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Module booster = MazClient.MODULE_MANAGER.getModule("FPS Booster");

        if (booster == null || !booster.isEnabled()) {
            return;
        }

        maz$particleCounter++;

        // Keep roughly one out of every three particles.
        if (maz$particleCounter % 3 != 0) {
            cir.setReturnValue(false);
        }
    }
}
