package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.rendering.PrimitiveQuadSorter;
import net.minecraft.client.render.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderSortMixin {

    @Shadow
    public abstract int getVertexCount();

    @Inject(method = "sortQuads(FFF)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$skipRedundantTranslucentQuadSort(float camX, float camY, float camZ, CallbackInfo ci) {
        if (PrimitiveQuadSorter.getInstance().shouldSkipRedundantSort(camX, camY, camZ, this.getVertexCount())) {
            ci.cancel();
        }
    }
}
