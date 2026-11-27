package com.kinetic.optimizations.entities;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.particles.ParticleEngineOptimizer;
import com.kinetic.optimizations.performance.WorldRenderStats;

/**
 * Master Phase 5 coordinator for Entity, BlockEntity, and Particle optimizations.
 * Publishes per-frame counters to WorldRenderStats before reset.
 */
public final class KineticEntityParticleEngine {

    private static final KineticEntityParticleEngine INSTANCE = new KineticEntityParticleEngine();

    private final EntityVisibilityOptimizer entityOptimizer = new EntityVisibilityOptimizer();
    private final BlockEntityCullingOptimizer blockEntityOptimizer = new BlockEntityCullingOptimizer();
    private final ParticleEngineOptimizer particleOptimizer = new ParticleEngineOptimizer();

    private KineticEntityParticleEngine() {
    }

    public static KineticEntityParticleEngine getInstance() {
        return INSTANCE;
    }

    public void onFrameStart() {
        if (!KineticConfig.modEnabled) {
            return;
        }
        WorldRenderStats stats = KineticDiagnosticsEngine.getInstance().getWorldRenderStats();
        stats.setEntityCounts(
            entityOptimizer.getTotalEvaluatedThisFrame(),
            entityOptimizer.getRenderedThisFrame(),
            entityOptimizer.getCulledThisFrame()
        );
        stats.setParticleCounts(
            particleOptimizer.getActiveParticleCount(),
            particleOptimizer.getCulledParticlesThisFrame()
        );

        entityOptimizer.onFrameStart();
        blockEntityOptimizer.onFrameStart();
        particleOptimizer.onFrameStart();
    }

    public EntityVisibilityOptimizer getEntityOptimizer() {
        return entityOptimizer;
    }

    public BlockEntityCullingOptimizer getBlockEntityOptimizer() {
        return blockEntityOptimizer;
    }

    public ParticleEngineOptimizer getParticleOptimizer() {
        return particleOptimizer;
    }
}
