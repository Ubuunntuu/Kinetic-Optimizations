package com.kinetic.optimizations.gui;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.config.PerformancePreset;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.utilities.KineticLogger;

import java.lang.reflect.Method;

public final class KineticKeybindController {

    private static final KineticKeybindController INSTANCE = new KineticKeybindController();

    private static final int LWJGL2_KEY_F8 = 66;
    private static final int LWJGL2_KEY_F9 = 67;
    private static final int LWJGL2_KEY_F10 = 68;

    private Method lwjgl2IsKeyDown;
    private boolean reflectionResolved = false;

    private boolean f8WasDown = false;
    private boolean f9WasDown = false;
    private boolean f10WasDown = false;

    private KineticKeybindController() {
    }

    public static KineticKeybindController getInstance() {
        return INSTANCE;
    }

    public void onClientTick() {
        if (!KineticConfig.modEnabled) {
            return;
        }
        resolveInputBridge();
        if (lwjgl2IsKeyDown == null) {
            return;
        }

        boolean f8Down = isKeyDown(LWJGL2_KEY_F8);
        boolean f9Down = isKeyDown(LWJGL2_KEY_F9);
        boolean f10Down = isKeyDown(LWJGL2_KEY_F10);

        if (f8Down && !f8WasDown) {
            KineticConfig.showPerformanceOverlay = !KineticConfig.showPerformanceOverlay;
            KineticConfig.save();
            KineticLogger.info("[Keybind F8] Performance Overlay: %s",
                KineticConfig.showPerformanceOverlay ? "ENABLED" : "DISABLED");
        }

        if (f9Down && !f9WasDown) {
            PerformancePreset next = switch (KineticConfig.activePreset) {
                case BALANCED -> PerformancePreset.PERFORMANCE;
                case PERFORMANCE -> PerformancePreset.EXTREME;
                case EXTREME -> PerformancePreset.ULTRA;
                case ULTRA, CUSTOM -> PerformancePreset.BALANCED;
            };
            KineticConfig.applyPreset(next);
            KineticConfig.save();
            KineticLogger.info("[Keybind F9] Switched Performance Preset to: %s", next.getDisplayName());
        }

        if (f10Down && !f10WasDown) {
            KineticDiagnosticsEngine engine = KineticDiagnosticsEngine.getInstance();
            engine.exportSnapshotToDisk();
            engine.getBenchmarkSession().startSession(engine.getMemoryAndGcTracker());
            KineticLogger.info("[Keybind F10] Exported diagnostic snapshot & started 60s CSV benchmark.");
        }

        f8WasDown = f8Down;
        f9WasDown = f9Down;
        f10WasDown = f10Down;
    }

    private void resolveInputBridge() {
        if (reflectionResolved) {
            return;
        }
        reflectionResolved = true;
        try {
            Class<?> keyboardClass = Class.forName("org.lwjgl.input.Keyboard");
            lwjgl2IsKeyDown = keyboardClass.getMethod("isKeyDown", int.class);
        } catch (Throwable ignored) {
            lwjgl2IsKeyDown = null;
        }
    }

    private boolean isKeyDown(int keyCode) {
        try {
            Object res = lwjgl2IsKeyDown.invoke(null, keyCode);
            return Boolean.TRUE.equals(res);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
