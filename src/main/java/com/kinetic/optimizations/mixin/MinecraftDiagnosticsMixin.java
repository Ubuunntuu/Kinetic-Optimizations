package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.chunks.KineticChunkEngine;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import com.kinetic.optimizations.rendering.KineticRenderOptimizer;
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
    }

    @Inject(method = "updateDisplay", at = @At("RETURN"))
    private void kinetic$onFrameDisplayEnd(CallbackInfo ci) {
        KineticDiagnosticsEngine.getInstance().onFrameCompleted();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void kinetic$onClientTickStart(CallbackInfo ci) {
        KineticChunkEngine.getInstance().onClientTick();
    }
}
