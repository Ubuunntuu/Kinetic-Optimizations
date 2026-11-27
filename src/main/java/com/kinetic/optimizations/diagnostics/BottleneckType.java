package com.kinetic.optimizations.diagnostics;

public enum BottleneckType {
    NONE("Optimal", "Frame pacing and resource utilization are healthy."),
    MEMORY_PRESSURE("Memory Pressure", "Heap usage exceeds 85%. Consider increasing max heap or enabling Memory optimizations."),
    EXCESSIVE_GC("Excessive Garbage Collection", "High allocation churn is triggering GC pauses and hurting 1% low FPS."),
    CHUNK_REBUILD_SPIKE("Chunk Rebuild Bottleneck", "Heavy chunk compilation activity is causing frame-time spikes."),
    EXCESSIVE_ENTITIES("Excessive Entity Rendering", "High rendered entity count is bottlenecking CPU render preparation."),
    EXCESSIVE_PARTICLES("Excessive Particle Load", "High active particle count is increasing vertex buffer upload overhead."),
    CPU_MAIN_THREAD_BOUND("CPU Main-Thread Bound", "High frame-time variance detected on the client render/tick thread."),
    GPU_OR_DRIVER_BOUND("GPU / OpenGL State Bound", "Consistently high frame duration with low CPU variance.");

    private final String label;
    private final String recommendation;

    BottleneckType(String label, String recommendation) {
        this.label = label;
        this.recommendation = recommendation;
    }

    public String getLabel() {
        return label;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
