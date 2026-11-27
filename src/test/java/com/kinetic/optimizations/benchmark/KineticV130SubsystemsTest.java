package com.kinetic.optimizations.benchmark;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.entities.ItemAndXpRenderThrottler;
import com.kinetic.optimizations.rendering.RenderScratchPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;

class KineticV130SubsystemsTest {

    @Test
    void verifyItemClumpThrottlerPreservesConfiguredMeshesPerCellAndSeparatesXpOrbs() {
        KineticConfig.modEnabled = true;
        KineticConfig.throttleClumpedItemsAndXp = true;
        KineticConfig.maxRenderedDropsPerBlockCell = 4;

        ItemAndXpRenderThrottler throttler = ItemAndXpRenderThrottler.getInstance();
        throttler.onFrameStart();

        for (int i = 0; i < 4; i++) {
            Assertions.assertTrue(throttler.shouldRenderClumpedDrop(0, 12.4, 64.0, -8.2));
        }
        Assertions.assertFalse(throttler.shouldRenderClumpedDrop(0, 12.4, 64.0, -8.2),
            "5th overlapping item mesh in exact same 1x1x1 block cell must be skipped");

        // XP orbs (typeTag = 1) in the same block cell have their own independent visual budget
        Assertions.assertTrue(throttler.shouldRenderClumpedDrop(1, 12.4, 64.0, -8.2));

        // Next frame resets the render budget
        throttler.onFrameStart();
        Assertions.assertTrue(throttler.shouldRenderClumpedDrop(0, 12.4, 64.0, -8.2));
    }

    @Test
    void verifyJava25FfmOffHeapCacheLineAlignment() {
        RenderScratchPool.ScratchBuffers scratch = RenderScratchPool.get();
        MemorySegment seg = scratch.getAlignedFloatSegment16();
        Assertions.assertEquals(0L, seg.address() % 64L, "Off-heap FFM segment must be 64-byte cache-line aligned");
    }
}
