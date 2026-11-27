package com.kinetic.optimizations.performance;

/**
 * L1-cache-resident (16 KB) trigonometric lookup table and branchless fast math operations.
 * Replaces vanilla's 256 KB SIN_TABLE to reduce L1D cache thrashing on hot render/tick loops.
 */
public final class FastMathEngine {

    private static final int LUT_BITS = 12;
    private static final int LUT_SIZE = 1 << LUT_BITS; // 4096 entries = 16 KB (fits in 32 KB L1D cache)
    private static final int LUT_MASK = LUT_SIZE - 1;
    private static final float RAD_TO_INDEX = (float) (LUT_SIZE / (Math.PI * 2.0));
    private static final int COS_PHASE_OFFSET = LUT_SIZE >> 2; // 1024

    private static final float[] SIN_LUT = new float[LUT_SIZE];

    static {
        for (int i = 0; i < LUT_SIZE; i++) {
            SIN_LUT[i] = (float) Math.sin((i * Math.PI * 2.0) / LUT_SIZE);
        }
        // Enforce exact cardinal values
        SIN_LUT[0] = 0.0f;
        SIN_LUT[COS_PHASE_OFFSET] = 1.0f;
        SIN_LUT[COS_PHASE_OFFSET << 1] = 0.0f;
        SIN_LUT[COS_PHASE_OFFSET * 3] = -1.0f;
    }

    private FastMathEngine() {
    }

    public static float sin(float radians) {
        return SIN_LUT[(int) (radians * RAD_TO_INDEX) & LUT_MASK];
    }

    public static float cos(float radians) {
        return SIN_LUT[((int) (radians * RAD_TO_INDEX) + COS_PHASE_OFFSET) & LUT_MASK];
    }

    public static int fastFloor(double value) {
        int xi = (int) value;
        return value < xi ? xi - 1 : xi;
    }

    public static int fastFloor(float value) {
        int xi = (int) value;
        return value < xi ? xi - 1 : xi;
    }

    public static double fastInvSqrt(double x) {
        double xhalf = 0.5d * x;
        long i = Double.doubleToRawLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= (1.5d - xhalf * x * x);
        return x;
    }
}
