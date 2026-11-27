package com.kinetic.optimizations.resources;

import com.kinetic.optimizations.chunks.KineticChunkEngine;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.entities.EntityCollisionThrottler;
import com.kinetic.optimizations.entities.ItemAndXpRenderThrottler;
import com.kinetic.optimizations.lighting.LightingDeduplicator;
import com.kinetic.optimizations.memory.AllocationReducer;
import com.kinetic.optimizations.rendering.GlStateCache;
import com.kinetic.optimizations.utilities.KineticLogger;

/**
 * Resets transient world/lobby caches when switching multiplayer servers, minigame lobbies, or dimensions.
 */
public final class WorldSwitchCleaner {

    private WorldSwitchCleaner() {
    }

    public static void onWorldChanged() {
        GlStateCache.invalidateAll();
        com.kinetic.optimizations.networking.FastStringInterner.getInstance().clear();
        com.kinetic.optimizations.rendering.PrimitiveQuadSorter.getInstance().invalidateSortAnchor();
        KineticChunkEngine.getInstance().onClientTick();
        LightingDeduplicator.getInstance().onTickRollover();
        EntityCollisionThrottler.getInstance().onTickRollover();
        ItemAndXpRenderThrottler.getInstance().onFrameStart();
        AllocationReducer.resetThreadLocalRing();
        KineticDiagnosticsEngine.getInstance().getFrameTimeTracker().reset();
        KineticLogger.debug("World switch detected: reset transient GL, chunk, lighting, and frame-pacing caches.");
    }
}
