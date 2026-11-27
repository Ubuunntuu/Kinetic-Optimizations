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

        // 4096-entry LUT step error bound is <= ~0.0016 radians
        Assertions.assertTrue(maxSinError < 0.002f, "Sin LUT error too high: " + maxSinError);
        Assertions.assertTrue(maxCosError < 0.002f, "Cos LUT error too high: " + maxCosError);

        // Verify negative boundary fastFloor matches Math.floor
        Assertions.assertEquals(-1, FastMathEngine.fastFloor(-0.001d));
        Assertions.assertEquals(-2, FastMathEngine.fastFloor(-1.999d));
        Assertions.assertEquals(0, FastMathEngine.fastFloor(0.0d));
        Assertions.assertEquals(42, FastMathEngine.fastFloor(42.999d));
    }

    @Test
    void verifyChunkSectionDeduplicatorAndNegativeCoords() {
        ChunkRebuildDeduplicator dedup = new ChunkRebuildDeduplicator();
        KineticConfig.modEnabled = true;
        KineticConfig.deduplicateChunkUpdates = true;

        Assertions.assertTrue(dedup.shouldScheduleRebuild(-15, 4, 32));
        Assertions.assertFalse(dedup.shouldScheduleRebuild(-15, 4, 32), "Duplicate section rebuild in same tick must be rejected");
        Assertions.assertTrue(dedup.shouldScheduleRebuild(-15, 5, 32), "Adjacent Y section must be accepted");

        dedup.onTickRollover();
        Assertions.assertTrue(dedup.shouldScheduleRebuild(-15, 4, 32), "After tick rollover, section must be accepted again");
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
                Assertions.assertFalse(KineticConfig.enableExperimentalOptimizations,
                    "Preset " + preset + " must never enable experimental features automatically");
                Assertions.assertFalse(KineticConfig.aggressiveOcclusionCulling,
                    "Preset " + preset + " must never enable experimental occlusion culling");
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
        Assertions.assertFalse(culler.shouldSkipInvisibleArmorStand(true, true, false), "Named hologram armor stands must NOT be skipped");
        Assertions.assertFalse(culler.shouldSkipInvisibleArmorStand(true, false, true), "Equipped armor stands must NOT be skipped");
    }

    @Test
    void runHardwareCpuAndMemoryMicrobenchmarks() throws Exception {
        final int iterations = 10_000_000;

        // Warmup JIT
        float warmupSink = 0.0f;
        for (int i = 0; i < 500_000; i++) {
            warmupSink += (float) Math.sin(i * 0.01f) + FastMathEngine.sin(i * 0.01f);
            AllocationReducer.MutablePosCursor c = AllocationReducer.acquireCursor(i, 64, -i);
            warmupSink += c.x;
        }

        // 1. Standard Math.sin/cos vs Kinetic 16KB L1 LUT
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

        // 2. Heap int[3] allocation vs Thread-Local MutablePosCursor Ring
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
        for (int i = 0; i < iterations; i++) {
            AllocationReducer.MutablePosCursor c = AllocationReducer.acquireCursor(i, 64, -i).move(i % 6);
            cursorCoordSum += c.x + c.y + c.z;
        }
        long cursorRingNs = System.nanoTime() - t3;

        // 3. 64-bit Chunk Section Rebuild Deduplicator throughput (5M lookups)
        ChunkRebuildDeduplicator dedup = new ChunkRebuildDeduplicator();
        KineticConfig.modEnabled = true;
        KineticConfig.deduplicateChunkUpdates = true;
        final int chunkLookups = 5_000_000;
        long t4 = System.nanoTime();
        int scheduled = 0;
        for (int i = 0; i < chunkLookups; i++) {
            if (dedup.shouldScheduleRebuild(i & 31, (i >> 5) & 7, (i >> 8) & 31)) {
                scheduled++;
            }
        }
        long dedupNs = System.nanoTime() - t4;

        Assertions.assertEquals(heapCoordSum, cursorCoordSum);
        Assertions.assertTrue(scheduled > 0 && warmupSink != Float.NaN && stdSum != Float.NaN && fastSum != Float.NaN);

        double mathSpeedup = (double) stdMathNs / Math.max(1L, fastMathNs);
        String report = String.format(
            Locale.ROOT,
            """
            === KINETIC OPTIMIZATIONS PHASE 9 HARDWARE MICROBENCHMARK ===
            JVM: Java %s (%s) | Processors: %d
            1. Trig (10M sin+cos pairs):
               - Standard Math.sin/cos: %.2f ms (%.2f ns/pair)
               - Kinetic 16KB L1 LUT:   %.2f ms (%.2f ns/pair) -> %.2fx speedup
            2. Coordinate Cursor (10M neighbor steps):
               - Heap int[3] alloc:     %.2f ms (%.2f ns/op)
               - Thread-Local Cursor:   %.2f ms (%.2f ns/op) [Zero Heap Churn]
            3. 64-Bit Chunk Section Deduplicator (5M rebuild checks):
               - Total Time:            %.2f ms (%.2f ns/check, accepted %d unique sections)
            """,
            Runtime.version(),
            System.getProperty("java.vm.name"),
            Runtime.getRuntime().availableProcessors(),
            stdMathNs / 1_000_000.0, (double) stdMathNs / iterations,
            fastMathNs / 1_000_000.0, (double) fastMathNs / iterations, mathSpeedup,
            heapAllocNs / 1_000_000.0, (double) heapAllocNs / iterations,
            cursorRingNs / 1_000_000.0, (double) cursorRingNs / iterations,
            dedupNs / 1_000_000.0, (double) dedupNs / chunkLookups, scheduled
        );

        System.out.println(report);
        Path out = Path.of("build", "reports", "kinetic_cpu_microbenchmark.txt");
        Files.createDirectories(out.getParent());
        Files.writeString(out, report, StandardCharsets.UTF_8);
    }
}
