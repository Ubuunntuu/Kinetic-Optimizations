package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftDiagnosticsMixin {

    @Inject(method = "runGameLoop", at = @At("RETURN"), require = 0)
    private void kinetic$onFrameLoopIteration(CallbackInfo ci) {
        KineticDiagnosticsEngine.getInstance().onFrameCompleted();
    }
}
