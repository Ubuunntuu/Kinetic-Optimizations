package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.networking.FastStringInterner;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public abstract class PacketStringInternMixin {

    @Inject(method = "readString(I)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private void kinetic$internDecodedPacketString(int maxLength, CallbackInfoReturnable<String> cir) {
        String decoded = cir.getReturnValue();
        if (decoded != null) {
            cir.setReturnValue(FastStringInterner.getInstance().canonicalize(decoded));
        }
    }
}
