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

    private static boolean maz$modulesResolved;
    private static int maz$particlesUntilKeep;
    private static int maz$lastKeepEvery = -1;
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
            maz$lastKeepEvery = -1;
            maz$particlesUntilKeep = 0;
            return;
        }

        int keepEvery = fpsBooster.getParticleKeepEvery();
        if (keepEvery <= 1) {
            maz$lastKeepEvery = keepEvery;
            maz$particlesUntilKeep = 0;
            return;
        }

        if (keepEvery != maz$lastKeepEvery) {
            maz$lastKeepEvery = keepEvery;
            maz$particlesUntilKeep = keepEvery - 1;
        }

        if (maz$particlesUntilKeep > 0) {
            maz$particlesUntilKeep--;
            cir.setReturnValue(false);
            return;
        }

        maz$particlesUntilKeep = keepEvery - 1;
    }

    private static void maz$resolveModules() {
        if (maz$modulesResolved) {
            return;
        }

        maz$noParticles = MazClient.MODULE_MANAGER.getModule("NoParticles");
        Module module = MazClient.MODULE_MANAGER.getModule("FPS Booster");
        if (module instanceof FpsBoosterModule fpsBooster) {
            maz$fpsBooster = fpsBooster;
        }
        maz$modulesResolved = true;
    }
}
