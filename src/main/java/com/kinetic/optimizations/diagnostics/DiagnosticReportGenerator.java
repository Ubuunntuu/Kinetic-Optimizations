package com.kinetic.optimizations.diagnostics;

import com.kinetic.optimizations.KineticOptimizations;
import com.kinetic.optimizations.compatibility.ModCompatibilityManager;
import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.core.KineticMixinPlugin;
import com.kinetic.optimizations.performance.FrameTimeTracker;
import com.kinetic.optimizations.performance.MemoryAndGcTracker;
import com.kinetic.optimizations.performance.WorldRenderStats;
import com.kinetic.optimizations.rendering.KineticRenderOptimizer;
import com.kinetic.optimizations.utilities.KineticLogger;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Generates formatted diagnostic telemetry reports for in-game HUD, console logs, and file export.
 */
public final class DiagnosticReportGenerator {

    private DiagnosticReportGenerator() {
    }

    public static String buildCompactHudLine(
        FrameTimeTracker frames,
        MemoryAndGcTracker memory,
        WorldRenderStats world,
        BottleneckAnalyzer analyzer
    ) {
        return String.format(
            Locale.ROOT,
            "[Kinetic] FPS: %.0f (Avg: %.0f | 1%% Low: %.0f) | Frame: %.2f ms (Jitter: %.2f ms) | Mem: %d/%d MB (%.0f%% | %.1f MB/s) | GC: %d (%d ms) | Status: %s",
            frames.getCurrentFps(),
            frames.getAverageFps(),
            frames.getOnePercentLowFps(),
            frames.getLastFrameTimeMs(),
            frames.getFrameTimeStdDevMs(),
            memory.getUsedHeapMb(),
            memory.getMaxHeapMb(),
            memory.getHeapUsagePercent(),
            memory.getAllocationRateMbPerSec(),
            memory.getTotalGcCollections(),
            memory.getTotalGcTimeMs(),
            analyzer.getActiveBottleneck().getLabel()
        );
    }

    public static Path exportDiagnosticReport(
        FrameTimeTracker frames,
        MemoryAndGcTracker memory,
        WorldRenderStats world,
        BottleneckAnalyzer analyzer
    ) {
        Path reportPath = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("kinetic_diagnostics_report.txt");

        BottleneckType bottleneck = analyzer.getActiveBottleneck();
        KineticRenderOptimizer renderOpt = KineticRenderOptimizer.getInstance();
        List<String> compatAdvisories = ModCompatibilityManager.getInstance().getActiveWarnings();

        StringBuilder sb = new StringBuilder(1536);
        sb.append("=== KINETIC OPTIMIZATIONS DIAGNOSTIC REPORT ===\n");
        sb.append("Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append('\n');
        sb.append("Mod Version: ").append(KineticOptimizations.MOD_VERSION).append('\n');
        sb.append("Java Runtime: ").append(Runtime.version()).append(" (").append(System.getProperty("java.vm.name")).append(")\n");
        sb.append("Active Preset: ").append(KineticConfig.activePreset.getDisplayName()).append('\n');
        sb.append("Safe Mode: ").append(KineticMixinPlugin.isSafeModeActive()).append('\n');
        sb.append("Compatibility Mode: ").append(KineticConfig.compatibilityMode).append('\n');
        sb.append("Mixins (Applied / Skipped): ").append(KineticMixinPlugin.getAppliedMixinsCount())
            .append(" / ").append(KineticMixinPlugin.getSkippedMixinsCount()).append("\n\n");

        sb.append("--- Frame Pacing & FPS ---\n");
        sb.append(String.format(Locale.ROOT, "Current FPS: %.1f\n", frames.getCurrentFps()));
        sb.append(String.format(Locale.ROOT, "Average FPS: %.1f\n", frames.getAverageFps()));
        sb.append(String.format(Locale.ROOT, "1%% Low FPS: %.1f\n", frames.getOnePercentLowFps()));
        sb.append(String.format(Locale.ROOT, "Average Frame Time: %.3f ms\n", frames.getAverageFrameTimeMs()));
        sb.append(String.format(Locale.ROOT, "1%% High Frame Time: %.3f ms\n", frames.getOnePercentHighFrameTimeMs()));
        sb.append(String.format(Locale.ROOT, "Frame-Time StdDev (Jitter): %.3f ms\n\n", frames.getFrameTimeStdDevMs()));

        sb.append("--- Memory & Garbage Collection ---\n");
        sb.append(String.format(Locale.ROOT, "Heap Usage: %d MB / %d MB (%.1f%%)\n",
            memory.getUsedHeapMb(), memory.getMaxHeapMb(), memory.getHeapUsagePercent()));
        sb.append(String.format(Locale.ROOT, "Allocation Rate: %.1f MB/s\n", memory.getAllocationRateMbPerSec()));
        sb.append(String.format(Locale.ROOT, "Total GC Collections: %d (%d ms total pause)\n",
            memory.getTotalGcCollections(), memory.getTotalGcTimeMs()));
        sb.append(String.format(Locale.ROOT, "Process CPU Load: %.1f%%\n\n", memory.getProcessCpuLoadPercent()));

        sb.append("--- World & Rendering Subsystems ---\n");
        sb.append(String.format(Locale.ROOT, "Saved OpenGL Calls/s: %d\n", renderOpt.getSavedGlCallsPerSecond()));
        sb.append(String.format(Locale.ROOT, "Culled Objects/s: %d\n", renderOpt.getCulledRenderObjectsPerSecond()));
        sb.append(String.format(Locale.ROOT, "Entities (Rendered / Total / Culled): %d / %d / %d\n",
            world.getRenderedEntities(), world.getTotalEntities(), world.getCulledEntities()));
        sb.append(String.format(Locale.ROOT, "Visible Chunks: %d | Rebuilds/s: %d\n",
            world.getVisibleChunks(), world.getChunkRebuildsPerSecond()));
        sb.append(String.format(Locale.ROOT, "Particles (Active / Culled): %d / %d\n\n",
            world.getActiveParticles(), world.getCulledParticles()));

        sb.append("--- Compatibility Advisories ---\n");
        if (compatAdvisories.isEmpty()) {
            sb.append("No conflicting rendering or coremod transformations detected.\n\n");
        } else {
            for (String note : compatAdvisories) {
                sb.append("- ").append(note).append('\n');
            }
            sb.append('\n');
        }

        sb.append("--- Automatic Bottleneck Analysis ---\n");
        sb.append("Detected Status: ").append(bottleneck.getLabel()).append('\n');
        sb.append("Recommendation: ").append(bottleneck.getRecommendation()).append('\n');

        try {
            Files.createDirectories(reportPath.getParent());
            Files.writeString(reportPath, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            KineticLogger.error("Failed to write diagnostic report to disk.", e);
        }
        return reportPath;
    }
}
