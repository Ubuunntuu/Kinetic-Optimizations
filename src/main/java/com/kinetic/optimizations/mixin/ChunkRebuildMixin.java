package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.chunks.KineticChunkEngine;
import net.minecraft.client.render.world.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderChunk.class)
public abstract class ChunkRebuildMixin {

    @Inject(method = "setDirty", at = @At("HEAD"))
    private void kinetic$onRenderChunkActivity(CallbackInfo ci) {
        KineticChunkEngine.getInstance().notifyChunkRebuildStarted();
    }
}
