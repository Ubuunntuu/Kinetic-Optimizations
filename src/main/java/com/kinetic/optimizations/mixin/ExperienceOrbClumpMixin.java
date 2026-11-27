package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.entities.ItemAndXpRenderThrottler;
import net.minecraft.client.render.entity.ExperienceOrbRenderer;
import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbRenderer.class)
public abstract class ExperienceOrbClumpMixin {

    @Inject(method = "render(Lnet/minecraft/entity/ExperienceOrbEntity;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$throttleClumpedXpOrbs(
        ExperienceOrbEntity orb,
        double dx, double dy, double dz,
        float yaw, float tickDelta,
        CallbackInfo ci
    ) {
        if (orb != null && !ItemAndXpRenderThrottler.getInstance().shouldRenderClumpedDrop(1, orb.x, orb.y, orb.z)) {
            ci.cancel();
        }
    }
}
