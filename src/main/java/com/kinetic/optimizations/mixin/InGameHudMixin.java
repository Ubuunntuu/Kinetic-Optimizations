package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.gui.PerformanceOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void kinetic$renderPerformanceOverlay(float tickDelta, CallbackInfo ci) {
        if (!KineticConfig.modEnabled || !KineticConfig.showPerformanceOverlay) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        TextRenderer tr = mc.textRenderer;
        if (tr == null) {
            return;
        }
        String[] lines = PerformanceOverlayRenderer.getInstance().getOverlayLines();
        float y = 6.0f;
        for (int i = 0; i < lines.length; i++) {
            int color = (i == 0) ? 0x55FFFF : 0xE0E0E0;
            tr.drawWithShadow(lines[i], 6.0f, y, color);
            y += 10.0f;
        }
    }
}
