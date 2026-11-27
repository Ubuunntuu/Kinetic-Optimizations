package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import net.minecraft.client.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void kinetic$onParticleTick(CallbackInfo ci) {
        KineticEntityParticleEngine.getInstance().getParticleOptimizer().onFrameStart();
    }
}
