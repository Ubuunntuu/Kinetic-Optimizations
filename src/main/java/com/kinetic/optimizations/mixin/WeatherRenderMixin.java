package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.rendering.WeatherRenderOptimizer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class WeatherRenderMixin {

    @Inject(method = "renderSnowAndRain(F)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$protectFramePacingInHeavyWeather(float tickDelta, CallbackInfo ci) {
        if (WeatherRenderOptimizer.getInstance().shouldSkipHeavyWeatherPass()) {
            ci.cancel();
        }
    }
}
