package com.kinetic.optimizations.compatibility;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.utilities.KineticLogger;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Detects external rendering, math, shader, and OneClient ecosystem mods.
 * Surgically disables only conflicting Kinetic mixins while keeping compatible subsystems active.
 */
public final class ModCompatibilityManager {

    private static final ModCompatibilityManager INSTANCE = new ModCompatibilityManager();

    private static final List<CompatibilityRule> KNOWN_RULES = List.of(
        new CompatibilityRule("optifine", "OptiFine", CompatibilityRule.ConflictCategory.RENDER_PIPELINE_OVERRIDE,
            "OptiFine detected: bypassing GL capability & chunk mixins to prevent shader/render conflicts."),
        new CompatibilityRule("optifabric", "OptiFabric", CompatibilityRule.ConflictCategory.RENDER_PIPELINE_OVERRIDE,
            "OptiFabric detected: bypassing GL capability & chunk mixins."),
        new CompatibilityRule("sodium", "Sodium / Port", CompatibilityRule.ConflictCategory.CHUNK_ENGINE_OVERRIDE,
            "Sodium chunk renderer detected: yielding ChunkRebuildMixin and GlStateDeduplicationMixin."),
        new CompatibilityRule("celeritas", "Celeritas", CompatibilityRule.ConflictCategory.CHUNK_ENGINE_OVERRIDE,
            "Celeritas renderer detected: yielding ChunkRebuildMixin and GlStateDeduplicationMixin."),
        new CompatibilityRule("veloxcaelo", "VeloxCaelo", CompatibilityRule.ConflictCategory.SHADER_PIPELINE,
            "VeloxCaelo shader pipeline detected: yielding GL capability deduplication to preserve shader state."),
        new CompatibilityRule("iris", "Iris Shaders", CompatibilityRule.ConflictCategory.SHADER_PIPELINE,
            "Iris Shaders detected: yielding GL capability deduplication to preserve custom framebuffers."),
        new CompatibilityRule("betterfps", "BetterFPS", CompatibilityRule.ConflictCategory.FAST_MATH_OVERRIDE,
            "BetterFPS detected: yielding MathHelperFastTrigMixin to avoid duplicate sin/cos hooks."),
        new CompatibilityRule("patcher", "Patcher", CompatibilityRule.ConflictCategory.RENDER_PIPELINE_OVERRIDE,
            "Patcher detected: isolating overlapping entity/GL hooks."),
        new CompatibilityRule("foamfix", "FoamFix", CompatibilityRule.ConflictCategory.COMPATIBLE_COMPANION,
            "FoamFix detected: compatible with Kinetic memory cursor rings."),
        new CompatibilityRule("vanillafix", "VanillaFix", CompatibilityRule.ConflictCategory.COMPATIBLE_COMPANION,
            "VanillaFix detected: compatible crash/texture isolation."),
        new CompatibilityRule("oneconfig", "OneConfig (Polyfrost/OneClient)", CompatibilityRule.ConflictCategory.COMPATIBLE_COMPANION,
            "OneConfig detected: compatible with OneClient HUD & lifecycle.")
    );

    private boolean scanned = false;
    private boolean disableGlStateMixins = false;
    private boolean disableChunkMixins = false;
    private boolean disableFastMathMixin = false;
    private final List<String> activeWarnings = new ArrayList<>();

    private ModCompatibilityManager() {
    }

    public static ModCompatibilityManager getInstance() {
        return INSTANCE;
    }

    public synchronized void scanEnvironment() {
        if (scanned) {
            return;
        }
        scanned = true;
        activeWarnings.clear();

        // Check classloader presence for non-modid agents like OptiFine
        if (isClassPresent("optifine.OptiFineTweaker") || isClassPresent("net.optifine.Config")) {
            applyRule(KNOWN_RULES.getFirst());
        }

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String id = mod.getMetadata().getId().toLowerCase(Locale.ROOT);
            for (CompatibilityRule rule : KNOWN_RULES) {
                if (id.equals(rule.modIdOrPackage()) || id.contains(rule.modIdOrPackage())) {
                    applyRule(rule);
                }
            }
        }
    }

    private void applyRule(CompatibilityRule rule) {
        activeWarnings.add(rule.advisory());
        KineticLogger.info("[Compatibility] %s", rule.advisory());

        if (!KineticConfig.compatibilityMode) {
            return;
        }

        switch (rule.category()) {
            case RENDER_PIPELINE_OVERRIDE -> {
                disableGlStateMixins = true;
                disableChunkMixins = true;
            }
            case CHUNK_ENGINE_OVERRIDE -> {
                disableChunkMixins = true;
                disableGlStateMixins = true;
            }
            case SHADER_PIPELINE -> disableGlStateMixins = true;
            case FAST_MATH_OVERRIDE -> disableFastMathMixin = true;
            case COMPATIBLE_COMPANION -> {
                // Fully compatible; no mixins disabled
            }
        }
    }

    public boolean shouldAllowMixin(String simpleMixinName) {
        scanEnvironment();
        if (!KineticConfig.compatibilityMode) {
            return true;
        }

        if (disableGlStateMixins && ("GlStateDeduplicationMixin".equals(simpleMixinName) || "BufferUploaderMixin".equals(simpleMixinName))) {
            KineticLogger.warn("[Compatibility] Skipping %s due to external render/shader mod.", simpleMixinName);
            return false;
        }
        if (disableChunkMixins && "ChunkRebuildMixin".equals(simpleMixinName)) {
            KineticLogger.warn("[Compatibility] Skipping %s due to external chunk renderer.", simpleMixinName);
            return false;
        }
        if (disableFastMathMixin && "MathHelperFastTrigMixin".equals(simpleMixinName)) {
            KineticLogger.warn("[Compatibility] Skipping %s due to external fast-math mod.", simpleMixinName);
            return false;
        }
        return true;
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, ModCompatibilityManager.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public List<String> getActiveWarnings() {
        return Collections.unmodifiableList(activeWarnings);
    }
}
