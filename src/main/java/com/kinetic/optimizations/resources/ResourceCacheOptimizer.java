package com.kinetic.optimizations.resources;

import com.kinetic.optimizations.memory.AllocationReducer;
import com.kinetic.optimizations.rendering.GlStateCache;
import com.kinetic.optimizations.rendering.PrimitiveQuadSorter;
import com.kinetic.optimizations.rendering.TextWidthCache;
import com.kinetic.optimizations.utilities.KineticLogger;

import java.util.concurrent.atomic.AtomicInteger;

public final class ResourceCacheOptimizer {

    private static final ResourceCacheOptimizer INSTANCE = new ResourceCacheOptimizer();
    private final AtomicInteger reloadCount = new AtomicInteger(0);

    private ResourceCacheOptimizer() {
    }

    public static ResourceCacheOptimizer getInstance() {
        return INSTANCE;
    }

    public void onResourceReload() {
        GlStateCache.invalidateAll();
        TextWidthCache.getInstance().invalidateAll();
        ModelVertexDeduplicator.getInstance().clearPool();
        PrimitiveQuadSorter.getInstance().invalidateSortAnchor();
        AllocationReducer.resetThreadLocalRing();
        int count = reloadCount.incrementAndGet();
        KineticLogger.info("Resource reload #%d detected: flushed GL state, TextWidthCache, ModelQuadPool, and scratch rings.", count);
    }

    public int getReloadCount() {
        return reloadCount.get();
    }
}
