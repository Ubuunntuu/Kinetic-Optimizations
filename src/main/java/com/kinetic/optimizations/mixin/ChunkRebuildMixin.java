package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.chunks.KineticChunkEngine;
import net.minecraft.client.render.world.GlListRenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlListRenderChunk.class)
public abstract class ChunkRebuildMixin {

    @Inject(method = "delete", at = @At("HEAD"))
    private void kinetic$onChunkRendererHook(CallbackInfo ci) {
        KineticChunkEngine.getInstance().onClientTick();
    }
}
