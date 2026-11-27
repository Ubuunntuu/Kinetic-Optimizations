package com.kinetic.optimizations.chunks;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;

/**
 * Central Phase 4 Chunk Engine coordinating rebuild deduplication,
 * frame-paced GPU buffer upload budgeting, and sub-chunk visibility culling.
 */
public final class KineticChunkEngine {

    private static final KineticChunkEngine INSTANCE = new KineticChunkEngine();

    private final ChunkRebuildDeduplicator deduplicator = new ChunkRebuildDeduplicator();
    private final ChunkUploadBudgeter uploadBudgeter = new ChunkUploadBudgeter();
    private final ChunkVisibilityOptimizer visibilityOptimizer = new ChunkVisibilityOptimizer();

    private KineticChunkEngine() {
    }

    public static KineticChunkEngine getInstance() {
        return INSTANCE;
    }

    public void onFrameStart() {
        if (!KineticConfig.modEnabled) {
            return;
        }
        uploadBudgeter.onFrameStart();
    }

    public void onClientTick() {
        if (!KineticConfig.modEnabled) {
            return;
        }
        deduplicator.onTickRollover();
    }

    public boolean shouldScheduleChunkRebuild(int originX, int originY, int originZ) {
        boolean allowed = deduplicator.shouldScheduleRebuild(originX >> 4, originY >> 4, originZ >> 4);
        if (allowed) {
            KineticDiagnosticsEngine.getInstance().getWorldRenderStats().recordChunkRebuild();
        }
        return allowed;
    }

    public void notifyChunkRebuildStarted() {
        KineticDiagnosticsEngine.getInstance().getWorldRenderStats().recordChunkRebuild();
    }

    public ChunkRebuildDeduplicator getDeduplicator() {
        return deduplicator;
    }

    public ChunkUploadBudgeter getUploadBudgeter() {
        return uploadBudgeter;
    }

    public ChunkVisibilityOptimizer getVisibilityOptimizer() {
        return visibilityOptimizer;
    }
}
