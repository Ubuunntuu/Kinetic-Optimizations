package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zero-allocation 6-plane view frustum and squared-distance culler for entities,
 * tile entities, and particle emitters.
 */
public final class FastFrustumCuller {

    private static final FastFrustumCuller INSTANCE = new FastFrustumCuller();

    // 6 frustum planes: [planeIndex * 4 + (A, B, C, D)]
    private final float[] planes = new float[24];
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

    public void updateFrustumPlanes(float[] clipMatrix) {
        if (clipMatrix == null || clipMatrix.length < 16) {
            planesValid = false;
            return;
        }

        // Right plane
        setPlane(0, clipMatrix[3] - clipMatrix[0], clipMatrix[7] - clipMatrix[4], clipMatrix[11] - clipMatrix[8], clipMatrix[15] - clipMatrix[12]);
        // Left plane
        setPlane(1, clipMatrix[3] + clipMatrix[0], clipMatrix[7] + clipMatrix[4], clipMatrix[11] + clipMatrix[8], clipMatrix[15] + clipMatrix[12]);
        // Bottom plane
        setPlane(2, clipMatrix[3] + clipMatrix[1], clipMatrix[7] + clipMatrix[5], clipMatrix[11] + clipMatrix[9], clipMatrix[15] + clipMatrix[13]);
        // Top plane
        setPlane(3, clipMatrix[3] - clipMatrix[1], clipMatrix[7] - clipMatrix[5], clipMatrix[11] - clipMatrix[9], clipMatrix[15] - clipMatrix[13]);
        // Far plane
        setPlane(4, clipMatrix[3] - clipMatrix[2], clipMatrix[7] - clipMatrix[6], clipMatrix[11] - clipMatrix[10], clipMatrix[15] - clipMatrix[14]);
        // Near plane
        setPlane(5, clipMatrix[3] + clipMatrix[2], clipMatrix[7] + clipMatrix[6], clipMatrix[11] + clipMatrix[10], clipMatrix[15] + clipMatrix[14]);

        planesValid = true;
    }

    private void setPlane(int planeIdx, float a, float b, float c, float d) {
        float invLen = (float) (1.0 / Math.sqrt(a * a + b * b + c * c));
        int offset = planeIdx << 2;
        planes[offset] = a * invLen;
        planes[offset + 1] = b * invLen;
        planes[offset + 2] = c * invLen;
        planes[offset + 3] = d * invLen;
    }

    /**
     * Fast AABB intersection check without allocating a BoundingBox object.
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

        for (int i = 0; i < 24; i += 4) {
            float a = planes[i];
            float b = planes[i + 1];
            float c = planes[i + 2];
            float d = planes[i + 3];

            float px = a >= 0.0f ? fMaxX : fMinX;
            float py = b >= 0.0f ? fMaxY : fMinY;
            float pz = c >= 0.0f ? fMaxZ : fMinZ;

            if ((a * px + b * py + c * pz + d) < 0.0f) {
                culledObjectsAccumulator.incrementAndGet();
                return false;
            }
        }
        return true;
    }

    /**
     * Squared distance check avoiding Math.sqrt on entity and tile-entity render paths.
     */
    public boolean isWithinRenderDistanceSq(double x, double y, double z, double maxDistanceSq) {
        double dx = x - cameraX;
        double dy = y - cameraY;
        double dz = z - cameraZ;
        boolean within = (dx * dx + dy * dy + dz * dz) <= maxDistanceSq;
        if (!within) {
            culledObjectsAccumulator.incrementAndGet();
        }
        return within;
    }

    /**
     * Fast check for decorative invisible armor stands with no custom name or worn equipment.
     */
    public boolean shouldSkipInvisibleArmorStand(boolean isInvisible, boolean hasCustomName, boolean hasEquipment) {
        if (!KineticConfig.modEnabled || !KineticConfig.skipInvisibleArmorStands) {
            return false;
        }
        if (isInvisible && !hasCustomName && !hasEquipment) {
            culledObjectsAccumulator.incrementAndGet();
            return true;
        }
        return false;
    }

    public int getCulledObjectsPerSecond() {
        return culledObjectsPerSecond;
    }
}
