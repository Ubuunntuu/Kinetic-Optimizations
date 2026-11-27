package com.kinetic.optimizations.performance;

import com.kinetic.optimizations.config.KineticConfig;

import java.lang.reflect.Method;
import java.util.concurrent.locks.LockSupport;

/**
 * Automatically throttles frame rate when the Minecraft window is unfocused or minimized,
 * dropping background GPU/CPU power draw to near-idle and restoring uncapped FPS immediately on focus.
 */
public final class UnfocusedFpsLimiter {

    private static final UnfocusedFpsLimiter INSTANCE = new UnfocusedFpsLimiter();

    private Method displayIsActiveMethod;
    private boolean reflectionResolved = false;
    private long nextAllowedFrameTimeNs = 0L;

    private UnfocusedFpsLimiter() {
    }

    public static UnfocusedFpsLimiter getInstance() {
        return INSTANCE;
    }

    public void throttleIfWindowUnfocused() {
        if (!KineticConfig.modEnabled || !KineticConfig.limitUnfocusedFps) {
            return;
        }
        if (isWindowActive()) {
            nextAllowedFrameTimeNs = 0L;
            return;
        }

        int targetFps = Math.max(5, Math.min(60, KineticConfig.unfocusedFpsTarget));
        long targetFrameDurationNs = 1_000_000_000L / targetFps;
        long now = System.nanoTime();

        if (nextAllowedFrameTimeNs == 0L) {
            nextAllowedFrameTimeNs = now + targetFrameDurationNs;
            return;
        }

        long sleepNs = nextAllowedFrameTimeNs - now;
        if (sleepNs > 500_000L && sleepNs < 1_000_000_000L) {
            LockSupport.parkNanos(sleepNs);
        }
        nextAllowedFrameTimeNs = System.nanoTime() + targetFrameDurationNs;
    }

    private boolean isWindowActive() {
        if (!reflectionResolved) {
            reflectionResolved = true;
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
            Object res = displayIsActiveMethod.invoke(null);
            return !Boolean.FALSE.equals(res);
        } catch (Throwable ignored) {
            return true;
        }
    }
}
