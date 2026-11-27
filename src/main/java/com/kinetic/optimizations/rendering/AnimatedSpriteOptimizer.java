package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;

import java.lang.reflect.Method;

/**
 * Prevents redundant animated texture PCIe sub-image uploads when the window is unfocused
 * and paces atlas tick work during severe frame-time spikes (>35 ms).
 */
public final class AnimatedSpriteOptimizer {

    private static final AnimatedSpriteOptimizer INSTANCE = new AnimatedSpriteOptimizer();

    private Method displayIsActiveMethod;
    private boolean resolved = false;
    private int tickParity = 0;

    private AnimatedSpriteOptimizer() {
    }

    public static AnimatedSpriteOptimizer getInstance() {
        return INSTANCE;
    }

    public boolean shouldSkipAtlasAnimationTick() {
        if (!KineticConfig.modEnabled || !KineticConfig.smartAnimatedTextures) {
            return false;
        }
        // Skip all animated texture PCIe uploads when the game window is unfocused
        if (!isWindowActive()) {
            GlStateCache.onFrameStart();
            return true;
        }

        // During extreme frame-time spikes (>35 ms / <28 FPS), half-rate background sprite uploads
        double lastFrameMs = KineticDiagnosticsEngine.getInstance().getFrameTimeTracker().getLastFrameTimeMs();
        if (lastFrameMs > 35.0) {
            tickParity = (tickParity + 1) & 1;
            return tickParity == 1;
        }
        return false;
    }

    private boolean isWindowActive() {
        if (!resolved) {
            resolved = true;
            try {
                Class<?> displayClass = Class.forName("org.lwjgl.opengl.Display");
                displayIsActiveMethod = displayClass.getMethod("isActive");
            } catch (Throwable ignored) {
                displayIsActiveMethod = null;
            }
        }
        if (displayIsActiveMethod == null) {
            return true;
        }
        try {
            return !Boolean.FALSE.equals(displayIsActiveMethod.invoke(null));
        } catch (Throwable ignored) {
            return true;
        }
    }
}
