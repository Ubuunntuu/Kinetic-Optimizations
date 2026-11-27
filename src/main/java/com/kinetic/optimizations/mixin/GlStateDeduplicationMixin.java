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

    @Inject(method = "enableBlend", at = @At("HEAD"), cancellable = true)
    private static void kinetic$deduplicateEnableBlend(CallbackInfo ci) {
        if (GlStateCache.shouldSkipBlendChange(true)) {
            ci.cancel();
        }
    }

    @Inject(method = "disableBlend", at = @At("HEAD"), cancellable = true)
    private static void kinetic$deduplicateDisableBlend(CallbackInfo ci) {
        if (GlStateCache.shouldSkipBlendChange(false)) {
            ci.cancel();
        }
    }

    @Inject(method = "enableCull", at = @At("HEAD"), cancellable = true)
    private static void kinetic$deduplicateEnableCull(CallbackInfo ci) {
        if (GlStateCache.shouldSkipCullChange(true)) {
            ci.cancel();
        }
    }

    @Inject(method = "disableCull", at = @At("HEAD"), cancellable = true)
    private static void kinetic$deduplicateDisableCull(CallbackInfo ci) {
        if (GlStateCache.shouldSkipCullChange(false)) {
            ci.cancel();
        }
    }
}
