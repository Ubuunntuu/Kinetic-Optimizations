package com.kinetic.optimizations.benchmark;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.entities.EntityCollisionThrottler;
import com.kinetic.optimizations.lighting.LightingDeduplicator;
import com.kinetic.optimizations.rendering.TextWidthCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KineticPhase13SubsystemsTest {

    @Test
    void verifyLightingDeduplicatorSeparatesSkyAndBlockLight() {
        KineticConfig.modEnabled = true;
        KineticConfig.deduplicateLightUpdates = true;
        LightingDeduplicator dedup = LightingDeduplicator.getInstance();
        dedup.onTickRollover();

        // Sky light (ordinal 0) vs Block light (ordinal 1) at identical negative coordinates must NOT collide
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
}
