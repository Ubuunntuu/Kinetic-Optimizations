package com.kinetic.optimizations.gui;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.BottleneckType;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.performance.FrameTimeTracker;
import com.kinetic.optimizations.performance.MemoryAndGcTracker;
import com.kinetic.optimizations.performance.WorldRenderStats;
import com.kinetic.optimizations.rendering.KineticRenderOptimizer;

import java.util.Locale;

public final class PerformanceOverlayRenderer {

    private static final PerformanceOverlayRenderer INSTANCE = new PerformanceOverlayRenderer();
    private static final long HUD_REFRESH_INTERVAL_NS = 250_000_000L;

    private long lastHudRefreshNs = 0L;
    private final String[] cachedLines = new String[]{
        "Kinetic Optimizations v1.2.0",
        "FPS: -- | 1% Low: -- | Frame: -- ms",
        "RAM: --/-- MB | Alloc: -- MB/s | GC: --",
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
        boolean benchActive = engine.getBenchmarkSession().isActive();

        cachedLines[0] = String.format(
            Locale.ROOT,
            "Kinetic v1.2.0 [%s]%s (F7:Menu F8:HUD F9:Preset F10:Bench)",
            KineticConfig.activePreset.getDisplayName(),
            benchActive ? " [BENCHMARKING 60s]" : ""
        );
        cachedLines[1] = String.format(
            Locale.ROOT,
            "FPS: %.0f (Avg: %.0f | 1%% Low: %.0f) | %.2f ms (Jitter: %.2f ms)",
            frames.getCurrentFps(),
            frames.getAverageFps(),
            frames.getOnePercentLowFps(),
            frames.getLastFrameTimeMs(),
            frames.getFrameTimeStdDevMs()
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
            "GL Saved: %d/s | Culled: %d/s | Status: %s",
            renderOpt.getSavedGlCallsPerSecond(),
            renderOpt.getCulledRenderObjectsPerSecond(),
            bottleneck.getLabel()
        );
    }
}
