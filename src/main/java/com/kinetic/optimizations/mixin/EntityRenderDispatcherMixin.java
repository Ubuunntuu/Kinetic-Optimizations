package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.entities.EntityVisibilityOptimizer;
import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import com.kinetic.optimizations.rendering.FastFrustumCuller;
import net.minecraft.client.render.Culler;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "setCameraPos", at = @At("HEAD"))
    private void kinetic$updateCameraPosition(double x, double y, double z, CallbackInfo ci) {
        FastFrustumCuller.getInstance().setCameraPosition(x, y, z);
    }

    @Inject(method = "shouldRender", at = @At("RETURN"))
    private void kinetic$trackEntityVisibility(
        Entity entity,
        Culler culler,
        double camX,
        double camY,
        double camZ,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!KineticConfig.modEnabled) {
            return;
        }
        EntityVisibilityOptimizer optimizer = KineticEntityParticleEngine.getInstance().getEntityOptimizer();
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            optimizer.recordEntityRendered();
        } else {
            optimizer.recordEntityCulled();
        }
    }
}
