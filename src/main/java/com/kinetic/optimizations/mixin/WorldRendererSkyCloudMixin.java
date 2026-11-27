package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.performance.FastMathEngine;
import com.kinetic.optimizations.rendering.SkyAndCloudCuller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererSkyCloudMixin {

    @Unique
    private static boolean kinetic$isDeepUndergroundZeroSkyLight() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return false;
        }
        Entity cam = mc.getCamera();
        ClientWorld world = mc.world;
        if (cam == null || world == null || cam.y >= 44.0) {
            return false;
        }
        int bx = FastMathEngine.fastFloor(cam.x);
        int by = FastMathEngine.fastFloor(cam.y + 1.5);
        int bz = FastMathEngine.fastFloor(cam.z);
        int skyLight = world.getLight(LightType.SKY, new BlockPos(bx, by, bz));
        return SkyAndCloudCuller.getInstance().shouldCullSkyOrClouds(cam.y, skyLight, true);
    }

    @Inject(method = "renderSky(FI)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$cullUndergroundSky(float tickDelta, int anaglyphPass, CallbackInfo ci) {
        if (kinetic$isDeepUndergroundZeroSkyLight()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderClouds(FI)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$cullUndergroundClouds(float tickDelta, int anaglyphPass, CallbackInfo ci) {
        if (kinetic$isDeepUndergroundZeroSkyLight()) {
            ci.cancel();
        }
    }
}
