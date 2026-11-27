package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unrolled 6-plane frustum & squared-distance culler utilizing Java 25 hardware Math.fma
 * (x86-64 FMA3 VFMADD instructions) and zero-allocation register-local plane coefficients.
 */
public final class FastFrustumCuller {

    private static final FastFrustumCuller INSTANCE = new FastFrustumCuller();

    // Unrolled register-friendly plane coefficients (Right, Left, Bottom, Top, Far, Near)
    private float p0a, p0b, p0c, p0d;
    private float p1a, p1b, p1c, p1d;
    private float p2a, p2b, p2c, p2d;
    private float p3a, p3b, p3c, p3d;
    private float p4a, p4b, p4c, p4d;
    private float p5a, p5b, p5c, p5d;

    private double cameraX = 0.0;
    private double cameraY = 0.0;
    private double cameraZ = 0.0;
    private boolean planesValid = false;

    private final AtomicInteger culledObjectsAccumulator = new AtomicInteger(0);
    private volatile int culledObjectsPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    private FastFrustumCuller() {
    }

    public static FastFrustumCuller getInstance() {
        return INSTANCE;
    }

    public void onFrameStart() {
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            culledObjectsPerSecond = culledObjectsAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    public void setCameraPosition(double x, double y, double z) {
        this.cameraX = x;
        this.cameraY = y;
        this.cameraZ = z;
    }

    public void updateFrustumPlanes(float[] m) {
        if (m == null || m.length < 16) {
            planesValid = false;
            return;
        }
        float inv;

        // Plane 0: Right
        float a = m[3] - m[0], b = m[7] - m[4], c = m[11] - m[8], d = m[15] - m[12];
        inv = invLength(a, b, c);
        p0a = a * inv; p0b = b * inv; p0c = c * inv; p0d = d * inv;

        // Plane 1: Left
        a = m[3] + m[0]; b = m[7] + m[4]; c = m[11] + m[8]; d = m[15] + m[12];
        inv = invLength(a, b, c);
        p1a = a * inv; p1b = b * inv; p1c = c * inv; p1d = d * inv;

        // Plane 2: Bottom
        a = m[3] + m[1]; b = m[7] + m[5]; c = m[11] + m[9]; d = m[15] + m[13];
        inv = invLength(a, b, c);
        p2a = a * inv; p2b = b * inv; p2c = c * inv; p2d = d * inv;

        // Plane 3: Top
        a = m[3] - m[1]; b = m[7] - m[5]; c = m[11] - m[9]; d = m[15] - m[13];
        inv = invLength(a, b, c);
        p3a = a * inv; p3b = b * inv; p3c = c * inv; p3d = d * inv;

        // Plane 4: Far
        a = m[3] - m[2]; b = m[7] - m[6]; c = m[11] - m[10]; d = m[15] - m[14];
        inv = invLength(a, b, c);
        p4a = a * inv; p4b = b * inv; p4c = c * inv; p4d = d * inv;

        // Plane 5: Near
        a = m[3] + m[2]; b = m[7] + m[6]; c = m[11] + m[10]; d = m[15] + m[14];
        inv = invLength(a, b, c);
        p5a = a * inv; p5b = b * inv; p5c = c * inv; p5d = d * inv;

        planesValid = true;
    }

    private static float invLength(float a, float b, float c) {
        return (float) (1.0 / Math.sqrt(Math.fma(a, a, Math.fma(b, b, c * c))));
    }

    /**
     * Fully unrolled 6-plane AABB intersection test using hardware FMA3 instructions.
     */
    public boolean isBoxVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (!KineticConfig.modEnabled || !KineticConfig.fastFrustumCulling || !planesValid) {
            return true;
        }

        float fMinX = (float) (minX - cameraX);
        float fMinY = (float) (minY - cameraY);
        float fMinZ = (float) (minZ - cameraZ);
        float fMaxX = (float) (maxX - cameraX);
        float fMaxY = (float) (maxY - cameraY);
        float fMaxZ = (float) (maxZ - cameraZ);

        if (planeOutside(p0a, p0b, p0c, p0d, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ)
            || planeOutside(p1a, p1b, p1c, p1d, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ)
            || planeOutside(p2a, p2b, p2c, p2d, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ)
            || planeOutside(p3a, p3b, p3c, p3d, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ)
            || planeOutside(p4a, p4b, p4c, p4d, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ)
            || planeOutside(p5a, p5b, p5c, p5d, fMinX, fMinY, fMinZ, fMaxX, fMaxY, fMaxZ)) {
            culledObjectsAccumulator.lazySet(culledObjectsAccumulator.get() + 1);
            return false;
        }
        return true;
    }

    private static boolean planeOutside(
        float a, float b, float c, float d,
        float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ
    ) {
        float px = a >= 0.0f ? maxX : minX;
        float py = b >= 0.0f ? maxY : minY;
        float pz = c >= 0.0f ? maxZ : minZ;
        return Math.fma(a, px, Math.fma(b, py, Math.fma(c, pz, d))) < 0.0f;
    }

    public boolean isWithinRenderDistanceSq(double x, double y, double z, double maxDistanceSq) {
        double dx = x - cameraX;
        double dy = y - cameraY;
        double dz = z - cameraZ;
        boolean within = Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)) <= maxDistanceSq;
        if (!within) {
            culledObjectsAccumulator.lazySet(culledObjectsAccumulator.get() + 1);
        }
        return within;
    }

    public boolean shouldSkipInvisibleArmorStand(boolean isInvisible, boolean hasCustomName, boolean hasEquipment) {
        if (!KineticConfig.modEnabled || !KineticConfig.skipInvisibleArmorStands) {
            return false;
        }
        if (isInvisible && !hasCustomName && !hasEquipment) {
            culledObjectsAccumulator.lazySet(culledObjectsAccumulator.get() + 1);
            return true;
        }
        return false;
    }

    public int getCulledObjectsPerSecond() {
        return culledObjectsPerSecond;
    }
}
