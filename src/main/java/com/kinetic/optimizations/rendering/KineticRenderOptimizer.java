package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

/**
 * Master Phase 3 & Phase 7 coordinator for OpenGL state deduplication, frustum culling,
 * buffer upload optimization, and render scratch buffer management.
 */
public final class KineticRenderOptimizer {

    private static final KineticRenderOptimizer INSTANCE = new KineticRenderOptimizer();
    private final BufferUploadOptimizer bufferUploadOptimizer = new BufferUploadOptimizer();

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
        bufferUploadOptimizer.onFrameStart();
        PrimitiveQuadSorter.getInstance().onFrameStart();
        SkyAndCloudCuller.getInstance().onFrameStart();
    }

    public int getSavedGlCallsPerSecond() {
        return GlStateCache.getSavedCallsPerSecond() + bufferUploadOptimizer.getSkippedEmptyUploadsPerSecond();
    }

    public int getCulledRenderObjectsPerSecond() {
        return FastFrustumCuller.getInstance().getCulledObjectsPerSecond();
    }

    public BufferUploadOptimizer getBufferUploadOptimizer() {
        return bufferUploadOptimizer;
    }
}
