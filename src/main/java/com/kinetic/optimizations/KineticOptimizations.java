package com.kinetic.optimizations;

import com.kinetic.optimizations.bootstrap.KineticPreLaunch;
import com.kinetic.optimizations.chunks.KineticChunkEngine;
import com.kinetic.optimizations.compatibility.ModCompatibilityManager;
import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.core.KineticMixinPlugin;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.entities.KineticEntityParticleEngine;
import com.kinetic.optimizations.memory.KineticCpuMemoryEngine;
import com.kinetic.optimizations.rendering.KineticRenderOptimizer;
import com.kinetic.optimizations.utilities.KineticLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class KineticOptimizations implements ClientModInitializer {

    public static final String MOD_ID = "kinetic_optimizations";
    public static final String MOD_NAME = "Kinetic Optimizations";
    public static final String MOD_VERSION = "1.1.0";

    private static KineticOptimizations instance;

    @Override
    public void onInitializeClient() {
        instance = this;
        KineticPreLaunch.ensureInitialized();
        ModCompatibilityManager.getInstance().scanEnvironment();

        if (!KineticConfig.modEnabled) {
            KineticLogger.warn("%s is disabled in configuration.", MOD_NAME);
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMb = runtime.maxMemory() / (1024L * 1024L);
        int processors = runtime.availableProcessors();

        KineticLogger.info("Initializing %s v%s [OneClient / Ornithe 1.8.9 Release]", MOD_NAME, MOD_VERSION);
        KineticLogger.info("Runtime: Java %s (%s) | Logical CPUs: %d | Max Heap: %d MB | Loaded Mods: %d",
            Runtime.version(),
            System.getProperty("java.vm.name"),
            processors,
            maxMemoryMb,
            FabricLoader.getInstance().getAllMods().size()
        );
        KineticLogger.info("Active Preset: %s | Safe Mode: %s | Compat Mode: %s | Mixins Applied: %d (Skipped: %d)",
            KineticConfig.activePreset.getDisplayName(),
            KineticMixinPlugin.isSafeModeActive(),
            KineticConfig.compatibilityMode,
            KineticMixinPlugin.getAppliedMixinsCount(),
            KineticMixinPlugin.getSkippedMixinsCount()
        );

        KineticRenderOptimizer.getInstance().onFrameStart();
        KineticChunkEngine.getInstance().onFrameStart();
        KineticEntityParticleEngine.getInstance().onFrameStart();
        KineticCpuMemoryEngine.getInstance().onClientTick();
        KineticDiagnosticsEngine.getInstance().onFrameCompleted();
        KineticDiagnosticsEngine.getInstance().exportSnapshotToDisk();
        KineticLogger.info("Kinetic Optimizations v1.0.0 initialized. Hotkeys: [F7] Settings GUI | [F8] HUD | [F9] Cycle Preset | [F10] 60s Benchmark.");
    }

    public static KineticOptimizations getInstance() {
        return instance;
    }
}
