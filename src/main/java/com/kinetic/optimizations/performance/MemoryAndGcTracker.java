package com.kinetic.optimizations.performance;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;

/**
 * Tracks JVM heap pressure, real-time allocation rate (MB/s), GC pause activity, and CPU load.
 */
public final class MemoryAndGcTracker {

    private static final long SAMPLE_INTERVAL_NS = 500_000_000L; // 500 ms

    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    private long lastSampleTimeNs = 0L;
    private long lastUsedBytes = 0L;
    private long lastGcCount = 0L;
    private long lastGcTimeMs = 0L;

    private volatile long usedHeapMb = 0L;
    private volatile long allocatedHeapMb = 0L;
    private volatile long maxHeapMb = 1L;
    private volatile double heapUsagePercent = 0.0;
    private volatile double allocationRateMbPerSec = 0.0;

    private volatile long totalGcCollections = 0L;
    private volatile long totalGcTimeMs = 0L;
    private volatile long recentGcTimeDeltaMs = 0L;
    private volatile double processCpuLoadPercent = -1.0;

    public void updateIfDue() {
        long now = System.nanoTime();
        if (now - lastSampleTimeNs < SAMPLE_INTERVAL_NS) {
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        long totalBytes = runtime.totalMemory();
        long freeBytes = runtime.freeMemory();
        long maxBytes = runtime.maxMemory();
        long usedBytes = totalBytes - freeBytes;

        this.usedHeapMb = usedBytes / (1024L * 1024L);
        this.allocatedHeapMb = totalBytes / (1024L * 1024L);
        this.maxHeapMb = Math.max(1L, maxBytes / (1024L * 1024L));
        this.heapUsagePercent = ((double) usedBytes / (double) Math.max(1L, maxBytes)) * 100.0;

        long currentGcCount = 0L;
        long currentGcTime = 0L;
        for (int i = 0, size = gcBeans.size(); i < size; i++) {
            GarbageCollectorMXBean bean = gcBeans.get(i);
            long count = bean.getCollectionCount();
            long time = bean.getCollectionTime();
            if (count > 0) {
                currentGcCount += count;
            }
            if (time > 0) {
                currentGcTime += time;
            }
        }

        if (lastSampleTimeNs != 0L) {
            double elapsedSeconds = (now - lastSampleTimeNs) / 1_000_000_000.0;
            long bytesDelta = usedBytes - lastUsedBytes;
            if (bytesDelta > 0 && elapsedSeconds > 0.0) {
                double mbDelta = bytesDelta / (1024.0 * 1024.0);
                this.allocationRateMbPerSec = mbDelta / elapsedSeconds;
            } else if (currentGcCount == lastGcCount) {
                this.allocationRateMbPerSec = 0.0;
            }

            this.recentGcTimeDeltaMs = Math.max(0L, currentGcTime - lastGcTimeMs);
        }

        this.totalGcCollections = currentGcCount;
        this.totalGcTimeMs = currentGcTime;
        this.lastGcCount = currentGcCount;
        this.lastGcTimeMs = currentGcTime;
        this.lastUsedBytes = usedBytes;
        this.lastSampleTimeNs = now;

        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            double load = sunOsBean.getProcessCpuLoad();
            if (load >= 0.0) {
                this.processCpuLoadPercent = load * 100.0;
            }
        }
    }

    public long getUsedHeapMb() {
        return usedHeapMb;
    }

    public long getAllocatedHeapMb() {
        return allocatedHeapMb;
    }

    public long getMaxHeapMb() {
        return maxHeapMb;
    }

    public double getHeapUsagePercent() {
        return heapUsagePercent;
    }

    public double getAllocationRateMbPerSec() {
        return allocationRateMbPerSec;
    }

    public long getTotalGcCollections() {
        return totalGcCollections;
    }

    public long getTotalGcTimeMs() {
        return totalGcTimeMs;
    }

    public long getRecentGcTimeDeltaMs() {
        return recentGcTimeDeltaMs;
    }

    public double getProcessCpuLoadPercent() {
        return processCpuLoadPercent;
    }
}
