package com.kinetic.optimizations.compatibility;

public record CompatibilityRule(
    String modIdOrPackage,
    String displayName,
    ConflictCategory category,
    String advisory
) {
    public enum ConflictCategory {
        RENDER_PIPELINE_OVERRIDE,
        CHUNK_ENGINE_OVERRIDE,
        FAST_MATH_OVERRIDE,
        SHADER_PIPELINE,
        COMPATIBLE_COMPANION
    }
}
