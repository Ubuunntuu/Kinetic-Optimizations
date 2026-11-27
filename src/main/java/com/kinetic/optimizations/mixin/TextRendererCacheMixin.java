package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.rendering.TextWidthCache;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class TextRendererCacheMixin {

    @Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void kinetic$checkStringWidthCache(String text, CallbackInfoReturnable<Integer> cir) {
        int cached = TextWidthCache.getInstance().getCachedWidth(text);
        if (cached >= 0) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("RETURN"))
    private void kinetic$populateStringWidthCache(String text, CallbackInfoReturnable<Integer> cir) {
        Integer result = cir.getReturnValue();
        if (result != null && result >= 0) {
            TextWidthCache.getInstance().putCachedWidth(text, result);
        }
    }
}
