package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.lighting.LightingDeduplicator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldLightingMixin {

    @Shadow
    @Final
    public boolean isClient;

    @Inject(method = "updateLight(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void kinetic$deduplicateClientLightCheck(LightType type, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isClient || type == null || pos == null) {
            return;
        }
        if (!LightingDeduplicator.getInstance().shouldCheckLight(type.ordinal(), pos.getX(), pos.getY(), pos.getZ())) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }
}
