package com.kinetic.optimizations.benchmark;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.memory.ScalarBoxMath;
import com.kinetic.optimizations.networking.FastStringInterner;
import com.kinetic.optimizations.rendering.PrimitiveQuadSorter;
import com.kinetic.optimizations.rendering.SkyAndCloudCuller;
import com.kinetic.optimizations.resources.ModelVertexDeduplicator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

class KineticV140SubsystemsTest {

    @Test
    void verifyPrimitiveQuadSortMatchesBoxedDescendingOrderAndBenchmark() {
        final int quadCount = 1024;
        float[] dist = new float[quadCount];
        int[] primIdx = new int[quadCount];
        Integer[] boxedIdx = new Integer[quadCount];

        for (int i = 0; i < quadCount; i++) {
            dist[i] = (float) Math.sin(i * 0.37) * 100.0f + (i * 0.01f);
            primIdx[i] = i;
            boxedIdx[i] = i;
        }

        PrimitiveQuadSorter.sortQuadIndicesDescending(dist, primIdx, quadCount);
        Arrays.sort(boxedIdx, Comparator.comparingDouble((Integer idx) -> (double) dist[idx]).reversed());

        for (int i = 0; i < quadCount; i++) {
            Assertions.assertEquals(boxedIdx[i].intValue(), primIdx[i]);
        }

        // Benchmark 5,000 translucent chunk sorts of 256 quads (Vanilla Boxed Integer[] vs Kinetic Primitive int[])
        final int runs = 5_000;
        final int n = 256;
        float[] benchDist = new float[n];
        for (int i = 0; i < n; i++) benchDist[i] = ((i * 73) & 255) * 0.5f;

        long tBoxedStart = System.nanoTime();
        int boxedSink = 0;
        for (int r = 0; r < runs; r++) {
            Integer[] b = new Integer[n];
            for (int i = 0; i < n; i++) b[i] = i;
            Arrays.sort(b, (a, c) -> Float.compare(benchDist[c], benchDist[a]));
            boxedSink += b[0];
        }
        long boxedNs = System.nanoTime() - tBoxedStart;

        long tPrimStart = System.nanoTime();
        int primSink = 0;
        int[] reusableIdx = new int[n];
        for (int r = 0; r < runs; r++) {
            for (int i = 0; i < n; i++) reusableIdx[i] = i;
            PrimitiveQuadSorter.sortQuadIndicesDescending(benchDist, reusableIdx, n);
            primSink += reusableIdx[0];
        }
        long primNs = System.nanoTime() - tPrimStart;

        Assertions.assertEquals(boxedSink, primSink);
        System.out.printf(
            Locale.ROOT,
            "=== v1.4.0 TRANSLUCENT QUAD SORT BENCHMARK (5,000 x 256 Quads) ===%n"
            + "- Vanilla Boxed Integer[] Sort: %.2f ms%n"
            + "- Kinetic Primitive int[] Sort: %.2f ms -> %.2fx speedup (0 Heap Allocations)%n",
            boxedNs / 1_000_000.0,
            primNs / 1_000_000.0,
            (double) boxedNs / Math.max(1L, primNs)
        );
    }

    @Test
    void verifyModelVertexDeduplicatorSharesIdenticalQuadArrays() {
        KineticConfig.modEnabled = true;
        KineticConfig.deduplicateBakedModelQuads = true;
        ModelVertexDeduplicator dedup = ModelVertexDeduplicator.getInstance();
        dedup.clearPool();

        int[] quadA = new int[28];
        int[] quadB = new int[28];
        Arrays.fill(quadA, 0x3F800000);
        Arrays.fill(quadB, 0x3F800000);

        int[] first = dedup.canonicalizeVertices(quadA);
        int[] second = dedup.canonicalizeVertices(quadB);
        Assertions.assertSame(first, second, "Identical BakedQuad int[28] arrays must share canonical reference");
    }

    @Test
    void verifyFastStringInternerReturnsSameReference() {
        KineticConfig.modEnabled = true;
        KineticConfig.internScoreboardAndPacketStrings = true;
        FastStringInterner interner = FastStringInterner.getInstance();
        interner.clear();

        String s1 = new String("§e§lBEDWARS §711/27/26");
        String s2 = new String("§e§lBEDWARS §711/27/26");
        Assertions.assertNotSame(s1, s2);

        String c1 = interner.canonicalize(s1);
        String c2 = interner.canonicalize(s2);
        Assertions.assertSame(c1, c2, "Repeated scoreboard/packet strings must resolve to identical String reference");
    }

    @Test
    void verifyScalarBoxMathAndUndergroundSkyCuller() {
        Assertions.assertTrue(ScalarBoxMath.intersectsOffset(0, 0, 0, 1, 1, 1, 5, 0, 0, 5.5, 0.2, 0.2, 6.5, 1.2, 1.2));
        Assertions.assertFalse(ScalarBoxMath.intersectsOffset(0, 0, 0, 1, 1, 1, 10, 0, 0, 5.5, 0.2, 0.2, 6.5, 1.2, 1.2));

        KineticConfig.modEnabled = true;
        KineticConfig.cullUndergroundSkyAndClouds = true;
        SkyAndCloudCuller culler = SkyAndCloudCuller.getInstance();
        Assertions.assertTrue(culler.shouldCullSkyOrClouds(16.0, 0, true), "Deep cave with 0 sky light should skip sky/clouds");
        Assertions.assertFalse(culler.shouldCullSkyOrClouds(16.0, 8, true), "Open ravine with sky light > 0 must keep sky/clouds");
        Assertions.assertFalse(culler.shouldCullSkyOrClouds(72.0, 0, true), "Surface Y >= 44 must keep sky/clouds");
    }
}
