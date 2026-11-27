package com.kinetic.optimizations.entities;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;

/**
 * Tracks per-tick entity density per 2x2x2 block cell on the client thread.
 * Once more than maxEntitiesPerCollisionCell entities occupy the same cell,
 * redundant client-only visual push scans are throttled to prevent O(N^2) tick spikes.
 */
public final class EntityCollisionThrottler {

    private static final EntityCollisionThrottler INSTANCE = new EntityCollisionThrottler();
    private static final int TABLE_SIZE = 4096;
    private static final int TABLE_MASK = TABLE_SIZE - 1;
    private static final int MAX_CELL_ENTITIES = 16;

    private final long[] cellKeys = new long[TABLE_SIZE];
    private final int[] cellGenerations = new int[TABLE_SIZE];
    private final short[] cellCounts = new short[TABLE_SIZE];
    private int currentGeneration = 1;
    private int occupiedCells = 0;

    private EntityCollisionThrottler() {
    }

    public static EntityCollisionThrottler getInstance() {
        return INSTANCE;
    }

    public void onTickRollover() {
        currentGeneration++;
        if (currentGeneration == Integer.MAX_VALUE) {
            Arrays.fill(cellGenerations, 0);
            currentGeneration = 1;
        }
        occupiedCells = 0;
    }

    public boolean shouldRunClientPushCheck(double x, double y, double z) {
        if (!KineticConfig.modEnabled || !KineticConfig.throttleCrammedEntityCollisions) {
            return true;
        }
        if (occupiedCells >= (TABLE_SIZE >> 1)) {
            onTickRollover();
        }

        int cx = ((int) Math.floor(x)) >> 1;
        int cy = ((int) Math.floor(y)) >> 1;
        int cz = ((int) Math.floor(z)) >> 1;

        long key = (((long) (cx & 0x3FFFFF)) << 42)
                 | (((long) (cy & 0xFFFFF)) << 22)
                 | ((long) (cz & 0x3FFFFF));

        int gen = this.currentGeneration;
        int slot = ((int) ((key ^ (key >>> 32)) * 0x9E3779B97F4A7C15L >>> 52)) & TABLE_MASK;

        while (true) {
            if (cellGenerations[slot] != gen) {
                cellGenerations[slot] = gen;
                cellKeys[slot] = key;
                cellCounts[slot] = 1;
                occupiedCells++;
                return true;
            }
            if (cellKeys[slot] == key) {
                int nextCount = cellCounts[slot] + 1;
                cellCounts[slot] = (short) Math.min(Short.MAX_VALUE, nextCount);
                return nextCount <= MAX_CELL_ENTITIES;
            }
            slot = (slot + 1) & TABLE_MASK;
        }
    }
}
