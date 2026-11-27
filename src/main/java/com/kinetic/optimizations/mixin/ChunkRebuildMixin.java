package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.chunks.KineticChunkEngine;
import com.kinetic.optimizations.config.KineticConfig;
import net.minecraft.client.render.world.RenderChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderChunk.class)
public abstract class ChunkRebuildMixin {

    @Shadow
    public abstract BlockPos getOrigin();

    @Shadow
    public abstract boolean isDirty();

    @Inject(method = "setDirty", at = @At("HEAD"), cancellable = true)
    private void kinetic$deduplicateChunkDirty(boolean urgent, CallbackInfo ci) {
        if (!KineticConfig.modEnabled || !KineticConfig.deduplicateChunkUpdates || urgent) {
            KineticChunkEngine.getInstance().notifyChunkRebuildStarted();
            return;
        }
        // If already marked dirty and we already scheduled this 16x16x16 section in the current tick, skip redundant dirty work
        if (this.isDirty()) {
            BlockPos origin = this.getOrigin();
            if (origin != null && !KineticChunkEngine.getInstance().shouldScheduleChunkRebuild(origin.getX(), origin.getY(), origin.getZ())) {
                ci.cancel();
                return;
            }
        }
        KineticChunkEngine.getInstance().notifyChunkRebuildStarted();
    }
}
