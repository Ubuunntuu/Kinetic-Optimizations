package com.kinetic.optimizations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kinetic.optimizations.utilities.KineticLogger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KineticConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile Path cachedConfigPath;
    private static KineticConfigData data = new KineticConfigData();

    // 1. General
    public static boolean modEnabled = true;
    public static PerformancePreset activePreset = PerformancePreset.PERFORMANCE;
    public static boolean safeMode = false;
    public static boolean compatibilityMode = true;

    // 2. Rendering
    public static boolean optimizeStateChanges = true;
    public static boolean fastFrustumCulling = true;
    public static boolean cullHiddenTileEntities = true;
    public static boolean skipInvisibleArmorStands = true;
    public static boolean smartAnimatedTextures = true;
    public static boolean cacheTextRendererWidths = true;
    public static boolean optimizeWeatherRendering = true;
    public static boolean cullUndergroundSkyAndClouds = true;
    public static boolean primitiveTranslucentSort = true;

    // 3. Chunks & Lighting
    public static boolean optimizeChunkRebuildQueue = true;
    public static boolean deduplicateChunkUpdates = true;
    public static boolean deduplicateLightUpdates = true;
    public static int maxChunkUploadsPerFrame = 4;

    // 4. Entities, Drops & Nametags
    public static boolean fastEntityVisibility = true;
    public static boolean optimizeEntityDistanceCalc = true;
    public static boolean throttleCrammedEntityCollisions = true;
    public static boolean throttleClumpedItemsAndXp = true;
    public static int maxRenderedDropsPerBlockCell = 8;
    public static boolean cullDistantNametags = true;
    public static int maxNametagRenderDistanceSq = 2304;
    public static int maxEntityRenderDistanceSq = 4096;

    // 5. Particles
    public static boolean batchParticleRendering = true;
    public static boolean cullOffscreenParticles = true;
    public static int maxActiveParticles = 4000;
    public static int particleRenderDistance = 48;

    // 6. Memory & Models
    public static boolean reduceBlockPosAllocations = true;
    public static boolean cacheEnumFacingValues = true;
    public static boolean reuseRenderBuffers = true;
    public static boolean deduplicateBakedModelQuads = true;

    // 7. CPU
    public static boolean fastMathLookup = true;
    public static boolean optimizeClientTickLoop = true;

    // 8. GPU
    public static boolean deduplicateTextureBinds = true;
    public static boolean minimizeRedundantBufferUploads = true;
    public static boolean limitUnfocusedFps = true;
    public static int unfocusedFpsTarget = 15;

    // 9. Networking & Strings
    public static boolean optimizePacketProcessing = true;
    public static boolean reducePacketAllocations = true;
    public static boolean internScoreboardAndPacketStrings = true;

    // 10. Advanced
    public static boolean enableExperimentalOptimizations = false;
    public static boolean aggressiveOcclusionCulling = false;

    // 11. Diagnostics, Sparkline & HUD Corner (0=Top-Left, 1=Top-Right, 2=Bottom-Left, 3=Bottom-Right)
    public static boolean showPerformanceOverlay = false;
    public static boolean showFrameTimeGraph = true;
    public static int hudCornerPosition = 0;
    public static boolean enableBottleneckAnalyzer = true;
    public static boolean debugLogging = false;
    public static boolean runSixtySecondBenchmark = false;

    private KineticConfig() {
    }

    public static String getHudCornerLabel() {
        return switch (hudCornerPosition & 3) {
            case 1 -> "Top-Right";
            case 2 -> "Bottom-Left";
            case 3 -> "Bottom-Right";
            default -> "Top-Left";
        };
    }

    private static Path resolveConfigPath() {
        Path existing = cachedConfigPath;
        if (existing != null) {
            return existing;
        }
        try {
            FabricLoader loader = FabricLoader.getInstance();
            if (loader != null && loader.getConfigDir() != null) {
                cachedConfigPath = loader.getConfigDir().resolve("kinetic_optimizations.json");
                return cachedConfigPath;
            }
        } catch (Throwable ignored) {
        }
        cachedConfigPath = Path.of("build", "test-config", "kinetic_optimizations.json");
        return cachedConfigPath;
    }

    public static synchronized void init() {
        Path path = resolveConfigPath();
        if (Files.exists(path)) {
            load();
        } else {
            applyPreset(PerformancePreset.PERFORMANCE);
            save();
        }
    }

    public static synchronized void load() {
        Path path = resolveConfigPath();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            KineticConfigData loaded = GSON.fromJson(reader, KineticConfigData.class);
            if (loaded != null) {
                data = loaded;
            }
        } catch (Exception e) {
            KineticLogger.error("Failed to read kinetic_optimizations.json; restoring defaults.", e);
            data = new KineticConfigData();
        }

        if (data.resetToDefaults) {
            KineticLogger.info("resetToDefaults requested; restoring clean default configuration.");
            data = new KineticConfigData();
        }

        syncDataToStaticFlags();
        save();
    }

    public static synchronized void save() {
        Path path = resolveConfigPath();
        syncStaticFlagsToData();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            KineticLogger.error("Failed to save kinetic_optimizations.json", e);
        }
    }

    public static synchronized void resetToDefaults() {
        data = new KineticConfigData();
        syncDataToStaticFlags();
        save();
    }

    private static void syncDataToStaticFlags() {
        modEnabled = data.modEnabled;
        activePreset = PerformancePreset.fromString(data.performancePreset);
        safeMode = data.safeMode;
        compatibilityMode = data.compatibilityMode;

        optimizeStateChanges = data.optimizeStateChanges;
        fastFrustumCulling = data.fastFrustumCulling;
        cullHiddenTileEntities = data.cullHiddenTileEntities;
        skipInvisibleArmorStands = data.skipInvisibleArmorStands;
        smartAnimatedTextures = data.smartAnimatedTextures;
        cacheTextRendererWidths = data.cacheTextRendererWidths;
        optimizeWeatherRendering = data.optimizeWeatherRendering;
        cullUndergroundSkyAndClouds = data.cullUndergroundSkyAndClouds;
        primitiveTranslucentSort = data.primitiveTranslucentSort;

        optimizeChunkRebuildQueue = data.optimizeChunkRebuildQueue;
        deduplicateChunkUpdates = data.deduplicateChunkUpdates;
        deduplicateLightUpdates = data.deduplicateLightUpdates;
        maxChunkUploadsPerFrame = clamp(data.maxChunkUploadsPerFrame, 1, 16);

        fastEntityVisibility = data.fastEntityVisibility;
        optimizeEntityDistanceCalc = data.optimizeEntityDistanceCalc;
        throttleCrammedEntityCollisions = data.throttleCrammedEntityCollisions;
        throttleClumpedItemsAndXp = data.throttleClumpedItemsAndXp;
        maxRenderedDropsPerBlockCell = clamp(data.maxRenderedDropsPerBlockCell, 2, 32);
        cullDistantNametags = data.cullDistantNametags;
        maxNametagRenderDistanceSq = clamp(data.maxNametagRenderDistanceSq, 256, 16384);
        maxEntityRenderDistanceSq = clamp(data.maxEntityRenderDistanceSq, 256, 65536);

        batchParticleRendering = data.batchParticleRendering;
        cullOffscreenParticles = data.cullOffscreenParticles;
        maxActiveParticles = clamp(data.maxActiveParticles, 100, 16000);
        particleRenderDistance = clamp(data.particleRenderDistance, 8, 256);

        reduceBlockPosAllocations = data.reduceBlockPosAllocations;
        cacheEnumFacingValues = data.cacheEnumFacingValues;
        reuseRenderBuffers = data.reuseRenderBuffers;
        deduplicateBakedModelQuads = data.deduplicateBakedModelQuads;

        fastMathLookup = data.fastMathLookup;
        optimizeClientTickLoop = data.optimizeClientTickLoop;

        deduplicateTextureBinds = data.deduplicateTextureBinds;
        minimizeRedundantBufferUploads = data.minimizeRedundantBufferUploads;
        limitUnfocusedFps = data.limitUnfocusedFps;
        unfocusedFpsTarget = clamp(data.unfocusedFpsTarget, 5, 60);

        optimizePacketProcessing = data.optimizePacketProcessing;
        reducePacketAllocations = data.reducePacketAllocations;
        internScoreboardAndPacketStrings = data.internScoreboardAndPacketStrings;

        enableExperimentalOptimizations = data.enableExperimentalOptimizations;
        aggressiveOcclusionCulling = data.aggressiveOcclusionCulling;

        showPerformanceOverlay = data.showPerformanceOverlay;
        showFrameTimeGraph = data.showFrameTimeGraph;
        hudCornerPosition = clamp(data.hudCornerPosition, 0, 3);
        enableBottleneckAnalyzer = data.enableBottleneckAnalyzer;
        debugLogging = data.debugLogging;
        runSixtySecondBenchmark = data.runSixtySecondBenchmark;

        if (activePreset != PerformancePreset.CUSTOM) {
            applyPreset(activePreset);
        }

        if (!enableExperimentalOptimizations) {
            aggressiveOcclusionCulling = false;
        }

        KineticLogger.setDebugEnabled(debugLogging);
    }

    private static void syncStaticFlagsToData() {
        data.modEnabled = modEnabled;
        data.performancePreset = activePreset.name();
        data.safeMode = safeMode;
        data.compatibilityMode = compatibilityMode;
        data.resetToDefaults = false;

        data.optimizeStateChanges = optimizeStateChanges;
        data.fastFrustumCulling = fastFrustumCulling;
        data.cullHiddenTileEntities = cullHiddenTileEntities;
        data.skipInvisibleArmorStands = skipInvisibleArmorStands;
        data.smartAnimatedTextures = smartAnimatedTextures;
        data.cacheTextRendererWidths = cacheTextRendererWidths;
        data.optimizeWeatherRendering = optimizeWeatherRendering;
        data.cullUndergroundSkyAndClouds = cullUndergroundSkyAndClouds;
        data.primitiveTranslucentSort = primitiveTranslucentSort;

        data.optimizeChunkRebuildQueue = optimizeChunkRebuildQueue;
        data.deduplicateChunkUpdates = deduplicateChunkUpdates;
        data.deduplicateLightUpdates = deduplicateLightUpdates;
        data.maxChunkUploadsPerFrame = maxChunkUploadsPerFrame;

        data.fastEntityVisibility = fastEntityVisibility;
        data.optimizeEntityDistanceCalc = optimizeEntityDistanceCalc;
        data.throttleCrammedEntityCollisions = throttleCrammedEntityCollisions;
        data.throttleClumpedItemsAndXp = throttleClumpedItemsAndXp;
        data.maxRenderedDropsPerBlockCell = maxRenderedDropsPerBlockCell;
        data.cullDistantNametags = cullDistantNametags;
        data.maxNametagRenderDistanceSq = maxNametagRenderDistanceSq;
        data.maxEntityRenderDistanceSq = maxEntityRenderDistanceSq;

        data.batchParticleRendering = batchParticleRendering;
        data.cullOffscreenParticles = cullOffscreenParticles;
        data.maxActiveParticles = maxActiveParticles;
        data.particleRenderDistance = particleRenderDistance;

        data.reduceBlockPosAllocations = reduceBlockPosAllocations;
        data.cacheEnumFacingValues = cacheEnumFacingValues;
        data.reuseRenderBuffers = reuseRenderBuffers;
        data.deduplicateBakedModelQuads = deduplicateBakedModelQuads;

        data.fastMathLookup = fastMathLookup;
        data.optimizeClientTickLoop = optimizeClientTickLoop;

        data.deduplicateTextureBinds = deduplicateTextureBinds;
        data.minimizeRedundantBufferUploads = minimizeRedundantBufferUploads;
        data.limitUnfocusedFps = limitUnfocusedFps;
        data.unfocusedFpsTarget = unfocusedFpsTarget;

        data.optimizePacketProcessing = optimizePacketProcessing;
        data.reducePacketAllocations = reducePacketAllocations;
        data.internScoreboardAndPacketStrings = internScoreboardAndPacketStrings;

        data.enableExperimentalOptimizations = enableExperimentalOptimizations;
        data.aggressiveOcclusionCulling = aggressiveOcclusionCulling;

        data.showPerformanceOverlay = showPerformanceOverlay;
        data.showFrameTimeGraph = showFrameTimeGraph;
        data.hudCornerPosition = hudCornerPosition;
        data.enableBottleneckAnalyzer = enableBottleneckAnalyzer;
        data.debugLogging = debugLogging;
        data.runSixtySecondBenchmark = runSixtySecondBenchmark;
    }

    public static void applyPreset(PerformancePreset preset) {
        activePreset = preset;
        switch (preset) {
            case BALANCED -> {
                optimizeStateChanges = true;
                fastFrustumCulling = true;
                cullHiddenTileEntities = false;
                skipInvisibleArmorStands = false;
                smartAnimatedTextures = true;
                cacheTextRendererWidths = true;
                optimizeWeatherRendering = false;
                cullUndergroundSkyAndClouds = true;
                primitiveTranslucentSort = true;
                optimizeChunkRebuildQueue = true;
                deduplicateChunkUpdates = true;
                deduplicateLightUpdates = true;
                maxChunkUploadsPerFrame = 3;
                fastEntityVisibility = true;
                optimizeEntityDistanceCalc = true;
                throttleCrammedEntityCollisions = false;
                throttleClumpedItemsAndXp = true;
                maxRenderedDropsPerBlockCell = 12;
                cullDistantNametags = false;
                maxNametagRenderDistanceSq = 4096;
                maxEntityRenderDistanceSq = 4096;
                batchParticleRendering = true;
                cullOffscreenParticles = true;
                maxActiveParticles = 4000;
                particleRenderDistance = 64;
                reduceBlockPosAllocations = true;
                cacheEnumFacingValues = true;
                reuseRenderBuffers = false;
                deduplicateBakedModelQuads = true;
                fastMathLookup = false;
                optimizeClientTickLoop = true;
                deduplicateTextureBinds = true;
                minimizeRedundantBufferUploads = true;
                limitUnfocusedFps = true;
                unfocusedFpsTarget = 30;
                optimizePacketProcessing = true;
                reducePacketAllocations = false;
                internScoreboardAndPacketStrings = true;
                enableExperimentalOptimizations = false;
                aggressiveOcclusionCulling = false;
            }
            case PERFORMANCE -> {
                optimizeStateChanges = true;
                fastFrustumCulling = true;
                cullHiddenTileEntities = true;
                skipInvisibleArmorStands = true;
                smartAnimatedTextures = true;
                cacheTextRendererWidths = true;
                optimizeWeatherRendering = true;
                cullUndergroundSkyAndClouds = true;
                primitiveTranslucentSort = true;
                optimizeChunkRebuildQueue = true;
                deduplicateChunkUpdates = true;
                deduplicateLightUpdates = true;
                maxChunkUploadsPerFrame = 4;
                fastEntityVisibility = true;
                optimizeEntityDistanceCalc = true;
                throttleCrammedEntityCollisions = true;
                throttleClumpedItemsAndXp = true;
                maxRenderedDropsPerBlockCell = 8;
                cullDistantNametags = true;
                maxNametagRenderDistanceSq = 2304;
                maxEntityRenderDistanceSq = 4096;
                batchParticleRendering = true;
                cullOffscreenParticles = true;
                maxActiveParticles = 3000;
                particleRenderDistance = 48;
                reduceBlockPosAllocations = true;
                cacheEnumFacingValues = true;
                reuseRenderBuffers = true;
                deduplicateBakedModelQuads = true;
                fastMathLookup = true;
                optimizeClientTickLoop = true;
                deduplicateTextureBinds = true;
                minimizeRedundantBufferUploads = true;
                limitUnfocusedFps = true;
                unfocusedFpsTarget = 15;
                optimizePacketProcessing = true;
                reducePacketAllocations = true;
                internScoreboardAndPacketStrings = true;
                enableExperimentalOptimizations = false;
                aggressiveOcclusionCulling = false;
            }
            case EXTREME -> {
                optimizeStateChanges = true;
                fastFrustumCulling = true;
                cullHiddenTileEntities = true;
                skipInvisibleArmorStands = true;
                smartAnimatedTextures = true;
                cacheTextRendererWidths = true;
                optimizeWeatherRendering = true;
                cullUndergroundSkyAndClouds = true;
                primitiveTranslucentSort = true;
                optimizeChunkRebuildQueue = true;
                deduplicateChunkUpdates = true;
                deduplicateLightUpdates = true;
                maxChunkUploadsPerFrame = 6;
                fastEntityVisibility = true;
                optimizeEntityDistanceCalc = true;
                throttleCrammedEntityCollisions = true;
                throttleClumpedItemsAndXp = true;
                maxRenderedDropsPerBlockCell = 5;
                cullDistantNametags = true;
                maxNametagRenderDistanceSq = 1600;
                maxEntityRenderDistanceSq = 3025;
                batchParticleRendering = true;
                cullOffscreenParticles = true;
                maxActiveParticles = 1500;
                particleRenderDistance = 32;
                reduceBlockPosAllocations = true;
                cacheEnumFacingValues = true;
                reuseRenderBuffers = true;
                deduplicateBakedModelQuads = true;
                fastMathLookup = true;
                optimizeClientTickLoop = true;
                deduplicateTextureBinds = true;
                minimizeRedundantBufferUploads = true;
                limitUnfocusedFps = true;
                unfocusedFpsTarget = 15;
                optimizePacketProcessing = true;
                reducePacketAllocations = true;
                internScoreboardAndPacketStrings = true;
                enableExperimentalOptimizations = false;
                aggressiveOcclusionCulling = false;
            }
            case ULTRA -> {
                optimizeStateChanges = true;
                fastFrustumCulling = true;
                cullHiddenTileEntities = true;
                skipInvisibleArmorStands = true;
                smartAnimatedTextures = true;
                cacheTextRendererWidths = true;
                optimizeWeatherRendering = true;
                cullUndergroundSkyAndClouds = true;
                primitiveTranslucentSort = true;
                optimizeChunkRebuildQueue = true;
                deduplicateChunkUpdates = true;
                deduplicateLightUpdates = true;
                maxChunkUploadsPerFrame = 8;
                fastEntityVisibility = true;
                optimizeEntityDistanceCalc = true;
                throttleCrammedEntityCollisions = true;
                throttleClumpedItemsAndXp = true;
                maxRenderedDropsPerBlockCell = 3;
                cullDistantNametags = true;
                maxNametagRenderDistanceSq = 1024;
                maxEntityRenderDistanceSq = 2304;
                batchParticleRendering = true;
                cullOffscreenParticles = true;
                maxActiveParticles = 1000;
                particleRenderDistance = 24;
                reduceBlockPosAllocations = true;
                cacheEnumFacingValues = true;
                reuseRenderBuffers = true;
                deduplicateBakedModelQuads = true;
                fastMathLookup = true;
                optimizeClientTickLoop = true;
                deduplicateTextureBinds = true;
                minimizeRedundantBufferUploads = true;
                limitUnfocusedFps = true;
                unfocusedFpsTarget = 10;
                optimizePacketProcessing = true;
                reducePacketAllocations = true;
                internScoreboardAndPacketStrings = true;
                enableExperimentalOptimizations = false;
                aggressiveOcclusionCulling = false;
            }
            case CUSTOM -> {
            }
        }
    }

    public static void markCustom() {
        activePreset = PerformancePreset.CUSTOM;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Path getConfigPath() {
        return resolveConfigPath();
    }

    public static final class KineticConfigData {
        public boolean modEnabled = true;
        public String performancePreset = PerformancePreset.PERFORMANCE.name();
        public boolean safeMode = false;
        public boolean compatibilityMode = true;
        public boolean resetToDefaults = false;

        public boolean optimizeStateChanges = true;
        public boolean fastFrustumCulling = true;
        public boolean cullHiddenTileEntities = true;
        public boolean skipInvisibleArmorStands = true;
        public boolean smartAnimatedTextures = true;
        public boolean cacheTextRendererWidths = true;
        public boolean optimizeWeatherRendering = true;
        public boolean cullUndergroundSkyAndClouds = true;
        public boolean primitiveTranslucentSort = true;

        public boolean optimizeChunkRebuildQueue = true;
        public boolean deduplicateChunkUpdates = true;
        public boolean deduplicateLightUpdates = true;
        public int maxChunkUploadsPerFrame = 4;

        public boolean fastEntityVisibility = true;
        public boolean optimizeEntityDistanceCalc = true;
        public boolean throttleCrammedEntityCollisions = true;
        public boolean throttleClumpedItemsAndXp = true;
        public int maxRenderedDropsPerBlockCell = 8;
        public boolean cullDistantNametags = true;
        public int maxNametagRenderDistanceSq = 2304;
        public int maxEntityRenderDistanceSq = 4096;

        public boolean batchParticleRendering = true;
        public boolean cullOffscreenParticles = true;
        public int maxActiveParticles = 4000;
        public int particleRenderDistance = 48;

        public boolean reduceBlockPosAllocations = true;
        public boolean cacheEnumFacingValues = true;
        public boolean reuseRenderBuffers = true;
        public boolean deduplicateBakedModelQuads = true;

        public boolean fastMathLookup = true;
        public boolean optimizeClientTickLoop = true;

        public boolean deduplicateTextureBinds = true;
        public boolean minimizeRedundantBufferUploads = true;
        public boolean limitUnfocusedFps = true;
        public int unfocusedFpsTarget = 15;

        public boolean optimizePacketProcessing = true;
        public boolean reducePacketAllocations = true;
        public boolean internScoreboardAndPacketStrings = true;

        public boolean enableExperimentalOptimizations = false;
        public boolean aggressiveOcclusionCulling = false;

        public boolean showPerformanceOverlay = false;
        public boolean showFrameTimeGraph = true;
        public int hudCornerPosition = 0;
        public boolean enableBottleneckAnalyzer = true;
        public boolean debugLogging = false;
        public boolean runSixtySecondBenchmark = false;
    }
}
