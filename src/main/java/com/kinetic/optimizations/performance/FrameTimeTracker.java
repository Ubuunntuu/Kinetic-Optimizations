package com.kinetic.optimizations.performance;

import java.util.Arrays;

/**
 * Zero-allocation ring-buffer profiler tracking real-time frame durations,
 * FPS, 1% low FPS, jitter (StdDev), and live 120-frame sparkline bars.
 */
public final class FrameTimeTracker {

    private static final int SAMPLE_CAPACITY = 512;
    private static final int SAMPLE_MASK = SAMPLE_CAPACITY - 1;
    private static final long STATS_UPDATE_INTERVAL_NS = 250_000_000L;

    private final long[] frameDurationsNs = new long[SAMPLE_CAPACITY];
    private final long[] scratchSortBuffer = new long[SAMPLE_CAPACITY];

    private int writeIndex = 0;
    private int recordedSamples = 0;
    private long lastFrameBoundaryNs = 0L;
    private long lastStatsComputeNs = 0L;

    private volatile double currentFps = 0.0;
    private volatile double averageFps = 0.0;
    private volatile double onePercentLowFps = 0.0;
    private volatile double lastFrameTimeMs = 0.0;
    private volatile double averageFrameTimeMs = 0.0;
    private volatile double onePercentHighFrameTimeMs = 0.0;
    private volatile double frameTimeStdDevMs = 0.0;

    public void onFrameBoundary() {
        long now = System.nanoTime();
        if (lastFrameBoundaryNs != 0L) {
            long durationNs = now - lastFrameBoundaryNs;
            if (durationNs > 0L && durationNs < 10_000_000_000L) {
                frameDurationsNs[writeIndex] = durationNs;
                writeIndex = (writeIndex + 1) & SAMPLE_MASK;
                if (recordedSamples < SAMPLE_CAPACITY) {
                    recordedSamples++;
                }
                lastFrameTimeMs = durationNs / 1_000_000.0;
                currentFps = 1_000_000_000.0 / durationNs;
            }
        }
        lastFrameBoundaryNs = now;

        if (now - lastStatsComputeNs >= STATS_UPDATE_INTERVAL_NS && recordedSamples >= 8) {
            recomputeWindowMetrics();
            lastStatsComputeNs = now;
        }
    }

    private void recomputeWindowMetrics() {
        final int count = recordedSamples;
        long sumNs = 0L;

        for (int i = 0; i < count; i++) {
            long sample = frameDurationsNs[i];
            scratchSortBuffer[i] = sample;
            sumNs += sample;
        }

        double meanNs = (double) sumNs / count;
        averageFrameTimeMs = meanNs / 1_000_000.0;
        averageFps = meanNs > 0.0 ? (1_000_000_000.0 / meanNs) : 0.0;

        double varianceAcc = 0.0;
        for (int i = 0; i < count; i++) {
            double diffMs = (scratchSortBuffer[i] - meanNs) / 1_000_000.0;
            varianceAcc += (diffMs * diffMs);
        }
        frameTimeStdDevMs = Math.sqrt(varianceAcc / count);

        Arrays.sort(scratchSortBuffer, 0, count);
        int tailCount = Math.max(1, count / 100);
        long tailSumNs = 0L;
        for (int i = count - tailCount; i < count; i++) {
            tailSumNs += scratchSortBuffer[i];
        }
        double worstMeanNs = (double) tailSumNs / tailCount;
        onePercentHighFrameTimeMs = worstMeanNs / 1_000_000.0;
        onePercentLowFps = worstMeanNs > 0.0 ? (1_000_000_000.0 / worstMeanNs) : 0.0;
    }

    /**
     * Zero-allocation read of a recent frame time in milliseconds (0 = oldest in window, windowSize-1 = newest).
     */
    public float getRecentFrameMs(int offsetFromNewest) {
        if (offsetFromNewest < 0 || offsetFromNewest >= recordedSamples) {
            return 0.0f;
        }
        int idx = (writeIndex - 1 - offsetFromNewest) & SAMPLE_MASK;
        return frameDurationsNs[idx] / 1_000_000.0f;
    }

    public int getSampleCount() { return recordedSamples; }

    public int getRecordedSampleCount() {
        return recordedSamples;
    }

    public void reset() {
        writeIndex = 0;
        recordedSamples = 0;
        lastFrameBoundaryNs = 0L;
    }

    public double getCurrentFps() { return currentFps; }
    public double getAverageFps() { return averageFps; }
    public double getOnePercentLowFps() { return onePercentLowFps; }
    public double getLastFrameTimeMs() { return lastFrameTimeMs; }
    public double getAverageFrameTimeMs() { return averageFrameTimeMs; }
    public double getOnePercentHighFrameTimeMs() { return onePercentHighFrameTimeMs; }
    public double getFrameTimeStdDevMs() { return frameTimeStdDevMs; }
}
