package com.kinetic.optimizations.performance;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe telemetry counters for entities, chunks, chunk rebuilds, and particles.
 */
public final class WorldRenderStats {

    private final AtomicInteger totalEntities = new AtomicInteger(0);
    private final AtomicInteger renderedEntities = new AtomicInteger(0);
    private final AtomicInteger culledEntities = new AtomicInteger(0);

    private final AtomicInteger visibleChunks = new AtomicInteger(0);
    private final AtomicInteger chunkRebuildsThisSecond = new AtomicInteger(0);
    private final AtomicInteger chunkRebuildAccumulator = new AtomicInteger(0);

    private final AtomicInteger activeParticles = new AtomicInteger(0);
    private final AtomicInteger culledParticles = new AtomicInteger(0);

    private long lastSecondRolloverNs = System.nanoTime();

    public void onFrameTick() {
        long now = System.nanoTime();
        if (now - lastSecondRolloverNs >= 1_000_000_000L) {
            chunkRebuildsThisSecond.set(chunkRebuildAccumulator.getAndSet(0));
            lastSecondRolloverNs = now;
        }
    }

    public void recordChunkRebuild() {
        chunkRebuildAccumulator.incrementAndGet();
    }

    public void setEntityCounts(int total, int rendered, int culled) {
        totalEntities.set(total);
        renderedEntities.set(rendered);
        culledEntities.set(culled);
    }

    public void setVisibleChunks(int visible) {
        visibleChunks.set(visible);
    }

    public void setParticleCounts(int active, int culled) {
        activeParticles.set(active);
        culledParticles.set(culled);
    }

    public int getTotalEntities() {
        return totalEntities.get();
    }

    public int getRenderedEntities() {
        return renderedEntities.get();
    }

    public int getCulledEntities() {
        return culledEntities.get();
    }

    public int getVisibleChunks() {
        return visibleChunks.get();
    }

    public int getChunkRebuildsPerSecond() {
        return chunkRebuildsThisSecond.get();
    }

    public int getActiveParticles() {
        return activeParticles.get();
    }

    public int getCulledParticles() {
        return culledParticles.get();
    }
}
