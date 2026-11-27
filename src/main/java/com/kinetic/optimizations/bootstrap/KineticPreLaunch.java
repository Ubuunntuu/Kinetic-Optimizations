package com.kinetic.optimizations.bootstrap;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.utilities.KineticLogger;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class KineticPreLaunch implements PreLaunchEntrypoint {

    private static volatile boolean initialized = false;

    @Override
    public void onPreLaunch() {
        ensureInitialized();
    }

    public static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;
        KineticConfig.init();
        KineticLogger.info("PreLaunch bootstrap completed (Config: %s, Preset: %s)",
            KineticConfig.getConfigPath().getFileName(),
            KineticConfig.activePreset.getDisplayName());
    }
}
