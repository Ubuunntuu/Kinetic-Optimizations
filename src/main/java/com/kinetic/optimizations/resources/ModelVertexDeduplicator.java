package com.kinetic.optimizations.resources;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 4,096-slot content-addressed pool for BakedQuad int[] vertex arrays.
 * Shares identical immutable quad vertex arrays across blockstates and resource reloads,
 * saving resident heap memory and improving CPU L2/L3 cache locality.
 */
public final class ModelVertexDeduplicator {

    private static final ModelVertexDeduplicator INSTANCE = new ModelVertexDeduplicator();
    private static final int POOL_SIZE = 4096;
    private static final int POOL_MASK = POOL_SIZE - 1;

    private final int[][] pool = new int[POOL_SIZE][];
    private final AtomicInteger deduplicatedQuads = new AtomicInteger(0);

    private ModelVertexDeduplicator() {
    }

    public static ModelVertexDeduplicator getInstance() {
        return INSTANCE;
    }

    public int[] canonicalizeVertices(int[] vertices) {
        if (!KineticConfig.modEnabled || !KineticConfig.deduplicateBakedModelQuads || vertices == null || vertices.length == 0) {
            return vertices;
        }
        int hash = Arrays.hashCode(vertices);
        int slot = ((hash ^ (hash >>> 16)) * 0x45d9f3b) & POOL_MASK;

        int[] existing = pool[slot];
        if (existing != null && Arrays.equals(existing, vertices)) {
            deduplicatedQuads.lazySet(deduplicatedQuads.get() + 1);
            return existing;
        }
        pool[slot] = vertices;
        return vertices;
    }

    public void clearPool() {
        Arrays.fill(pool, null);
    }

    public int getDeduplicatedQuadCount() {
        return deduplicatedQuads.get();
    }
}
