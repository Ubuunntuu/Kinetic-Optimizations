package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.entities.ItemAndXpRenderThrottler;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemAndXpClumpMixin {

    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$throttleClumpedItemMeshes(
        ItemEntity item,
        double dx, double dy, double dz,
        float yaw, float tickDelta,
        CallbackInfo ci
    ) {
        if (item != null && !ItemAndXpRenderThrottler.getInstance().shouldRenderClumpedDrop(0, item.x, item.y, item.z)) {
            ci.cancel();
        }
    }
}
