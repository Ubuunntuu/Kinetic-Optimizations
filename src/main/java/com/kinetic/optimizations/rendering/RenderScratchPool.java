package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Thread-local zero-allocation scratch buffers for OpenGL matrix math,
 * viewport calculations, and vertex staging.
 */
public final class RenderScratchPool {

    private static final ThreadLocal<ScratchBuffers> THREAD_BUFFERS =
        ThreadLocal.withInitial(ScratchBuffers::new);

    private RenderScratchPool() {
    }

    public static ScratchBuffers get() {
        return THREAD_BUFFERS.get();
    }

    public static final class ScratchBuffers {
        public final float[] matrix16 = new float[16];
        public final float[] vector4 = new float[4];
        public final double[] coords3 = new double[3];

        private final FloatBuffer directFloatBuffer16 = ByteBuffer
            .allocateDirect(16 * Float.BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

        private final IntBuffer directIntBuffer16 = ByteBuffer
            .allocateDirect(16 * Integer.BYTES)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();

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
