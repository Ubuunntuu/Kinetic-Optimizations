package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Eliminates boxed Float[] / Integer[] heap allocations during translucent quad sorting
 * and filters sub-threshold micro-camera jitter re-sorts.
 */
public final class PrimitiveQuadSorter {

    private static final PrimitiveQuadSorter INSTANCE = new PrimitiveQuadSorter();
    private static final float MIN_CAMERA_MOVE_SQ = 0.0625f; // (0.25 blocks)^2

    private float lastSortX = Float.NaN;
    private float lastSortY = Float.NaN;
    private float lastSortZ = Float.NaN;
    private int lastVertexCount = -1;

    private final AtomicInteger skippedSortsAccumulator = new AtomicInteger(0);
    private volatile int skippedSortsPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    private PrimitiveQuadSorter() {
    }

    public static PrimitiveQuadSorter getInstance() {
        return INSTANCE;
    }

    public void onFrameStart() {
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            skippedSortsPerSecond = skippedSortsAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    public boolean shouldSkipRedundantSort(float camX, float camY, float camZ, int vertexCount) {
        if (!KineticConfig.modEnabled || !KineticConfig.primitiveTranslucentSort) {
            return false;
        }
        if (vertexCount <= 4) {
            skippedSortsAccumulator.lazySet(skippedSortsAccumulator.get() + 1);
            return true;
        }
        if (vertexCount == lastVertexCount && !Float.isNaN(lastSortX)) {
            float dx = camX - lastSortX;
            float dy = camY - lastSortY;
            float dz = camZ - lastSortZ;
            if (Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)) < MIN_CAMERA_MOVE_SQ) {
                skippedSortsAccumulator.lazySet(skippedSortsAccumulator.get() + 1);
                return true;
            }
        }
        lastSortX = camX;
        lastSortY = camY;
        lastSortZ = camZ;
        lastVertexCount = vertexCount;
        return false;
    }

    public void invalidateSortAnchor() {
        lastSortX = Float.NaN;
        lastVertexCount = -1;
    }

    /**
     * In-place zero-allocation descending sort of quad indices by squared distance.
     * Uses insertion sort for small partitions (<= 24 quads) and dual-pivot-style primitive quicksort above.
     */
    public static void sortQuadIndicesDescending(float[] distancesSq, int[] quadIndices, int count) {
        if (count <= 1) {
            return;
        }
        quickSortDescending(distancesSq, quadIndices, 0, count - 1);
    }

    private static void quickSortDescending(float[] dist, int[] idx, int low, int high) {
        while (high - low > 20) {
            int mid = (low + high) >>> 1;
            float pivot = dist[idx[mid]];
            int i = low;
            int j = high;

            while (i <= j) {
                while (dist[idx[i]] > pivot) i++;
                while (dist[idx[j]] < pivot) j--;
                if (i <= j) {
                    int tmp = idx[i];
                    idx[i] = idx[j];
                    idx[j] = tmp;
                    i++;
                    j--;
                }
            }

            if (j - low < high - i) {
                if (low < j) quickSortDescending(dist, idx, low, j);
                low = i;
            } else {
                if (i < high) quickSortDescending(dist, idx, i, high);
                high = j;
            }
        }
        insertionSortDescending(dist, idx, low, high);
    }

    private static void insertionSortDescending(float[] dist, int[] idx, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int keyIdx = idx[i];
            float keyDist = dist[keyIdx];
            int j = i - 1;
            while (j >= low && dist[idx[j]] < keyDist) {
                idx[j + 1] = idx[j];
                j--;
            }
            idx[j + 1] = keyIdx;
        }
    }

    public int getSkippedSortsPerSecond() {
        return skippedSortsPerSecond;
    }
}
