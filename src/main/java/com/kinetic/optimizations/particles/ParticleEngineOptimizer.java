package com.kinetic.optimizations.particles;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.rendering.FastFrustumCuller;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Optimizes particle lifecycle caps, squared-distance filtering, and off-screen particle culling.
 * Preserves gameplay-critical particles while preventing particle explosions from tanking 1% low FPS.
 */
public final class ParticleEngineOptimizer {

    private final AtomicInteger activeParticleCount = new AtomicInteger(0);
    private final AtomicInteger culledParticlesThisFrame = new AtomicInteger(0);

    public void onFrameStart() {
        culledParticlesThisFrame.set(0);
    }

    /**
     * Evaluates whether a newly spawned decorative particle should be admitted
     * based on distance from the camera and the active particle cap.
     */
    public boolean shouldSpawnParticle(double x, double y, double z, boolean isCriticalGameplayParticle) {
        if (!KineticConfig.modEnabled) {
            return true;
        }

        int currentActive = activeParticleCount.get();
        int maxAllowed = KineticConfig.maxActiveParticles;

        if (currentActive >= maxAllowed) {
            if (!isCriticalGameplayParticle) {
                culledParticlesThisFrame.incrementAndGet();
                return false;
            }
        }

        if (!isCriticalGameplayParticle) {
            double maxDist = KineticConfig.particleRenderDistance;
            double maxDistSq = maxDist * maxDist;
            if (!FastFrustumCuller.getInstance().isWithinRenderDistanceSq(x, y, z, maxDistSq)) {
                culledParticlesThisFrame.incrementAndGet();
                return false;
            }
        }

        return true;
    }

    /**
     * Evaluates whether an active particle at (x, y, z) should generate vertices this frame.
     */
    public boolean shouldRenderParticle(double x, double y, double z) {
        if (!KineticConfig.modEnabled || !KineticConfig.cullOffscreenParticles) {
            return true;
        }

        double maxDist = KineticConfig.particleRenderDistance;
        double maxDistSq = maxDist * maxDist;
        FastFrustumCuller culler = FastFrustumCuller.getInstance();

        if (!culler.isWithinRenderDistanceSq(x, y, z, maxDistSq)) {
            culledParticlesThisFrame.incrementAndGet();
            return false;
        }

        if (!culler.isBoxVisible(x - 0.25, y - 0.25, z - 0.25, x + 0.25, y + 0.25, z + 0.25)) {
            culledParticlesThisFrame.incrementAndGet();
            return false;
        }

        return true;
    }

    public void setActiveParticleCount(int count) {
        activeParticleCount.set(Math.max(0, count));
    }

    public void incrementActiveParticles() {
        activeParticleCount.incrementAndGet();
    }

    public int getActiveParticleCount() {
        return activeParticleCount.get();
    }

    public int getCulledParticlesThisFrame() {
        return culledParticlesThisFrame.get();
    }
}
