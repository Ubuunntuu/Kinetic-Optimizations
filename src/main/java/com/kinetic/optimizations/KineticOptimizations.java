package com.kinetic.optimizations;

import com.kinetic.optimizations.bootstrap.KineticPreLaunch;
import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.core.KineticMixinPlugin;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.utilities.KineticLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class KineticOptimizations implements ClientModInitializer {

    public static final String MOD_ID = "kinetic_optimizations";
    public static final String MOD_NAME = "Kinetic Optimizations";
    public static final String MOD_VERSION = "0.2.0-alpha";

    private static KineticOptimizations instance;

    @Override
    public void onInitializeClient() {
        instance = this;
        KineticPreLaunch.ensureInitialized();

        if (!KineticConfig.modEnabled) {
            KineticLogger.warn("%s is disabled in configuration.", MOD_NAME);
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMb = runtime.maxMemory() / (1024L * 1024L);
        int processors = runtime.availableProcessors();

        KineticLogger.info("Initializing %s v%s [OneClient / Ornithe 1.8.9]", MOD_NAME, MOD_VERSION);
        KineticLogger.info("Runtime: Java %s (%s) | Logical CPUs: %d | Max Heap: %d MB | Loaded Mods: %d",
            Runtime.version(),
            System.getProperty("java.vm.name"),
            processors,
            maxMemoryMb,
            FabricLoader.getInstance().getAllMods().size()
        );
        KineticLogger.info("Active Preset: %s | Safe Mode: %s | Compatibility Mode: %s | Applied Mixins: %d",
            KineticConfig.activePreset.getDisplayName(),
            KineticMixinPlugin.isSafeModeActive(),
            KineticConfig.compatibilityMode,
            KineticMixinPlugin.getAppliedMixinsCount()
        );

        // Initialize Phase 2 Diagnostics subsystem & export initial baseline report
        KineticDiagnosticsEngine.getInstance().onFrameCompleted();
        KineticDiagnosticsEngine.getInstance().exportSnapshotToDisk();
        KineticLogger.info("Phase 2 Performance Diagnostics & Bottleneck Analyzer active.");
    }

    public static KineticOptimizations getInstance() {
        return instance;
    }
}
