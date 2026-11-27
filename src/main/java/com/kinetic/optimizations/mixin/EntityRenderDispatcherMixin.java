package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.entities.EntityVisibilityOptimizer;
import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import com.kinetic.optimizations.rendering.FastFrustumCuller;
import net.minecraft.client.render.Culler;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.ArmorStandEntity;
import net.minecraft.entity.living.player.PlayerEntity;
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

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void kinetic$earlyCullEntity(
        Entity entity,
        Culler culler,
        double camX,
        double camY,
        double camZ,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!KineticConfig.modEnabled || !KineticConfig.fastEntityVisibility || entity == null) {
            return;
        }

        // Never cull players
        if (entity instanceof PlayerEntity) {
            return;
        }

        EntityVisibilityOptimizer optimizer = KineticEntityParticleEngine.getInstance().getEntityOptimizer();

        // Fast-path cull for invisible marker armor stands with no custom name
        if (KineticConfig.skipInvisibleArmorStands
                && entity instanceof ArmorStandEntity
                && entity.isInvisible()
                && !entity.hasCustomName()) {
            optimizer.recordEntityCulled();
            FastFrustumCuller.getInstance().shouldSkipInvisibleArmorStand(true, false, false);
            cir.setReturnValue(Boolean.FALSE);
            return;
        }

        // Fast squared-distance cull before vanilla bounding-box math
        if (KineticConfig.optimizeEntityDistanceCalc) {
            double dx = entity.x - camX;
            double dy = entity.y - camY;
            double dz = entity.z - camZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > KineticConfig.maxEntityRenderDistanceSq) {
                optimizer.recordEntityCulled();
                cir.setReturnValue(Boolean.FALSE);
            }
        }
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
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            KineticEntityParticleEngine.getInstance().getEntityOptimizer().recordEntityRendered();
        }
    }
}
