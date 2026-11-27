package com.kinetic.optimizations.lighting;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zero-allocation 32,768-slot generation-stamped open-addressed hash table
 * that filters duplicate sky/block light recalculations at the same BlockPos within a tick.
 */
public final class LightingDeduplicator {

    private static final LightingDeduplicator INSTANCE = new LightingDeduplicator();

    private static final int TABLE_SIZE = 32768;
    private static final int TABLE_MASK = TABLE_SIZE - 1;

    private final long[] packedLightKeys = new long[TABLE_SIZE];
    private final int[] slotGenerations = new int[TABLE_SIZE];
    private int currentGeneration = 1;
    private int occupiedCount = 0;

    private final AtomicInteger skippedLightChecksAccumulator = new AtomicInteger(0);
    private volatile int skippedLightChecksPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    private LightingDeduplicator() {
    }

    public static LightingDeduplicator getInstance() {
        return INSTANCE;
    }

    /**
     * Packs (lightTypeOrdinal [0..1], x, y, z) into a single 64-bit primitive key.
     */
    public static long packLightKey(int lightTypeOrdinal, int x, int y, int z) {
        long coords = (((long) x & 0x1FFFFFFL) << 38)
                    | (((long) y & 0xFFFL) << 26)
                    | ((long) z & 0x3FFFFFFL);
        return coords ^ (((long) (lightTypeOrdinal & 1)) << 63);
    }

    public void onTickRollover() {
        advanceGeneration();
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            skippedLightChecksPerSecond = skippedLightChecksAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    private void advanceGeneration() {
        currentGeneration++;
        if (currentGeneration == Integer.MAX_VALUE) {
            Arrays.fill(slotGenerations, 0);
            currentGeneration = 1;
        }
        occupiedCount = 0;
    }

    public boolean shouldCheckLight(int lightTypeOrdinal, int x, int y, int z) {
        if (!KineticConfig.modEnabled || !KineticConfig.deduplicateLightUpdates) {
            return true;
        }
        if (occupiedCount >= (TABLE_SIZE >> 1)) {
            advanceGeneration();
        }

        long key = packLightKey(lightTypeOrdinal, x, y, z);
        int gen = this.currentGeneration;
        int slot = ((int) ((key ^ (key >>> 32)) * 0x9E3779B97F4A7C15L >>> 47)) & TABLE_MASK;

        while (true) {
            if (slotGenerations[slot] != gen) {
                slotGenerations[slot] = gen;
                packedLightKeys[slot] = key;
                occupiedCount++;
                return true;
            }
            if (packedLightKeys[slot] == key) {
                skippedLightChecksAccumulator.lazySet(skippedLightChecksAccumulator.get() + 1);
                return false;
            }
            slot = (slot + 1) & TABLE_MASK;
        }
    }

    public int getSkippedLightChecksPerSecond() {
        return skippedLightChecksPerSecond;
    }
}
