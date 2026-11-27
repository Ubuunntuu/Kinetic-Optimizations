package com.kinetic.optimizations.networking;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks main-thread inbound packet processing bursts and provides non-intrusive
 * frame-time budgeting without altering packet order or multiplayer authority.
 */
public final class PacketBurstOptimizer {

    private final AtomicInteger packetsProcessedThisTick = new AtomicInteger(0);
    private volatile int peakPacketsPerTick = 0;

    public void onTickStart() {
        int lastCount = packetsProcessedThisTick.getAndSet(0);
        if (lastCount > peakPacketsPerTick) {
            peakPacketsPerTick = lastCount;
        }
    }

    public void recordPacketProcessed() {
        if (KineticConfig.modEnabled && KineticConfig.optimizePacketProcessing) {
            packetsProcessedThisTick.incrementAndGet();
        }
    }

    public boolean isExperiencingPacketBurst() {
        return packetsProcessedThisTick.get() > 200;
    }

    public int getPeakPacketsPerTick() {
        return peakPacketsPerTick;
    }
}
