package com.kinetic.optimizations.chunks;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChunkRebuildDeduplicator {

    private static final int TABLE_SIZE = 16384;
    private static final int TABLE_MASK = TABLE_SIZE - 1;

    private final long[] sectionKeys = new long[TABLE_SIZE];
    private final int[] slotGenerations = new int[TABLE_SIZE];
    private int currentGeneration = 1;
    private int occupiedInGeneration = 0;

    private final AtomicInteger deduplicatedAccumulator = new AtomicInteger(0);
    private volatile int deduplicatedPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    public static long packSectionKey(int sectionX, int sectionY, int sectionZ) {
        return ((long) (sectionX & 0x3FFFFF) << 42)
             | ((long) (sectionY & 0xFFFFF) << 22)
             | ((long) (sectionZ & 0x3FFFFF));
    }

    public void onTickRollover() {
        advanceGeneration();
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            deduplicatedPerSecond = deduplicatedAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    private void advanceGeneration() {
        currentGeneration++;
        if (currentGeneration == Integer.MAX_VALUE) {
            Arrays.fill(slotGenerations, 0);
            currentGeneration = 1;
        }
        occupiedInGeneration = 0;
    }

    public boolean shouldScheduleRebuild(int sectionX, int sectionY, int sectionZ) {
        if (!KineticConfig.modEnabled || !KineticConfig.deduplicateChunkUpdates) {
            return true;
        }

        if (occupiedInGeneration >= (TABLE_SIZE >> 1)) {
            advanceGeneration();
        }

        long key = packSectionKey(sectionX, sectionY, sectionZ);
        int gen = this.currentGeneration;
        int hash = (int) ((key ^ (key >>> 32)) * 0x9E3779B97F4A7C15L >>> 48);
        int slot = hash & TABLE_MASK;

        while (true) {
            if (slotGenerations[slot] != gen) {
                slotGenerations[slot] = gen;
                sectionKeys[slot] = key;
                occupiedInGeneration++;
                return true;
            }
            if (sectionKeys[slot] == key) {
                deduplicatedAccumulator.lazySet(deduplicatedAccumulator.get() + 1);
                return false;
            }
            slot = (slot + 1) & TABLE_MASK;
        }
    }

    public int getDeduplicatedPerSecond() {
        return deduplicatedPerSecond;
    }
}
