package com.kinetic.optimizations.entities;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.rendering.FastFrustumCuller;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-speed entity visibility, squared-distance, and frustum evaluator.
 * Avoids Math.sqrt and skips off-screen or invisible marker armor stands before model preparation.
 */
public final class EntityVisibilityOptimizer {

    private final AtomicInteger totalEvaluatedThisFrame = new AtomicInteger(0);
    private final AtomicInteger renderedThisFrame = new AtomicInteger(0);
    private final AtomicInteger culledThisFrame = new AtomicInteger(0);

    public void onFrameStart() {
        totalEvaluatedThisFrame.set(0);
        renderedThisFrame.set(0);
        culledThisFrame.set(0);
    }

    /**
     * Evaluates whether an entity at (x, y, z) with bounding box [minX..maxZ] should be rendered.
     * Gameplay-important entities (players, bosses, close entities within 16 blocks) are always preserved.
     */
    public boolean shouldRenderEntity(
        double x, double y, double z,
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ,
        boolean isPlayerOrBoss,
        boolean isInvisibleArmorStand
    ) {
        totalEvaluatedThisFrame.incrementAndGet();

        if (!KineticConfig.modEnabled || !KineticConfig.fastEntityVisibility) {
            renderedThisFrame.incrementAndGet();
            return true;
        }

        if (isInvisibleArmorStand && KineticConfig.skipInvisibleArmorStands) {
            culledThisFrame.incrementAndGet();
            return false;
        }

        FastFrustumCuller culler = FastFrustumCuller.getInstance();

        // Non-player/boss entities beyond configured max squared distance are culled early
        if (!isPlayerOrBoss && KineticConfig.optimizeEntityDistanceCalc) {
            double maxDistSq = KineticConfig.maxEntityRenderDistanceSq;
            if (!culler.isWithinRenderDistanceSq(x, y, z, maxDistSq)) {
                culledThisFrame.incrementAndGet();
                return false;
            }
        }

        // Frustum AABB check
        if (!culler.isBoxVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
            culledThisFrame.incrementAndGet();
            return false;
        }

        renderedThisFrame.incrementAndGet();
        return true;
    }

    public void recordEntityRendered() {
        totalEvaluatedThisFrame.incrementAndGet();
        renderedThisFrame.incrementAndGet();
    }

    public void recordEntityCulled() {
        totalEvaluatedThisFrame.incrementAndGet();
        culledThisFrame.incrementAndGet();
    }

    public int getTotalEvaluatedThisFrame() {
        return totalEvaluatedThisFrame.get();
    }

    public int getRenderedThisFrame() {
        return renderedThisFrame.get();
    }

    public int getCulledThisFrame() {
        return culledThisFrame.get();
    }
}
