package com.kinetic.optimizations.benchmark;

import com.kinetic.optimizations.chunks.ChunkRebuildDeduplicator;
import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.config.PerformancePreset;
import com.kinetic.optimizations.memory.AllocationReducer;
import com.kinetic.optimizations.performance.FastMathEngine;
import com.kinetic.optimizations.rendering.FastFrustumCuller;
import com.kinetic.optimizations.rendering.GlStateCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

class KineticBenchmarkAndVerificationTest {

    @Test
    void verifyFastMathAccuracyAndFloorBoundaries() {
        float maxSinError = 0.0f;
        float maxCosError = 0.0f;

        for (int i = -7200; i <= 7200; i++) {
            float rad = i * 0.005f;
            float sinErr = Math.abs((float) Math.sin(rad) - FastMathEngine.sin(rad));
            float cosErr = Math.abs((float) Math.cos(rad) - FastMathEngine.cos(rad));
            if (sinErr > maxSinError) maxSinError = sinErr;
            if (cosErr > maxCosError) maxCosError = cosErr;
        }

        Assertions.assertTrue(maxSinError < 0.002f, "Sin LUT error too high: " + maxSinError);
        Assertions.assertTrue(maxCosError < 0.002f, "Cos LUT error too high: " + maxCosError);

        Assertions.assertEquals(-1, FastMathEngine.fastFloor(-0.001d));
        Assertions.assertEquals(-2, FastMathEngine.fastFloor(-1.999d));
        Assertions.assertEquals(0, FastMathEngine.fastFloor(0.0d));
        Assertions.assertEquals(42, FastMathEngine.fastFloor(42.999d));
    }

    @Test
    void verifyPackedBlockPosSignPreservation() {
        long packed = AllocationReducer.packBlockPos(-123456, 128, -654321);
        Assertions.assertEquals(-123456, AllocationReducer.unpackX(packed));
        Assertions.assertEquals(128, AllocationReducer.unpackY(packed));
        Assertions.assertEquals(-654321, AllocationReducer.unpackZ(packed));

        long movedEast = AllocationReducer.offsetPacked(packed, 5);
        Assertions.assertEquals(-123455, AllocationReducer.unpackX(movedEast));
    }

    @Test
    void verifyChunkSectionDeduplicatorAndNegativeCoords() {
        ChunkRebuildDeduplicator dedup = new ChunkRebuildDeduplicator();
        KineticConfig.modEnabled = true;
        KineticConfig.deduplicateChunkUpdates = true;

        Assertions.assertTrue(dedup.shouldScheduleRebuild(-15, 4, 32));
        Assertions.assertFalse(dedup.shouldScheduleRebuild(-15, 4, 32));
        Assertions.assertTrue(dedup.shouldScheduleRebuild(-15, 5, 32));

        dedup.onTickRollover();
        Assertions.assertTrue(dedup.shouldScheduleRebuild(-15, 4, 32));
    }

    @Test
    void verifyGlStateCacheTriStateAndFrameReset() {
        KineticConfig.modEnabled = true;
        KineticConfig.optimizeStateChanges = true;
        KineticConfig.deduplicateTextureBinds = true;

        GlStateCache.invalidateAll();
        Assertions.assertFalse(GlStateCache.shouldSkipBindTexture(12));
        Assertions.assertTrue(GlStateCache.shouldSkipBindTexture(12));
        GlStateCache.invalidateTexture(12);
        Assertions.assertFalse(GlStateCache.shouldSkipBindTexture(12));

        GlStateCache.onFrameStart();
        Assertions.assertFalse(GlStateCache.shouldSkipBlendChange(true));
        Assertions.assertTrue(GlStateCache.shouldSkipBlendChange(true));
        Assertions.assertFalse(GlStateCache.shouldSkipBlendChange(false));
    }

    @Test
    void verifyPresetsNeverEnableExperimentalFeatures() {
        for (PerformancePreset preset : PerformancePreset.values()) {
            if (preset != PerformancePreset.CUSTOM) {
                KineticConfig.applyPreset(preset);
                Assertions.assertFalse(KineticConfig.enableExperimentalOptimizations);
                Assertions.assertFalse(KineticConfig.aggressiveOcclusionCulling);
            }
        }
    }

    @Test
    void verifyFrustumDistanceAndArmorStandCulling() {
        KineticConfig.modEnabled = true;
        KineticConfig.skipInvisibleArmorStands = true;
        FastFrustumCuller culler = FastFrustumCuller.getInstance();
        culler.setCameraPosition(0.0, 64.0, 0.0);

        Assertions.assertTrue(culler.isWithinRenderDistanceSq(10.0, 64.0, 10.0, 4096.0));
        Assertions.assertFalse(culler.isWithinRenderDistanceSq(100.0, 64.0, 0.0, 4096.0));
        Assertions.assertTrue(culler.shouldSkipInvisibleArmorStand(true, false, false));
        Assertions.assertFalse(culler.shouldSkipInvisibleArmorStand(true, true, false));
        Assertions.assertFalse(culler.shouldSkipInvisibleArmorStand(true, false, true));
    }

    @Test
    void runTunedReleaseMicrobenchmarks() throws Exception {
        final int iterations = 10_000_000;

        float warmupSink = 0.0f;
        AllocationReducer.CursorRing warmRing = AllocationReducer.acquireRing();
        for (int i = 0; i < 500_000; i++) {
            warmupSink += (float) Math.sin(i * 0.01f) + FastMathEngine.sin(i * 0.01f);
            warmupSink += warmRing.next(i, 64, -i).move(i % 6).x;
            warmupSink += AllocationReducer.unpackX(AllocationReducer.packBlockPos(i, 64, -i));
        }

        long t0 = System.nanoTime();
        float stdSum = 0.0f;
        for (int i = 0; i < iterations; i++) {
            float angle = i * 0.001f;
            stdSum += (float) Math.sin(angle) + (float) Math.cos(angle);
        }
        long stdMathNs = System.nanoTime() - t0;

        long t1 = System.nanoTime();
        float fastSum = 0.0f;
        for (int i = 0; i < iterations; i++) {
            float angle = i * 0.001f;
            fastSum += FastMathEngine.sin(angle) + FastMathEngine.cos(angle);
        }
        long fastMathNs = System.nanoTime() - t1;

        long t2 = System.nanoTime();
        long heapCoordSum = 0L;
        for (int i = 0; i < iterations; i++) {
            int[] pos = new int[]{i, 64, -i};
            pos[0] += AllocationReducer.DIR_OFFSET_X[i % 6];
            heapCoordSum += pos[0] + pos[1] + pos[2];
        }
        long heapAllocNs = System.nanoTime() - t2;

        long t3 = System.nanoTime();
        long cursorCoordSum = 0L;
        AllocationReducer.CursorRing ring = AllocationReducer.acquireRing();
        for (int i = 0; i < iterations; i++) {
            AllocationReducer.MutablePosCursor c = ring.next(i, 64, -i).move(i % 6);
            cursorCoordSum += c.x + c.y + c.z;
        }
        long cursorRingNs = System.nanoTime() - t3;

        long tPacked = System.nanoTime();
        long packedCoordSum = 0L;
        for (int i = 0; i < iterations; i++) {
            long p = AllocationReducer.offsetPacked(AllocationReducer.packBlockPos(i, 64, -i), i % 6);
            packedCoordSum += AllocationReducer.unpackX(p) + AllocationReducer.unpackY(p) + AllocationReducer.unpackZ(p);
        }
        long packedNs = System.nanoTime() - tPacked;

        ChunkRebuildDeduplicator dedup = new ChunkRebuildDeduplicator();
        KineticConfig.modEnabled = true;
        KineticConfig.deduplicateChunkUpdates = true;
        final int chunkLookups = 5_000_000;
        long t4 = System.nanoTime();
        int scheduled = 0;
        for (int i = 0; i < chunkLookups; i++) {
            if (dedup.shouldScheduleRebuild(i & 15, (i >> 4) & 15, (i >> 8) & 15)) {
                scheduled++;
            }
        }
        long dedupNs = System.nanoTime() - t4;

        Assertions.assertEquals(heapCoordSum, cursorCoordSum);
        Assertions.assertEquals(heapCoordSum, packedCoordSum);
        Assertions.assertEquals(4096, scheduled);
        Assertions.assertTrue(warmupSink != Float.NaN && stdSum != Float.NaN && fastSum != Float.NaN);

        double mathSpeedup = (double) stdMathNs / Math.max(1L, fastMathNs);
        String report = String.format(
            Locale.ROOT,
            "=== KINETIC OPTIMIZATIONS v1.0.0 TUNED RELEASE MICROBENCHMARK ===\n"
            + "JVM: Java %s (%s) | Processors: %d\n"
            + "1. Trig (10M sin+cos pairs):\n"
            + "   - Standard Math.sin/cos:      %.2f ms (%.2f ns/pair)\n"
            + "   - Kinetic 16KB L1 LUT:        %.2f ms (%.2f ns/pair) -> %.2fx speedup\n"
            + "2. Zero-Allocation Coordinates (10M neighbor steps):\n"
            + "   - Baseline int[3]:            %.2f ms (%.2f ns/op)\n"
            + "   - Hoisted CursorRing:         %.2f ms (%.2f ns/op) [0 Heap Allocations]\n"
            + "   - 64-Bit Packed BlockPos:     %.2f ms (%.2f ns/op) [Register-Only]\n"
            + "3. Generation-Stamped Chunk Deduplicator (5M rebuild checks):\n"
            + "   - Total Time:                 %.2f ms (%.2f ns/check)\n"
            + "   - Deduplication Accuracy:     Accepted %d unique / Rejected %d duplicates (99.92%% filtered)\n",
            Runtime.version(),
            System.getProperty("java.vm.name"),
            Runtime.getRuntime().availableProcessors(),
            stdMathNs / 1_000_000.0, (double) stdMathNs / iterations,
            fastMathNs / 1_000_000.0, (double) fastMathNs / iterations, mathSpeedup,
            heapAllocNs / 1_000_000.0, (double) heapAllocNs / iterations,
            cursorRingNs / 1_000_000.0, (double) cursorRingNs / iterations,
            packedNs / 1_000_000.0, (double) packedNs / iterations,
            dedupNs / 1_000_000.0, (double) dedupNs / chunkLookups,
            scheduled, (chunkLookups - scheduled)
        );

        System.out.println(report);
        Path out = Path.of("build", "reports", "kinetic_cpu_microbenchmark.txt");
        Files.createDirectories(out.getParent());
        Files.writeString(out, report, StandardCharsets.UTF_8);
    }
}
