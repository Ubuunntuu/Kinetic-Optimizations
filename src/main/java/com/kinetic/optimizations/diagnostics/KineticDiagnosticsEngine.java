package com.kinetic.optimizations.diagnostics;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.performance.FrameTimeTracker;
import com.kinetic.optimizations.performance.KineticBenchmarkSession;
import com.kinetic.optimizations.performance.MemoryAndGcTracker;
import com.kinetic.optimizations.performance.WorldRenderStats;
import com.kinetic.optimizations.utilities.KineticLogger;

/**
 * Central coordinator for performance monitoring, bottleneck diagnostics, and 60s benchmark sessions.
 */
public final class KineticDiagnosticsEngine {

    private static final KineticDiagnosticsEngine INSTANCE = new KineticDiagnosticsEngine();

    private final FrameTimeTracker frameTimeTracker = new FrameTimeTracker();
    private final MemoryAndGcTracker memoryAndGcTracker = new MemoryAndGcTracker();
    private final WorldRenderStats worldRenderStats = new WorldRenderStats();
    private final BottleneckAnalyzer bottleneckAnalyzer = new BottleneckAnalyzer();
    private final KineticBenchmarkSession benchmarkSession = new KineticBenchmarkSession();

    private long lastPeriodicLogNs = System.nanoTime();
    private static final long PERIODIC_LOG_INTERVAL_NS = 30_000_000_000L;

    private KineticDiagnosticsEngine() {
    }

    public static KineticDiagnosticsEngine getInstance() {
        return INSTANCE;
    }

    public void onFrameCompleted() {
        if (!KineticConfig.modEnabled) {
            return;
        }
        frameTimeTracker.onFrameBoundary();
        memoryAndGcTracker.updateIfDue();
        worldRenderStats.onFrameTick();
        bottleneckAnalyzer.evaluate(frameTimeTracker, memoryAndGcTracker, worldRenderStats);
        benchmarkSession.onFrame(memoryAndGcTracker, worldRenderStats);

        if (KineticConfig.debugLogging) {
            long now = System.nanoTime();
            if (now - lastPeriodicLogNs >= PERIODIC_LOG_INTERVAL_NS) {
                lastPeriodicLogNs = now;
                KineticLogger.debug(
                    DiagnosticReportGenerator.buildCompactHudLine(
                        frameTimeTracker, memoryAndGcTracker, worldRenderStats, bottleneckAnalyzer
                    )
                );
            }
        }
    }

    public String getFormattedSummaryLine() {
        return DiagnosticReportGenerator.buildCompactHudLine(
            frameTimeTracker, memoryAndGcTracker, worldRenderStats, bottleneckAnalyzer
        );
    }

    public void exportSnapshotToDisk() {
        DiagnosticReportGenerator.exportDiagnosticReport(
            frameTimeTracker, memoryAndGcTracker, worldRenderStats, bottleneckAnalyzer
        );
    }

    public FrameTimeTracker getFrameTimeTracker() {
        return frameTimeTracker;
    }

    public MemoryAndGcTracker getMemoryAndGcTracker() {
        return memoryAndGcTracker;
    }

    public WorldRenderStats getWorldRenderStats() {
        return worldRenderStats;
    }

    public BottleneckAnalyzer getBottleneckAnalyzer() {
        return bottleneckAnalyzer;
    }

    public KineticBenchmarkSession getBenchmarkSession() {
        return benchmarkSession;
    }
}
