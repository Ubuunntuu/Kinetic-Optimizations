package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.chunks.KineticChunkEngine;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class ChunkRebuildMixin {

    @Inject(method = "reload", at = @At("HEAD"))
    private void kinetic$onWorldRendererTick(CallbackInfo ci) {
        KineticChunkEngine.getInstance().onClientTick();
    }
}
