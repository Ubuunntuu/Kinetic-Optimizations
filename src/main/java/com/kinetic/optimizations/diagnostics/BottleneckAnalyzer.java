package com.kinetic.optimizations.diagnostics;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.performance.FrameTimeTracker;
import com.kinetic.optimizations.performance.MemoryAndGcTracker;
import com.kinetic.optimizations.performance.WorldRenderStats;

/**
 * Automatic bottleneck detector that classifies the primary performance limiter
 * and provides actionable recommendations without altering settings automatically.
 */
public final class BottleneckAnalyzer {

    private volatile BottleneckType activeBottleneck = BottleneckType.NONE;
    private long lastEvaluationNs = 0L;
    private static final long EVALUATION_INTERVAL_NS = 1_000_000_000L; // 1 second

    public void evaluate(FrameTimeTracker frames, MemoryAndGcTracker memory, WorldRenderStats world) {
        if (!KineticConfig.enableBottleneckAnalyzer) {
            activeBottleneck = BottleneckType.NONE;
            return;
        }

        long now = System.nanoTime();
        if (now - lastEvaluationNs < EVALUATION_INTERVAL_NS) {
            return;
        }
        lastEvaluationNs = now;

        if (frames.getSampleCount() < 60) {
            return;
        }

        // 1. Check Memory Pressure & GC Pauses
        if (memory.getHeapUsagePercent() > 85.0) {
            activeBottleneck = BottleneckType.MEMORY_PRESSURE;
            return;
        }
        if (memory.getRecentGcTimeDeltaMs() > 25L || memory.getAllocationRateMbPerSec() > 300.0) {
            activeBottleneck = BottleneckType.EXCESSIVE_GC;
            return;
        }

        // 2. Check World Subsystem Bottlenecks
        if (world.getChunkRebuildsPerSecond() > 24 && frames.getOnePercentLowFps() < frames.getAverageFps() * 0.55) {
            activeBottleneck = BottleneckType.CHUNK_REBUILD_SPIKE;
            return;
        }
        if (world.getRenderedEntities() > 250) {
            activeBottleneck = BottleneckType.EXCESSIVE_ENTITIES;
            return;
        }
        if (world.getActiveParticles() > 2500) {
            activeBottleneck = BottleneckType.EXCESSIVE_PARTICLES;
            return;
        }

        // 3. Check CPU Frame Pacing Variance vs GPU Bound
        double avgFps = frames.getAverageFps();
        double lowFps = frames.getOnePercentLowFps();
        double stdDevMs = frames.getFrameTimeStdDevMs();

        if (stdDevMs > 6.5 && lowFps < avgFps * 0.50) {
            activeBottleneck = BottleneckType.CPU_MAIN_THREAD_BOUND;
            return;
        }

        if (avgFps < 60.0 && stdDevMs < 2.5 && memory.getProcessCpuLoadPercent() > 0.0 && memory.getProcessCpuLoadPercent() < 35.0) {
            activeBottleneck = BottleneckType.GPU_OR_DRIVER_BOUND;
            return;
        }

        activeBottleneck = BottleneckType.NONE;
    }

    public BottleneckType getActiveBottleneck() {
        return activeBottleneck;
    }
}
