package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

/**
 * Master Phase 3 coordinator for OpenGL state deduplication, frustum culling,
 * and render scratch buffer management.
 */
public final class KineticRenderOptimizer {

    private static final KineticRenderOptimizer INSTANCE = new KineticRenderOptimizer();

    private KineticRenderOptimizer() {
    }

    public static KineticRenderOptimizer getInstance() {
        return INSTANCE;
    }

    public void onFrameStart() {
        if (!KineticConfig.modEnabled) {
            return;
        }
        GlStateCache.onFrameStart();
        FastFrustumCuller.getInstance().onFrameStart();
    }

    public int getSavedGlCallsPerSecond() {
        return GlStateCache.getSavedCallsPerSecond();
    }

    public int getCulledRenderObjectsPerSecond() {
        return FastFrustumCuller.getInstance().getCulledObjectsPerSecond();
    }
}
