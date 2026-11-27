package com.kinetic.optimizations.chunks;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;

/**
 * Frame-paced chunk mesh upload budgeter. Prevents GPU buffer upload bursts
 * from stalling the OpenGL driver and spiking 1% low frame times.
 */
public final class ChunkUploadBudgeter {

    private int uploadsThisFrame = 0;
    private int adaptiveBudget = 4;

    public void onFrameStart() {
        uploadsThisFrame = 0;
        int configuredMax = KineticConfig.maxChunkUploadsPerFrame;
        if (!KineticConfig.modEnabled || !KineticConfig.optimizeChunkRebuildQueue) {
            adaptiveBudget = 16;
            return;
        }

        double lastFrameMs = KineticDiagnosticsEngine.getInstance()
            .getFrameTimeTracker()
            .getLastFrameTimeMs();

        // If the previous frame took > 22ms (<45 FPS), tighten the upload budget to protect frame pacing
        if (lastFrameMs > 22.0 && configuredMax > 2) {
            adaptiveBudget = Math.max(1, configuredMax >> 1);
        } else {
            adaptiveBudget = configuredMax;
        }
    }

    /**
     * Checks if another compiled chunk buffer can be uploaded to the GPU during the current frame.
     */
    public boolean tryConsumeUploadSlot(boolean isUrgentNearPlayer) {
        if (!KineticConfig.modEnabled || !KineticConfig.optimizeChunkRebuildQueue) {
            uploadsThisFrame++;
            return true;
        }
        if (isUrgentNearPlayer && uploadsThisFrame == 0) {
            uploadsThisFrame++;
            return true;
        }
        if (uploadsThisFrame < adaptiveBudget) {
            uploadsThisFrame++;
            return true;
        }
        return false;
    }

    public int getUploadsThisFrame() {
        return uploadsThisFrame;
    }

    public int getAdaptiveBudget() {
        return adaptiveBudget;
    }
}
