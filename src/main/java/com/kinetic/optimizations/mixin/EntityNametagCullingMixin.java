package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.config.KineticConfig;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityNametagCullingMixin<T extends Entity> {

    @Inject(
        method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void kinetic$cullDistantNametag(
        T entity,
        String text,
        double relX, double relY, double relZ,
        int maxDistance,
        CallbackInfo ci
    ) {
        if (!KineticConfig.modEnabled || !KineticConfig.cullDistantNametags) {
            return;
        }
        double distSq = relX * relX + relY * relY + relZ * relZ;
        if (distSq > KineticConfig.maxNametagRenderDistanceSq) {
            ci.cancel();
        }
    }
}
