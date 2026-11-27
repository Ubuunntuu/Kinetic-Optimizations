package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.performance.FastMathEngine;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MathHelper.class)
public abstract class MathHelperFastTrigMixin {

    @Inject(method = "sin", at = @At("HEAD"), cancellable = true)
    private static void kinetic$fastSin(float radians, CallbackInfoReturnable<Float> cir) {
        if (KineticConfig.modEnabled && KineticConfig.fastMathLookup) {
            cir.setReturnValue(FastMathEngine.sin(radians));
        }
    }

    @Inject(method = "cos", at = @At("HEAD"), cancellable = true)
    private static void kinetic$fastCos(float radians, CallbackInfoReturnable<Float> cir) {
        if (KineticConfig.modEnabled && KineticConfig.fastMathLookup) {
            cir.setReturnValue(FastMathEngine.cos(radians));
        }
    }
}
