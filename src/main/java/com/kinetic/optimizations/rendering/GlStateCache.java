package com.kinetic.optimizations.rendering;

import com.kinetic.optimizations.config.KineticConfig;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-speed shadow cache for OpenGL capability states and texture unit bindings.
 * Uses tri-state tracking (-1 = unknown, 0 = disabled, 1 = enabled) with frame-boundary
 * resynchronization so raw GL calls from other mods can never permanently desync state.
 */
public final class GlStateCache {

    private static final int STATE_UNKNOWN = -1;
    private static final int STATE_DISABLED = 0;
    private static final int STATE_ENABLED = 1;

    private static final int MAX_TEXTURE_UNITS = 8;
    private static final int[] BOUND_TEXTURES = new int[MAX_TEXTURE_UNITS];
    private static int activeTextureUnit = 0;

    private static int blendState = STATE_UNKNOWN;
    private static int depthTestState = STATE_UNKNOWN;
    private static int cullState = STATE_UNKNOWN;
    private static int alphaTestState = STATE_UNKNOWN;

    private static final AtomicInteger SAVED_CALLS_ACCUMULATOR = new AtomicInteger(0);
    private static volatile int savedCallsPerSecond = 0;
    private static long lastRolloverNs = System.nanoTime();

    static {
        Arrays.fill(BOUND_TEXTURES, -1);
    }

    private GlStateCache() {
    }

    public static void onFrameStart() {
        // Reset capability tri-states once per frame to prevent external raw GL desync
        blendState = STATE_UNKNOWN;
        depthTestState = STATE_UNKNOWN;
        cullState = STATE_UNKNOWN;
        alphaTestState = STATE_UNKNOWN;

        long now = System.nanoTime();
        if (now - lastRolloverNs >= 1_000_000_000L) {
            savedCallsPerSecond = SAVED_CALLS_ACCUMULATOR.getAndSet(0);
            lastRolloverNs = now;
        }
    }

    public static boolean shouldSkipBindTexture(int textureId) {
        if (!KineticConfig.modEnabled || !KineticConfig.deduplicateTextureBinds || textureId <= 0) {
            BOUND_TEXTURES[activeTextureUnit] = textureId;
            return false;
        }
        if (BOUND_TEXTURES[activeTextureUnit] == textureId) {
            SAVED_CALLS_ACCUMULATOR.incrementAndGet();
            return true;
        }
        BOUND_TEXTURES[activeTextureUnit] = textureId;
        return false;
    }

    public static boolean shouldSkipBlendChange(boolean enable) {
        if (!KineticConfig.modEnabled || !KineticConfig.optimizeStateChanges) {
            blendState = enable ? STATE_ENABLED : STATE_DISABLED;
            return false;
        }
        int target = enable ? STATE_ENABLED : STATE_DISABLED;
        if (blendState == target) {
            SAVED_CALLS_ACCUMULATOR.incrementAndGet();
            return true;
        }
        blendState = target;
        return false;
    }

    public static boolean shouldSkipDepthTestChange(boolean enable) {
        if (!KineticConfig.modEnabled || !KineticConfig.optimizeStateChanges) {
            depthTestState = enable ? STATE_ENABLED : STATE_DISABLED;
            return false;
        }
        int target = enable ? STATE_ENABLED : STATE_DISABLED;
        if (depthTestState == target) {
            SAVED_CALLS_ACCUMULATOR.incrementAndGet();
            return true;
        }
        depthTestState = target;
        return false;
    }

    public static boolean shouldSkipCullChange(boolean enable) {
        if (!KineticConfig.modEnabled || !KineticConfig.optimizeStateChanges) {
            cullState = enable ? STATE_ENABLED : STATE_DISABLED;
            return false;
        }
        int target = enable ? STATE_ENABLED : STATE_DISABLED;
        if (cullState == target) {
            SAVED_CALLS_ACCUMULATOR.incrementAndGet();
            return true;
        }
        cullState = target;
        return false;
    }

    public static boolean shouldSkipAlphaTestChange(boolean enable) {
        if (!KineticConfig.modEnabled || !KineticConfig.optimizeStateChanges) {
            alphaTestState = enable ? STATE_ENABLED : STATE_DISABLED;
            return false;
        }
        int target = enable ? STATE_ENABLED : STATE_DISABLED;
        if (alphaTestState == target) {
            SAVED_CALLS_ACCUMULATOR.incrementAndGet();
            return true;
        }
        alphaTestState = target;
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
        blendState = STATE_UNKNOWN;
        depthTestState = STATE_UNKNOWN;
        cullState = STATE_UNKNOWN;
        alphaTestState = STATE_UNKNOWN;
    }

    public static int getSavedCallsPerSecond() {
        return savedCallsPerSecond;
    }
}
