package com.kinetic.optimizations.entities;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.rendering.FastFrustumCuller;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Frustum and squared-distance culler for BlockEntities (TileEntities: chests, signs, skulls, banners).
 * Global block entities like beacons remain visible at long range.
 */
public final class BlockEntityCullingOptimizer {

    private static final double STANDARD_BLOCK_ENTITY_MAX_DIST_SQ = 4096.0; // 64 blocks squared
    private final AtomicInteger culledBlockEntitiesThisFrame = new AtomicInteger(0);

    public void onFrameStart() {
        culledBlockEntitiesThisFrame.set(0);
    }

    public boolean shouldRenderBlockEntity(int blockX, int blockY, int blockZ, boolean isBeaconOrGlobal) {
        if (!KineticConfig.modEnabled || !KineticConfig.cullHiddenTileEntities || isBeaconOrGlobal) {
            return true;
        }

        FastFrustumCuller culler = FastFrustumCuller.getInstance();
        double centerX = blockX + 0.5;
        double centerY = blockY + 0.5;
        double centerZ = blockZ + 0.5;

        if (!culler.isWithinRenderDistanceSq(centerX, centerY, centerZ, STANDARD_BLOCK_ENTITY_MAX_DIST_SQ)) {
            culledBlockEntitiesThisFrame.incrementAndGet();
            return false;
        }

        if (!culler.isBoxVisible(blockX, blockY, blockZ, blockX + 1.0, blockY + 1.0, blockZ + 1.0)) {
            culledBlockEntitiesThisFrame.incrementAndGet();
            return false;
        }

        return true;
    }

    public int getCulledBlockEntitiesThisFrame() {
        return culledBlockEntitiesThisFrame.get();
    }
}
