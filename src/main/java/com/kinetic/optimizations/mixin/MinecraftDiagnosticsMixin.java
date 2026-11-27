package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.chunks.KineticChunkEngine;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.entities.EntityCollisionThrottler;
import com.kinetic.optimizations.entities.ItemAndXpRenderThrottler;
import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import com.kinetic.optimizations.gui.KineticKeybindController;
import com.kinetic.optimizations.lighting.LightingDeduplicator;
import com.kinetic.optimizations.memory.KineticCpuMemoryEngine;
import com.kinetic.optimizations.performance.UnfocusedFpsLimiter;
import com.kinetic.optimizations.rendering.KineticRenderOptimizer;
import com.kinetic.optimizations.rendering.TextWidthCache;
import com.kinetic.optimizations.resources.ResourceCacheOptimizer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftDiagnosticsMixin {

    @Inject(method = "updateDisplay", at = @At("HEAD"))
    private void kinetic$onFrameDisplayStart(CallbackInfo ci) {
        KineticRenderOptimizer.getInstance().onFrameStart();
        KineticChunkEngine.getInstance().onFrameStart();
        KineticEntityParticleEngine.getInstance().onFrameStart();
        ItemAndXpRenderThrottler.getInstance().onFrameStart();
        TextWidthCache.getInstance().onFrameStart();
    }

    @Inject(method = "updateDisplay", at = @At("RETURN"))
    private void kinetic$onFrameDisplayEnd(CallbackInfo ci) {
        KineticDiagnosticsEngine.getInstance().onFrameCompleted();
        UnfocusedFpsLimiter.getInstance().throttleIfWindowUnfocused();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void kinetic$onClientTickStart(CallbackInfo ci) {
        KineticChunkEngine.getInstance().onClientTick();
        KineticCpuMemoryEngine.getInstance().onClientTick();
        LightingDeduplicator.getInstance().onTickRollover();
        EntityCollisionThrottler.getInstance().onTickRollover();
        KineticKeybindController.getInstance().onClientTick();
    }

    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void kinetic$onResourceReload(CallbackInfo ci) {
        ResourceCacheOptimizer.getInstance().onResourceReload();
    }

    @Inject(method = "setWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"), require = 0) private void kinetic$onClientWorldChanged(CallbackInfo ci) { com.kinetic.optimizations.resources.WorldSwitchCleaner.onWorldChanged(); }
}
