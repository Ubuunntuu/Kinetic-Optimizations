package com.kinetic.optimizations.core;

import com.kinetic.optimizations.bootstrap.KineticPreLaunch;
import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.utilities.KineticLogger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public final class KineticMixinPlugin implements IMixinConfigPlugin {

    private static final boolean JVM_SAFE_MODE = Boolean.getBoolean("kinetic.safemode");
    private static final AtomicInteger APPLIED_MIXINS_COUNT = new AtomicInteger(0);

    @Override
    public void onLoad(String mixinPackage) {
        KineticPreLaunch.ensureInitialized();
        if (isSafeModeActive()) {
            KineticLogger.warn("Safe Mode is ACTIVE. All Kinetic bytecode mixins are bypassed.");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return KineticConfig.modEnabled && !isSafeModeActive();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        APPLIED_MIXINS_COUNT.incrementAndGet();
    }

    public static boolean isSafeModeActive() {
        return JVM_SAFE_MODE || KineticConfig.safeMode;
    }

    public static int getAppliedMixinsCount() {
        return APPLIED_MIXINS_COUNT.get();
    }
}
