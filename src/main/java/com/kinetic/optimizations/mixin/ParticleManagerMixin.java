package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import com.kinetic.optimizations.particles.ParticleEngineOptimizer;
import net.minecraft.client.ParticleManager;
import net.minecraft.client.entity.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void kinetic$onParticleTick(CallbackInfo ci) {
        KineticEntityParticleEngine.getInstance().getParticleOptimizer().setActiveParticleCount(0);
    }

    @Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
    private void kinetic$admitOrCullSpawnedParticle(
        int particleId,
        double x, double y, double z,
        double vx, double vy, double vz,
        int[] parameters,
        CallbackInfoReturnable<Particle> cir
    ) {
        ParticleEngineOptimizer optimizer = KineticEntityParticleEngine.getInstance().getParticleOptimizer();
        // Preserve explosion/barrier/critical hit feedback (IDs 0, 1, 2, 9, 35) unconditionally
        boolean isCritical = (particleId <= 2 || particleId == 9 || particleId == 35);
        if (!optimizer.shouldSpawnParticle(x, y, z, isCritical)) {
            cir.setReturnValue(null);
            return;
        }
        optimizer.incrementActiveParticles();
    }
}
