package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.rendering.KineticRenderOptimizer;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.BufferUploader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferUploader.class)
public abstract class BufferUploaderMixin {

    @Inject(method = "end", at = @At("HEAD"), cancellable = true)
    private void kinetic$skipEmptyBufferUpload(BufferBuilder builder, CallbackInfo ci) {
        if (builder != null && KineticRenderOptimizer.getInstance().getBufferUploadOptimizer().shouldSkipBufferUpload(builder.getVertexCount())) {
            ci.cancel();
        }
    }
}
