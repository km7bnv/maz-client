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
    private static Module maz$noParticles;
    private static FpsBoosterModule maz$fpsBooster;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void maz$filterParticles(
            Particle particle,
            CallbackInfoReturnable<Boolean> cir
    ) {
        maz$resolveModules();

        if (maz$noParticles != null && maz$noParticles.isEnabled()) {
            cir.setReturnValue(false);
            return;
        }

        FpsBoosterModule fpsBooster = maz$fpsBooster;
        if (fpsBooster == null || !fpsBooster.isEnabled()) {
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

    private static void maz$resolveModules() {
        if (maz$noParticles == null) {
            maz$noParticles = MazClient.MODULE_MANAGER.getModule("NoParticles");
        }
        if (maz$fpsBooster == null) {
            Module module = MazClient.MODULE_MANAGER.getModule("FPS Booster");
            if (module instanceof FpsBoosterModule fpsBooster) {
                maz$fpsBooster = fpsBooster;
            }
        }
    }
}
