package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.memory.DirectionCache;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockModelRenderer.class)
public abstract class BlockModelDirectionMixin {

    @Redirect(
        method = "*",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;values()[Lnet/minecraft/util/math/Direction;"),
        require = 0
    )
    private Direction[] kinetic$reuseCachedDirections() {
        return KineticConfig.cacheEnumFacingValues ? DirectionCache.ALL : Direction.values();
    }
}
