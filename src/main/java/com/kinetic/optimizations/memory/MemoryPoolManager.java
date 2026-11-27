package com.kinetic.optimizations.memory;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.performance.MemoryAndGcTracker;
import com.kinetic.optimizations.utilities.KineticLogger;

/**
 * Monitors heap pressure and trims thread-local scratch pools when heap occupancy
 * exceeds 85% without ever calling stop-the-world System.gc().
 */
public final class MemoryPoolManager {

    private long lastPressureCheckNs = 0L;
    private static final long CHECK_INTERVAL_NS = 2_000_000_000L; // 2 seconds
    private int poolTrimEvents = 0;

    public void checkMemoryPressure() {
        if (!KineticConfig.modEnabled) {
            return;
        }
        long now = System.nanoTime();
        if (now - lastPressureCheckNs < CHECK_INTERVAL_NS) {
            return;
        }
        lastPressureCheckNs = now;

        MemoryAndGcTracker tracker = KineticDiagnosticsEngine.getInstance().getMemoryAndGcTracker();
        if (tracker.getHeapUsagePercent() > 85.0) {
            AllocationReducer.resetThreadLocalRing();
            poolTrimEvents++;
            KineticLogger.debug("High heap pressure (%.1f%%); trimmed thread-local cursor rings (event #%d).",
                tracker.getHeapUsagePercent(), poolTrimEvents);
        }
    }

    public int getPoolTrimEvents() {
        return poolTrimEvents;
    }
}
