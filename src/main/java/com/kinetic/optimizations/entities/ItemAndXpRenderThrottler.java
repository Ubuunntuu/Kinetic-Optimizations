package com.kinetic.optimizations.entities;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spatial 1x1x1 block-cell density budgeter for dropped ItemEntities and ExperienceOrbs.
 * Renders up to maxClumpedItemsPerCell meshes per 1x1x1 block cell per frame so massive loot/XP
 * piles never stall BakedQuad and GL blend state submission while keeping every pile clearly visible.
 */
public final class ItemAndXpRenderThrottler {

    private static final ItemAndXpRenderThrottler INSTANCE = new ItemAndXpRenderThrottler();
    private static final int TABLE_SIZE = 4096;
    private static final int TABLE_MASK = TABLE_SIZE - 1;

    private final long[] cellKeys = new long[TABLE_SIZE];
    private final int[] cellGenerations = new int[TABLE_SIZE];
    private final short[] renderedInCell = new short[TABLE_SIZE];
    private int currentFrameGeneration = 1;
    private int occupiedCells = 0;

    private final AtomicInteger skippedClumpMeshesAccumulator = new AtomicInteger(0);
    private volatile int skippedClumpMeshesPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    private ItemAndXpRenderThrottler() {
    }

    public static ItemAndXpRenderThrottler getInstance() {
        return INSTANCE;
    }

    public void onFrameStart() {
        currentFrameGeneration++;
        if (currentFrameGeneration == Integer.MAX_VALUE) {
            Arrays.fill(cellGenerations, 0);
            currentFrameGeneration = 1;
        }
        occupiedCells = 0;

        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            skippedClumpMeshesPerSecond = skippedClumpMeshesAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    /**
     * Returns true if this dropped item or XP orb should render its 3D/sprite mesh this frame.
     * typeTag: 0 = ItemEntity, 1 = ExperienceOrbEntity.
     */
    public boolean shouldRenderClumpedDrop(int typeTag, double x, double y, double z) {
        if (!KineticConfig.modEnabled || !KineticConfig.throttleClumpedItemsAndXp) {
            return true;
        }
        if (occupiedCells >= (TABLE_SIZE >> 1)) {
            currentFrameGeneration++;
            occupiedCells = 0;
        }

        int bx = fastFloor(x);
        int by = fastFloor(y);
        int bz = fastFloor(z);

        long key = (((long) (bx & 0x1FFFFFF)) << 38)
                 | (((long) (by & 0xFFF)) << 26)
                 | ((long) (bz & 0x3FFFFFF))
                 ^ (((long) (typeTag & 1)) << 63);

        int gen = this.currentFrameGeneration;
        int maxPerCell = Math.max(2, KineticConfig.maxRenderedDropsPerBlockCell);
        int slot = ((int) ((key ^ (key >>> 32)) * 0x9E3779B97F4A7C15L >>> 52)) & TABLE_MASK;

        while (true) {
            if (cellGenerations[slot] != gen) {
                cellGenerations[slot] = gen;
                cellKeys[slot] = key;
                renderedInCell[slot] = 1;
                occupiedCells++;
                return true;
            }
            if (cellKeys[slot] == key) {
                int count = renderedInCell[slot] + 1;
                renderedInCell[slot] = (short) Math.min(Short.MAX_VALUE, count);
                if (count <= maxPerCell) {
                    return true;
                }
                skippedClumpMeshesAccumulator.lazySet(skippedClumpMeshesAccumulator.get() + 1);
                return false;
            }
            slot = (slot + 1) & TABLE_MASK;
        }
    }

    private static int fastFloor(double v) {
        int i = (int) v;
        return v < i ? i - 1 : i;
    }

    public int getSkippedClumpMeshesPerSecond() {
        return skippedClumpMeshesPerSecond;
    }
}
