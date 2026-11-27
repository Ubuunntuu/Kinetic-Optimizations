package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void kinetic$onEntityDispatcherInit(CallbackInfo ci) {
        KineticEntityParticleEngine.getInstance().onFrameStart();
    }
}
