package com.kinetic.optimizations.performance;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.rendering.KineticRenderOptimizer;
import com.kinetic.optimizations.utilities.KineticLogger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

/**
 * Repeatable 60-second in-game benchmark session recorder.
 * Captures every frame duration during the run, computes exact Average FPS, 1% Low FPS,
 * 0.1% Low FPS, Frame-Time StdDev, Allocation Rate, and GC deltas, and appends a row to CSV.
 */
public final class KineticBenchmarkSession {

    private static final long BENCHMARK_DURATION_NS = 60_000_000_000L; // 60 seconds
    private static final int MAX_CAPTURED_FRAMES = 120_000;

    private final long[] capturedFramesNs = new long[MAX_CAPTURED_FRAMES];
    private boolean active = false;
    private long startTimestampNs = 0L;
    private long lastFrameNs = 0L;
    private int capturedCount = 0;
    private long startGcCount = 0L;
    private long startGcTimeMs = 0L;
    private double allocRateSum = 0.0;
    private int allocRateSamples = 0;

    public void startSession(MemoryAndGcTracker memTracker) {
        if (active) {
            return;
        }
        active = true;
        capturedCount = 0;
        startTimestampNs = System.nanoTime();
        lastFrameNs = startTimestampNs;
        startGcCount = memTracker.getTotalGcCollections();
        startGcTimeMs = memTracker.getTotalGcTimeMs();
        allocRateSum = 0.0;
        allocRateSamples = 0;
        KineticLogger.info("Started 60-second repeatable benchmark session [Preset: %s]...",
            KineticConfig.activePreset.getDisplayName());
    }

    public void onFrame(MemoryAndGcTracker memTracker, WorldRenderStats worldStats) {
        if (!active) {
            if (KineticConfig.runSixtySecondBenchmark) {
                KineticConfig.runSixtySecondBenchmark = false;
                KineticConfig.save();
                startSession(memTracker);
            }
            return;
        }

        long now = System.nanoTime();
        long frameDurationNs = now - lastFrameNs;
        lastFrameNs = now;

        if (frameDurationNs > 0L && capturedCount < MAX_CAPTURED_FRAMES) {
            capturedFramesNs[capturedCount++] = frameDurationNs;
        }

        double currentAlloc = memTracker.getAllocationRateMbPerSec();
        if (currentAlloc > 0.0) {
            allocRateSum += currentAlloc;
            allocRateSamples++;
        }

        if (now - startTimestampNs >= BENCHMARK_DURATION_NS || capturedCount >= MAX_CAPTURED_FRAMES) {
            finishAndExport(memTracker, worldStats);
        }
    }

    private void finishAndExport(MemoryAndGcTracker memTracker, WorldRenderStats worldStats) {
        active = false;
        if (capturedCount < 10) {
            KineticLogger.warn("Benchmark session ended with insufficient frame samples (%d).", capturedCount);
            return;
        }

        long sumNs = 0L;
        for (int i = 0; i < capturedCount; i++) {
            sumNs += capturedFramesNs[i];
        }
        double meanMs = (sumNs / (double) capturedCount) / 1_000_000.0;
        double avgFps = meanMs > 0.0 ? (1000.0 / meanMs) : 0.0;

        double varianceSum = 0.0;
        for (int i = 0; i < capturedCount; i++) {
            double diffMs = (capturedFramesNs[i] / 1_000_000.0) - meanMs;
            varianceSum += (diffMs * diffMs);
        }
        double stdDevMs = Math.sqrt(varianceSum / capturedCount);

        long[] sorted = Arrays.copyOf(capturedFramesNs, capturedCount);
        Arrays.sort(sorted);

        int onePctCount = Math.max(1, capturedCount / 100);
        long slowestSumNs = 0L;
        for (int i = capturedCount - onePctCount; i < capturedCount; i++) {
            slowestSumNs += sorted[i];
        }
        double onePctMs = (slowestSumNs / (double) onePctCount) / 1_000_000.0;
        double onePctLowFps = onePctMs > 0.0 ? (1000.0 / onePctMs) : 0.0;

        long gcCountDelta = Math.max(0L, memTracker.getTotalGcCollections() - startGcCount);
        long gcTimeDeltaMs = Math.max(0L, memTracker.getTotalGcTimeMs() - startGcTimeMs);
        double avgAllocMbSec = allocRateSamples > 0 ? (allocRateSum / allocRateSamples) : 0.0;

        Path csvPath = FabricLoader.getInstance().getConfigDir().resolve("kinetic_ingame_benchmarks.csv");
        boolean writeHeader = !Files.exists(csvPath);

        String row = String.format(
            Locale.ROOT,
            "%s,%s,%d,%.2f,%.2f,%.3f,%.3f,%.1f,%d,%d,%d,%d\n",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            KineticConfig.activePreset.name(),
            capturedCount,
            avgFps,
            onePctLowFps,
            meanMs,
            stdDevMs,
            avgAllocMbSec,
            gcCountDelta,
            gcTimeDeltaMs,
            KineticRenderOptimizer.getInstance().getSavedGlCallsPerSecond(),
            worldStats.getChunkRebuildsPerSecond()
        );

        try {
            Files.createDirectories(csvPath.getParent());
            if (writeHeader) {
                String header = "timestamp,preset,frames,avg_fps,one_percent_low_fps,avg_frame_ms,stddev_jitter_ms,avg_alloc_mb_s,gc_count_delta,gc_time_delta_ms,saved_gl_calls_s,chunk_rebuilds_s\n";
                Files.writeString(csvPath, header, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            Files.writeString(csvPath, row, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            KineticLogger.info("Benchmark Complete [%s]: Avg FPS=%.1f | 1%% Low=%.1f | Frame=%.2fms (Jitter=%.2fms) | GC=%d (%dms) -> Saved to %s",
                KineticConfig.activePreset.getDisplayName(), avgFps, onePctLowFps, meanMs, stdDevMs, gcCountDelta, gcTimeDeltaMs, csvPath.getFileName());
        } catch (IOException e) {
            KineticLogger.error("Failed to export benchmark CSV", e);
        }
    }

    public boolean isActive() {
        return active;
    }
}
