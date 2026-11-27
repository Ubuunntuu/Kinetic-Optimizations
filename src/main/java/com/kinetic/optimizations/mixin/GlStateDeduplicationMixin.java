package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.rendering.GlStateCache;
import net.minecraft.client.render.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public abstract class GlStateDeduplicationMixin {

    @Inject(method = "activeTexture", at = @At("HEAD"))
    private static void kinetic$trackActiveTextureUnit(int glTextureEnum, CallbackInfo ci) {
        GlStateCache.onActiveTextureUnitChanged(glTextureEnum);
    }

    @Inject(method = "bindTexture", at = @At("HEAD"), cancellable = true)
    private static void kinetic$deduplicateBindTexture(int textureId, CallbackInfo ci) {
        if (GlStateCache.shouldSkipBindTexture(textureId)) {
            ci.cancel();
        }
    }

    @Inject(method = "deleteTextures", at = @At("HEAD"))
    private static void kinetic$invalidateDeletedTexture(int textureId, CallbackInfo ci) {
        GlStateCache.invalidateTexture(textureId);
    }
}
