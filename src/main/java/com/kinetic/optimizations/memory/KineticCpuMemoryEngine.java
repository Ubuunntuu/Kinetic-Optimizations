package com.kinetic.optimizations.memory;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.networking.PacketBurstOptimizer;

/**
 * Master Phase 6 coordinator for FastMath, AllocationReducer, MemoryPoolManager,
 * and PacketBurstOptimizer.
 */
public final class KineticCpuMemoryEngine {

    private static final KineticCpuMemoryEngine INSTANCE = new KineticCpuMemoryEngine();

    private final MemoryPoolManager memoryPoolManager = new MemoryPoolManager();
    private final PacketBurstOptimizer packetBurstOptimizer = new PacketBurstOptimizer();

    private KineticCpuMemoryEngine() {
    }

    public static KineticCpuMemoryEngine getInstance() {
        return INSTANCE;
    }

    public void onClientTick() {
        if (!KineticConfig.modEnabled) {
            return;
        }
        packetBurstOptimizer.onTickStart();
        memoryPoolManager.checkMemoryPressure();
    }

    public MemoryPoolManager getMemoryPoolManager() {
        return memoryPoolManager;
    }

    public PacketBurstOptimizer getPacketBurstOptimizer() {
        return packetBurstOptimizer;
    }
}
