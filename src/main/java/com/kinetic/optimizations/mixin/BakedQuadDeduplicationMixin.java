package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.resources.ModelVertexDeduplicator;
import net.minecraft.client.render.model.block.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedQuad.class)
public abstract class BakedQuadDeduplicationMixin {

    @Shadow
    @Final
    @Mutable
    protected int[] vertices;

    @Inject(method = "<init>*", at = @At("RETURN"), require = 0)
    private void kinetic$canonicalizeQuadVertexArray(CallbackInfo ci) {
        this.vertices = ModelVertexDeduplicator.getInstance().canonicalizeVertices(this.vertices);
    }
}
