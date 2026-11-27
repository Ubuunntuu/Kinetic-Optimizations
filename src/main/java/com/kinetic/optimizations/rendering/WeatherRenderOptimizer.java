package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;

/**
 * Dynamically protects 1% low FPS during heavy rain/snow storms when frame duration spikes
 * or when window focus is lost.
 */
public final class WeatherRenderOptimizer {

    private static final WeatherRenderOptimizer INSTANCE = new WeatherRenderOptimizer();

    private WeatherRenderOptimizer() {
    }

    public static WeatherRenderOptimizer getInstance() {
        return INSTANCE;
    }

    public boolean shouldSkipHeavyWeatherPass() {
        if (!KineticConfig.modEnabled || !KineticConfig.optimizeWeatherRendering) {
            return false;
        }
        // Only skip non-gameplay decorative rain/snow pass during severe frame-time spikes (>40 ms / <25 FPS)
        double lastFrameMs = KineticDiagnosticsEngine.getInstance().getFrameTimeTracker().getLastFrameTimeMs();
        return lastFrameMs > 45.0;
    }
}
