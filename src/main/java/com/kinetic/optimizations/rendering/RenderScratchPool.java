package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Native Java 25 Foreign Function & Memory (FFM) off-heap scratch pool.
 * Allocates 64-byte CPU cache-line-aligned MemorySegments via Arena.ofAuto()
 * for zero-copy OpenGL matrix/viewport NIO views without DirectByteBuffer cleaner churn.
 */
public final class RenderScratchPool {

    private static final long CPU_CACHE_LINE_ALIGNMENT = 64L;
    private static final ThreadLocal<ScratchBuffers> THREAD_BUFFERS =
        ThreadLocal.withInitial(ScratchBuffers::new);

    private RenderScratchPool() {
    }

    public static ScratchBuffers get() {
        return THREAD_BUFFERS.get();
    }

    public static final class ScratchBuffers {
        private final Arena arena = Arena.ofAuto();

        // 64-byte cache-line aligned off-heap segments (16 floats = 64 bytes = exactly 1 x86-64 cache line!)
        private final MemorySegment floatSegment16 = arena.allocate(16L * Float.BYTES, CPU_CACHE_LINE_ALIGNMENT);
        private final MemorySegment intSegment16 = arena.allocate(16L * Integer.BYTES, CPU_CACHE_LINE_ALIGNMENT);

        private final FloatBuffer directFloatBuffer16 = floatSegment16
            .asByteBuffer()
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

        private final IntBuffer directIntBuffer16 = intSegment16
            .asByteBuffer()
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();

        public final float[] matrix16 = new float[16];
        public final float[] vector4 = new float[4];
        public final double[] coords3 = new double[3];

        public MemorySegment getAlignedFloatSegment16() {
            return floatSegment16;
        }

        public void writeFloatAt(int index, float value) {
            floatSegment16.setAtIndex(ValueLayout.JAVA_FLOAT, index, value);
        }

        public float readFloatAt(int index) {
            return floatSegment16.getAtIndex(ValueLayout.JAVA_FLOAT, index);
        }

        public FloatBuffer getCleanFloatBuffer16() {
            if (!KineticConfig.reuseRenderBuffers) {
                return ByteBuffer.allocateDirect(16 * Float.BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            }
            directFloatBuffer16.clear();
            return directFloatBuffer16;
        }

        public IntBuffer getCleanIntBuffer16() {
            if (!KineticConfig.reuseRenderBuffers) {
                return ByteBuffer.allocateDirect(16 * Integer.BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer();
            }
            directIntBuffer16.clear();
            return directIntBuffer16;
        }
    }
}
