package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks and short-circuits empty or zero-vertex BufferBuilder uploads before
 * expensive native ByteBuffer reads and OpenGL draw calls occur.
 */
public final class BufferUploadOptimizer {

    private final AtomicInteger totalUploadsAccumulator = new AtomicInteger(0);
    private final AtomicInteger skippedEmptyUploadsAccumulator = new AtomicInteger(0);

    private volatile int uploadsPerSecond = 0;
    private volatile int skippedEmptyUploadsPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    public void onFrameStart() {
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            uploadsPerSecond = totalUploadsAccumulator.getAndSet(0);
            skippedEmptyUploadsPerSecond = skippedEmptyUploadsAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    /**
     * Returns true if a buffer upload with the given vertexCount is empty and should be skipped.
     */
    public boolean shouldSkipBufferUpload(int vertexCount) {
        totalUploadsAccumulator.incrementAndGet();
        if (!KineticConfig.modEnabled || !KineticConfig.minimizeRedundantBufferUploads) {
            return false;
        }
        if (vertexCount <= 0) {
            skippedEmptyUploadsAccumulator.incrementAndGet();
            GlStateCache.onFrameStart(); // Count toward saved driver calls
            return true;
        }
        return false;
    }

    public void recordBufferUpload() {
        totalUploadsAccumulator.incrementAndGet();
    }

    public int getUploadsPerSecond() {
        return uploadsPerSecond;
    }

    public int getSkippedEmptyUploadsPerSecond() {
        return skippedEmptyUploadsPerSecond;
    }
}
