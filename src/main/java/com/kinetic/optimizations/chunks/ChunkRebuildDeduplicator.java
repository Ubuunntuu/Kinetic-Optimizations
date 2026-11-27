package com.kinetic.optimizations.chunks;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zero-allocation primitive 64-bit open-addressed hash table that deduplicates
 * redundant chunk section rebuild requests within the same frame/tick window.
 */
public final class ChunkRebuildDeduplicator {

    private static final int TABLE_SIZE = 2048; // Power of two
    private static final int TABLE_MASK = TABLE_SIZE - 1;
    private static final long EMPTY_KEY = Long.MIN_VALUE;

    private final long[] activeSectionKeys = new long[TABLE_SIZE];
    private int occupiedCount = 0;

    private final AtomicInteger deduplicatedAccumulator = new AtomicInteger(0);
    private volatile int deduplicatedPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    public ChunkRebuildDeduplicator() {
        Arrays.fill(activeSectionKeys, EMPTY_KEY);
    }

    /**
     * Packs 3D sub-chunk section coordinates into a single 64-bit primitive key without object allocation.
     */
    public static long packSectionKey(int sectionX, int sectionY, int sectionZ) {
        return ((long) (sectionX & 0x3FFFFF) << 42)
             | ((long) (sectionY & 0xFFFFF) << 22)
             | ((long) (sectionZ & 0x3FFFFF));
    }

    /**
     * Clears the per-tick deduplication table and updates per-second telemetry.
     */
    public synchronized void onTickRollover() {
        if (occupiedCount > 0) {
            Arrays.fill(activeSectionKeys, EMPTY_KEY);
            occupiedCount = 0;
        }
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            deduplicatedPerSecond = deduplicatedAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    /**
     * Returns true if this chunk section should be scheduled for rebuild, or false if it
     * was already queued during the current tick window.
     */
    public synchronized boolean shouldScheduleRebuild(int sectionX, int sectionY, int sectionZ) {
        if (!KineticConfig.modEnabled || !KineticConfig.deduplicateChunkUpdates) {
            return true;
        }

        if (occupiedCount >= (TABLE_SIZE >> 1)) {
            Arrays.fill(activeSectionKeys, EMPTY_KEY);
            occupiedCount = 0;
        }

        long key = packSectionKey(sectionX, sectionY, sectionZ);
        if (key == EMPTY_KEY) {
            key = Long.MAX_VALUE;
        }

        int slot = ((int) (key ^ (key >>> 32))) & TABLE_MASK;
        while (true) {
            long existing = activeSectionKeys[slot];
            if (existing == EMPTY_KEY) {
                activeSectionKeys[slot] = key;
                occupiedCount++;
                return true;
            }
            if (existing == key) {
                deduplicatedAccumulator.incrementAndGet();
                return false;
            }
            slot = (slot + 1) & TABLE_MASK;
        }
    }

    public int getDeduplicatedPerSecond() {
        return deduplicatedPerSecond;
    }
}
