package com.kinetic.optimizations.chunks;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.rendering.FastFrustumCuller;

/**
 * Evaluates 16x16x16 sub-chunk visibility and calculates camera-priority weights
 * so nearby forward-facing chunks compile and render before peripheral chunks.
 */
public final class ChunkVisibilityOptimizer {

    /**
     * Fast check for whether a 16x16x16 sub-chunk starting at (originX, originY, originZ)
     * intersects the view frustum and lies within the active render distance sphere.
     */
    public boolean isSubChunkVisible(int originX, int originY, int originZ, double maxRenderDistSq) {
        if (!KineticConfig.modEnabled) {
            return true;
        }

        FastFrustumCuller culler = FastFrustumCuller.getInstance();
        double centerX = originX + 8.0;
        double centerY = originY + 8.0;
        double centerZ = originZ + 8.0;

        if (!culler.isWithinRenderDistanceSq(centerX, centerY, centerZ, maxRenderDistSq)) {
            return false;
        }

        return culler.isBoxVisible(
            originX, originY, originZ,
            originX + 16.0, originY + 16.0, originZ + 16.0
        );
    }

    /**
     * Computes a squared distance priority score (lower = higher priority).
     * Chunks within 24 blocks of the player are marked urgent for immediate compilation.
     */
    public boolean isUrgentNearCamera(double cameraX, double cameraY, double cameraZ, int originX, int originY, int originZ) {
        double dx = (originX + 8.0) - cameraX;
        double dy = (originY + 8.0) - cameraY;
        double dz = (originZ + 8.0) - cameraZ;
        return (dx * dx + dy * dy + dz * dz) <= 576.0; // 24 blocks squared
    }
}
