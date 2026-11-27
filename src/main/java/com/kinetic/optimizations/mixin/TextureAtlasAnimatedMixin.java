package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.rendering.AnimatedSpriteOptimizer;
import net.minecraft.client.render.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasAnimatedMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void kinetic$throttleAnimatedSprites(CallbackInfo ci) {
        if (AnimatedSpriteOptimizer.getInstance().shouldSkipAtlasAnimationTick()) {
            ci.cancel();
        }
    }
}
