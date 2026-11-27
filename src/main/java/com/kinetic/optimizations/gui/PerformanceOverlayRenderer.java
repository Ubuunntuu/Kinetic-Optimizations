package com.kinetic.optimizations.gui;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.BottleneckType;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.performance.FrameTimeTracker;
import com.kinetic.optimizations.performance.MemoryAndGcTracker;
import com.kinetic.optimizations.performance.WorldRenderStats;
import com.kinetic.optimizations.rendering.KineticRenderOptimizer;

import java.util.Locale;

/**
 * Builds low-overhead HUD overlay strings for in-game rendering without per-frame heap churn
 * when the overlay is disabled, and updates at 4 Hz when enabled.
 */
public final class PerformanceOverlayRenderer {

    private static final PerformanceOverlayRenderer INSTANCE = new PerformanceOverlayRenderer();
    private static final long HUD_REFRESH_INTERVAL_NS = 250_000_000L; // 250 ms (4 Hz)

    private long lastHudRefreshNs = 0L;
    private final String[] cachedLines = new String[]{
        "Kinetic Optimizations v0.3.0-alpha",
        "FPS: -- | 1% Low: -- | Frame: -- ms",
        "Mem: --/-- MB | Alloc: -- MB/s | GC: --",
        "Chunks: -- | Entities: -- | Particles: --",
        "GL Saved: --/s | Culled: --/s | Status: Optimal"
    };

    private PerformanceOverlayRenderer() {
    }

    public static PerformanceOverlayRenderer getInstance() {
        return INSTANCE;
    }

    public String[] getOverlayLines() {
        if (!KineticConfig.showPerformanceOverlay) {
            return new String[0];
        }

        long now = System.nanoTime();
        if (now - lastHudRefreshNs >= HUD_REFRESH_INTERVAL_NS) {
            lastHudRefreshNs = now;
            refreshCachedLines();
        }
        return cachedLines;
    }

    private void refreshCachedLines() {
        KineticDiagnosticsEngine engine = KineticDiagnosticsEngine.getInstance();
        KineticRenderOptimizer renderOpt = KineticRenderOptimizer.getInstance();
        FrameTimeTracker frames = engine.getFrameTimeTracker();
        MemoryAndGcTracker mem = engine.getMemoryAndGcTracker();
        WorldRenderStats world = engine.getWorldRenderStats();
        BottleneckType bottleneck = engine.getBottleneckAnalyzer().getActiveBottleneck();

        cachedLines[0] = String.format(
            Locale.ROOT,
            "Kinetic Optimizations v0.3.0 [%s]",
            KineticConfig.activePreset.getDisplayName()
        );
        cachedLines[1] = String.format(
            Locale.ROOT,
            "FPS: %.0f (Avg: %.0f | 1%% Low: %.0f) | %.2f ms",
            frames.getCurrentFps(),
            frames.getAverageFps(),
            frames.getOnePercentLowFps(),
            frames.getLastFrameTimeMs()
        );
        cachedLines[2] = String.format(
            Locale.ROOT,
            "RAM: %d/%d MB (%.0f%%) | Alloc: %.1f MB/s | GC: %d (%d ms)",
            mem.getUsedHeapMb(),
            mem.getMaxHeapMb(),
            mem.getHeapUsagePercent(),
            mem.getAllocationRateMbPerSec(),
            mem.getTotalGcCollections(),
            mem.getTotalGcTimeMs()
        );
        cachedLines[3] = String.format(
            Locale.ROOT,
            "Chunks: %d (%d/s) | Ent: %d/%d | Part: %d",
            world.getVisibleChunks(),
            world.getChunkRebuildsPerSecond(),
            world.getRenderedEntities(),
            world.getTotalEntities(),
            world.getActiveParticles()
        );
        cachedLines[4] = String.format(
            Locale.ROOT,
            "GL Saved: %d/s | Culled: %d/s | Bottleneck: %s",
            renderOpt.getSavedGlCallsPerSecond(),
            renderOpt.getCulledRenderObjectsPerSecond(),
            bottleneck.getLabel()
        );
    }
}
