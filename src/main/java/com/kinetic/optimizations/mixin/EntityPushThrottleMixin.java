package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.entities.EntityCollisionThrottler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityPushThrottleMixin {

    @Shadow
    public World world;

    @Shadow
    public double x;

    @Shadow
    public double y;

    @Shadow
    public double z;

    @Inject(method = "push(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$throttleCrammedClientPush(Entity other, CallbackInfo ci) {
        if (this.world == null || !this.world.isClient) {
            return;
        }
        // Never alter local/remote player collision response
        if (((Object) this) instanceof PlayerEntity || other instanceof PlayerEntity) {
            return;
        }
        if (!EntityCollisionThrottler.getInstance().shouldRunClientPushCheck(this.x, this.y, this.z)) {
            ci.cancel();
        }
    }
}
