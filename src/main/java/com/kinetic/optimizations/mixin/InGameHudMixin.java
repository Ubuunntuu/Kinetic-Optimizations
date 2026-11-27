package com.kinetic.optimizations.mixin;

import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import com.kinetic.optimizations.gui.PerformanceOverlayRenderer;
import com.kinetic.optimizations.performance.FrameTimeTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void kinetic$renderOverlayAndSparkline(float tickDelta, CallbackInfo ci) {
        if (!KineticConfig.modEnabled || !KineticConfig.showPerformanceOverlay) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.textRenderer == null) {
            return;
        }
        TextRenderer tr = mc.textRenderer;
        String[] lines = PerformanceOverlayRenderer.getInstance().getOverlayLines();
        if (lines.length == 0) {
            return;
        }

        Window window = new Window(mc);
        int screenW = window.getWidth();
        int screenH = window.getHeight();

        int maxLineW = 124;
        for (String line : lines) {
            int w = tr.getWidth(line);
            if (w > maxLineW) maxLineW = w;
        }

        boolean showGraph = KineticConfig.showFrameTimeGraph;
        int totalBlockH = (lines.length * 10) + (showGraph ? 28 : 0);
        int corner = KineticConfig.hudCornerPosition & 3;

        int startX = (corner == 1 || corner == 3) ? Math.max(6, screenW - maxLineW - 8) : 6;
        int startY = (corner == 2 || corner == 3) ? Math.max(6, screenH - totalBlockH - 8) : 6;

        // Subtle translucent backdrop behind text + sparkline
        GuiElement.fill(startX - 3, startY - 3, startX + maxLineW + 3, startY + totalBlockH + 2, 0x66000000);

        float y = startY;
        for (int i = 0; i < lines.length; i++) {
            int color = (i == 0) ? 0x55FFFF : 0xE0E0E0;
            tr.drawWithShadow(lines[i], (float) startX, y, color);
            y += 10.0f;
        }

        // Zero-allocation 120-frame live colored frame-time sparkline bar graph
        if (showGraph) {
            FrameTimeTracker tracker = KineticDiagnosticsEngine.getInstance().getFrameTimeTracker();
            int graphLeft = startX;
            int graphBottom = startY + totalBlockH;
            int graphHeight = 22;
            int graphTop = graphBottom - graphHeight;
            int barCount = 120;

            GuiElement.fill(graphLeft, graphTop, graphLeft + barCount, graphBottom, 0x88101418);
            // 16.67 ms (60 FPS) reference line
            int line60Y = graphBottom - 11;
            GuiElement.fill(graphLeft, line60Y, graphLeft + barCount, line60Y + 1, 0x44FFFFFF);

            int available = Math.min(barCount, tracker.getRecordedSampleCount());
            for (int i = 0; i < available; i++) {
                float ms = tracker.getRecentFrameMs(available - 1 - i);
                int barH = Math.max(1, Math.min(graphHeight, (int) (ms * 0.66f)));
                int barColor;
                if (ms <= 8.5f) {
                    barColor = 0xFF55FF55;      // <= 8.5 ms (120+ FPS): Crisp Green
                } else if (ms <= 16.8f) {
                    barColor = 0xFFFFFF55;      // <= 16.8 ms (60-119 FPS): Amber/Yellow
                } else {
                    barColor = 0xFFFF5555;      // > 16.8 ms (<60 FPS spike): Red
                }
                int bx = graphLeft + (barCount - available) + i;
                GuiElement.fill(bx, graphBottom - barH, bx + 1, graphBottom, barColor);
            }
        }
    }
}
