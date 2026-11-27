package com.kinetic.optimizations.benchmark;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.entities.EntityCollisionThrottler;
import com.kinetic.optimizations.lighting.LightingDeduplicator;
import com.kinetic.optimizations.rendering.FastFrustumCuller;
import com.kinetic.optimizations.rendering.RenderScratchPool;
import com.kinetic.optimizations.rendering.TextWidthCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.util.Locale;

class KineticPhase13SubsystemsTest {

    @Test
    void verifyLightingDeduplicatorSeparatesSkyAndBlockLight() {
        KineticConfig.modEnabled = true;
        KineticConfig.deduplicateLightUpdates = true;
        LightingDeduplicator dedup = LightingDeduplicator.getInstance();
        dedup.onTickRollover();

        Assertions.assertTrue(dedup.shouldCheckLight(0, -512, 64, -1024));
        Assertions.assertFalse(dedup.shouldCheckLight(0, -512, 64, -1024));
        Assertions.assertTrue(dedup.shouldCheckLight(1, -512, 64, -1024));
        Assertions.assertFalse(dedup.shouldCheckLight(1, -512, 64, -1024));

        dedup.onTickRollover();
        Assertions.assertTrue(dedup.shouldCheckLight(0, -512, 64, -1024));
    }

    @Test
    void verifyTextWidthCacheLookupAndInvalidation() {
        KineticConfig.modEnabled = true;
        KineticConfig.cacheTextRendererWidths = true;
        TextWidthCache cache = TextWidthCache.getInstance();
        cache.invalidateAll();

        Assertions.assertEquals(-1, cache.getCachedWidth("§bKinetic §f144 FPS"));
        cache.putCachedWidth("§bKinetic §f144 FPS", 92);
        Assertions.assertEquals(92, cache.getCachedWidth("§bKinetic §f144 FPS"));

        cache.invalidateAll();
        Assertions.assertEquals(-1, cache.getCachedWidth("§bKinetic §f144 FPS"));
    }

    @Test
    void verifyEntityCollisionThrottlerCapsAtSixteenEntitiesPerCell() {
        KineticConfig.modEnabled = true;
        KineticConfig.throttleCrammedEntityCollisions = true;
        EntityCollisionThrottler throttler = EntityCollisionThrottler.getInstance();
        throttler.onTickRollover();

        for (int i = 0; i < 16; i++) {
            Assertions.assertTrue(throttler.shouldRunClientPushCheck(10.2, 64.0, -20.5));
        }
        Assertions.assertFalse(throttler.shouldRunClientPushCheck(10.2, 64.0, -20.5));

        throttler.onTickRollover();
        Assertions.assertTrue(throttler.shouldRunClientPushCheck(10.2, 64.0, -20.5));
    }

    @Test
    void verifyJava25FfmCacheLineAlignmentAndUnrolledFmaFrustumBenchmark() {
        RenderScratchPool.ScratchBuffers scratch = RenderScratchPool.get();
        MemorySegment seg = scratch.getAlignedFloatSegment16();

        // Verify 64-byte x86-64 CPU cache-line alignment on native off-heap address
        Assertions.assertEquals(0L, seg.address() % 64L, "MemorySegment must be 64-byte cache-line aligned!");
        scratch.writeFloatAt(0, 3.14159f);
        Assertions.assertEquals(3.14159f, scratch.readFloatAt(0), 1e-6f);
        Assertions.assertEquals(16, scratch.getCleanFloatBuffer16().capacity());

        // Benchmark Unrolled FMA 6-Plane Frustum Culling (10,000,000 AABB checks)
        KineticConfig.modEnabled = true;
        KineticConfig.fastFrustumCulling = true;
        FastFrustumCuller culler = FastFrustumCuller.getInstance();
        culler.setCameraPosition(0.0, 64.0, 0.0);
        culler.updateFrustumPlanes(new float[]{
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 64.0f
        });

        int warmup = 0;
        for (int i = 0; i < 200_000; i++) {
            if (culler.isBoxVisible(-2, 62, -2, 2, 66, 2)) warmup++;
        }

        final int checks = 10_000_000;
        long start = System.nanoTime();
        int visible = 0;
        for (int i = 0; i < checks; i++) {
            double x = (i & 127) - 64.0;
            if (culler.isBoxVisible(x, 63.0, -10.0, x + 1.0, 65.0, -9.0)) {
                visible++;
            }
        }
        long elapsedNs = System.nanoTime() - start;
        Assertions.assertTrue(visible > 0 && warmup > 0);

        System.out.printf(
            Locale.ROOT,
            "=== JAVA 25 FFM & UNROLLED FMA FRUSTUM BENCHMARK ===%n"
            + "- Off-Heap Arena Segment Address: 0x%x (64-byte aligned: YES)%n"
            + "- Unrolled FMA3 6-Plane Frustum (10M AABBs): %.2f ms (%.2f ns/AABB)%n",
            seg.address(),
            elapsedNs / 1_000_000.0,
            (double) elapsedNs / checks
        );
    }
}
