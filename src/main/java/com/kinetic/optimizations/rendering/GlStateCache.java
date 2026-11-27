package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-speed shadow cache for OpenGL state and texture unit bindings.
 * Eliminates redundant JNI/driver state transitions on hot rendering paths.
 */
public final class GlStateCache {

    private static final int MAX_TEXTURE_UNITS = 8;
    private static final int[] BOUND_TEXTURES = new int[MAX_TEXTURE_UNITS];
    private static int activeTextureUnit = 0;

    private static boolean blendEnabled = false;
    private static boolean depthTestEnabled = false;
    private static boolean cullFaceEnabled = false;
    private static boolean alphaTestEnabled = false;

    private static final AtomicInteger SAVED_CALLS_ACCUMULATOR = new AtomicInteger(0);
    private static volatile int savedCallsPerSecond = 0;
    private static long lastRolloverNs = System.nanoTime();

    static {
        Arrays.fill(BOUND_TEXTURES, -1);
    }

    private GlStateCache() {
    }

    public static void onFrameStart() {
        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            savedCallsPerSecond = SAVED_CALLS_ACCUMULATOR.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    /**
     * Returns true if the texture bind is redundant and can be safely skipped.
     */
    public static boolean shouldSkipBindTexture(int textureId) {
        if (!KineticConfig.modEnabled || !KineticConfig.deduplicateTextureBinds) {
            BOUND_TEXTURES[activeTextureUnit] = textureId;
            return false;
        }
        if (textureId <= 0) {
            BOUND_TEXTURES[activeTextureUnit] = textureId;
            return false;
        }
        int current = BOUND_TEXTURES[activeTextureUnit];
        if (current == textureId) {
            SAVED_CALLS_ACCUMULATOR.incrementAndGet();
            return true;
        }
        BOUND_TEXTURES[activeTextureUnit] = textureId;
        return false;
    }

    public static void onActiveTextureUnitChanged(int glTextureEnum) {
        int index = glTextureEnum - 0x84C0; // GL_TEXTURE0
        if (index >= 0 && index < MAX_TEXTURE_UNITS) {
            activeTextureUnit = index;
        } else {
            activeTextureUnit = 0;
        }
    }

    public static void invalidateTexture(int textureId) {
        for (int i = 0; i < MAX_TEXTURE_UNITS; i++) {
            if (BOUND_TEXTURES[i] == textureId) {
                BOUND_TEXTURES[i] = -1;
            }
        }
    }

    public static void invalidateAll() {
        Arrays.fill(BOUND_TEXTURES, -1);
    }

    public static void recordSavedStateCall() {
        SAVED_CALLS_ACCUMULATOR.incrementAndGet();
    }

    public static int getSavedCallsPerSecond() {
        return savedCallsPerSecond;
    }

    public static boolean isBlendEnabled() {
        return blendEnabled;
    }

    public static void setBlendEnabled(boolean enabled) {
        blendEnabled = enabled;
    }

    public static boolean isDepthTestEnabled() {
        return depthTestEnabled;
    }

    public static void setDepthTestEnabled(boolean enabled) {
        depthTestEnabled = enabled;
    }

    public static boolean isCullFaceEnabled() {
        return cullFaceEnabled;
    }

    public static void setCullFaceEnabled(boolean enabled) {
        cullFaceEnabled = enabled;
    }

    public static boolean isAlphaTestEnabled() {
        return alphaTestEnabled;
    }

    public static void setAlphaTestEnabled(boolean enabled) {
        alphaTestEnabled = enabled;
    }
}
