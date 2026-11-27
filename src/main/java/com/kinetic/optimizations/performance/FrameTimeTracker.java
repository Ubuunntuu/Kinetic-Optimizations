package com.kinetic.optimizations.performance;

import java.util.Arrays;

/**
 * Zero-allocation ring buffer tracking frame times, average FPS, 1% low FPS, and frame-time jitter.
 */
public final class FrameTimeTracker {

    private static final int SAMPLE_CAPACITY = 512;
    private static final int MASK = SAMPLE_CAPACITY - 1;

    private final long[] frameDurationsNs = new long[SAMPLE_CAPACITY];
    private final long[] scratchSortBuffer = new long[SAMPLE_CAPACITY];

    private int writeIndex = 0;
    private int sampleCount = 0;
    private long lastFrameTimestampNs = 0L;

    private volatile double currentFps = 0.0;
    private volatile double averageFps = 0.0;
    private volatile double onePercentLowFps = 0.0;
    private volatile double lastFrameTimeMs = 0.0;
    private volatile double averageFrameTimeMs = 0.0;
    private volatile double onePercentHighFrameTimeMs = 0.0;
    private volatile double frameTimeStdDevMs = 0.0;

    private long lastMetricsRecalcNs = 0L;
    private static final long RECALC_INTERVAL_NS = 250_000_000L; // 250 ms

    public void onFrameBoundary() {
        long now = System.nanoTime();
        if (lastFrameTimestampNs != 0L) {
            long durationNs = now - lastFrameTimestampNs;
            if (durationNs > 0L && durationNs < 10_000_000_000L) {
                frameDurationsNs[writeIndex] = durationNs;
                writeIndex = (writeIndex + 1) & MASK;
                if (sampleCount < SAMPLE_CAPACITY) {
                    sampleCount++;
                }

                double frameMs = durationNs / 1_000_000.0;
                this.lastFrameTimeMs = frameMs;
                this.currentFps = frameMs > 0.0 ? (1000.0 / frameMs) : 0.0;

                if (now - lastMetricsRecalcNs >= RECALC_INTERVAL_NS) {
                    recalculateWindowMetrics();
                    lastMetricsRecalcNs = now;
                }
            }
        }
        lastFrameTimestampNs = now;
    }

    private void recalculateWindowMetrics() {
        int count = this.sampleCount;
        if (count <= 0) {
            return;
        }

        long sumNs = 0L;
        for (int i = 0; i < count; i++) {
            long val = frameDurationsNs[i];
            scratchSortBuffer[i] = val;
            sumNs += val;
        }

        double meanNs = (double) sumNs / count;
        double meanMs = meanNs / 1_000_000.0;
        this.averageFrameTimeMs = meanMs;
        this.averageFps = meanMs > 0.0 ? (1000.0 / meanMs) : 0.0;

        // Variance & standard deviation for frame-time consistency
        double varianceSum = 0.0;
        for (int i = 0; i < count; i++) {
            double diffMs = (scratchSortBuffer[i] / 1_000_000.0) - meanMs;
            varianceSum += (diffMs * diffMs);
        }
        this.frameTimeStdDevMs = Math.sqrt(varianceSum / count);

        // 1% low FPS = average of the slowest 1% frame durations in the window
        Arrays.sort(scratchSortBuffer, 0, count);
        int slowestCount = Math.max(1, count / 100);
        long slowestSumNs = 0L;
        for (int i = count - slowestCount; i < count; i++) {
            slowestSumNs += scratchSortBuffer[i];
        }
        double worstMeanMs = (slowestSumNs / (double) slowestCount) / 1_000_000.0;
        this.onePercentHighFrameTimeMs = worstMeanMs;
        this.onePercentLowFps = worstMeanMs > 0.0 ? (1000.0 / worstMeanMs) : 0.0;
    }

    public void reset() {
        this.writeIndex = 0;
        this.sampleCount = 0;
        this.lastFrameTimestampNs = 0L;
        Arrays.fill(frameDurationsNs, 0L);
    }

    public double getCurrentFps() {
        return currentFps;
    }

    public double getAverageFps() {
        return averageFps;
    }

    public double getOnePercentLowFps() {
        return onePercentLowFps;
    }

    public double getLastFrameTimeMs() {
        return lastFrameTimeMs;
    }

    public double getAverageFrameTimeMs() {
        return averageFrameTimeMs;
    }

    public double getOnePercentHighFrameTimeMs() {
        return onePercentHighFrameTimeMs;
    }

    public double getFrameTimeStdDevMs() {
        return frameTimeStdDevMs;
    }

    public int getSampleCount() {
        return sampleCount;
    }
}
