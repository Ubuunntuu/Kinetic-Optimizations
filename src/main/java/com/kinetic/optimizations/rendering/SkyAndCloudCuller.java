package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Skips full-screen sky dome, star VBO, and 3D cloud passes when the camera is deep underground
 * in zero-skylight cave sub-chunks.
 */
public final class SkyAndCloudCuller {

    private static final SkyAndCloudCuller INSTANCE = new SkyAndCloudCuller();

    private final AtomicInteger skippedSkyCloudPassesAccumulator = new AtomicInteger(0);
    private volatile int skippedPassesPerSecond = 0;
    private long lastRolloverNs = System.nanoTime();

    private SkyAndCloudCuller() {
    }

    public static SkyAndCloudCuller getInstance() {
        return INSTANCE;
    }

    public void onFrameStart() {
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            skippedPassesPerSecond = skippedSkyCloudPassesAccumulator.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    public boolean shouldCullSkyOrClouds(double cameraY, int skyLightAtEye, boolean hasSkyInDimension) {
        if (!KineticConfig.modEnabled || !KineticConfig.cullUndergroundSkyAndClouds) {
            return false;
        }
        if (!hasSkyInDimension) {
            return false;
        }
        // Deep underground (< Y=44) with completely zero ambient sky light reaching the eye
        if (cameraY < 44.0 && skyLightAtEye <= 0) {
            skippedSkyCloudPassesAccumulator.lazySet(skippedSkyCloudPassesAccumulator.get() + 1);
            return true;
        }
        return false;
    }

    public int getSkippedPassesPerSecond() {
        return skippedPassesPerSecond;
    }
}
