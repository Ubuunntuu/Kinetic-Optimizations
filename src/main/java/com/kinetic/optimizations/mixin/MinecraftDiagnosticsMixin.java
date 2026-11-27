package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.rendering.KineticRenderOptimizer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftDiagnosticsMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void kinetic$onFrameLoopStart(CallbackInfo ci) {
        KineticRenderOptimizer.getInstance().onFrameStart();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void kinetic$onFrameLoopIteration(CallbackInfo ci) {
        KineticDiagnosticsEngine.getInstance().onFrameCompleted();
    }
}
