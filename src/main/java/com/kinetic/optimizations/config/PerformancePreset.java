package com.kinetic.optimizations.config;

import java.util.Locale;

public enum PerformancePreset {
    BALANCED("Balanced", "Conservative optimizations prioritizing strict vanilla behavior."),
    PERFORMANCE("Performance", "Recommended optimizations for high FPS and smooth frame pacing."),
    EXTREME("Extreme", "Aggressive culling, batching, and memory churn reductions."),
    ULTRA("Ultra", "Enables all verified stable optimizations across all subsystems."),
    CUSTOM("Custom", "User-defined manual feature toggles.");

    private final String displayName;
    private final String description;

    PerformancePreset(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static PerformancePreset fromString(String value) {
        if (value == null || value.isBlank()) {
            return PERFORMANCE;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (PerformancePreset preset : values()) {
            if (preset.name().equals(normalized) || preset.displayName.toUpperCase(Locale.ROOT).equals(normalized)) {
                return preset;
            }
        }
        return PERFORMANCE;
    }
}
