package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FI)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$cullDistantOrHiddenBlockEntity(BlockEntity blockEntity, float tickDelta, int destroyStage, CallbackInfo ci) {
        if (!KineticConfig.modEnabled || !KineticConfig.cullHiddenTileEntities || blockEntity == null || destroyStage >= 0) {
            return;
        }
        boolean isGlobal = blockEntity instanceof BeaconBlockEntity;
        BlockPos pos = blockEntity.getPos();
        if (pos != null && !KineticEntityParticleEngine.getInstance().getBlockEntityOptimizer()
                .shouldRenderBlockEntity(pos.getX(), pos.getY(), pos.getZ(), isGlobal)) {
            ci.cancel();
        }
    }
}
