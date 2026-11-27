package com.kinetic.optimizations.gui;

import com.kinetic.optimizations.KineticOptimizations;
import com.kinetic.optimizations.config.KineticConfig;
import com.kinetic.optimizations.config.PerformancePreset;
import com.kinetic.optimizations.diagnostics.KineticDiagnosticsEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.Locale;

public final class KineticConfigScreen extends Screen {

    private static final int TAB_GENERAL = 0;
    private static final int TAB_RENDER = 1;
    private static final int TAB_CHUNKS = 2;
    private static final int TAB_ENTITIES = 3;
    private static final int TAB_SYSTEM = 4;

    private final Screen parent;
    private int activeTab = TAB_GENERAL;
    private String statusBanner = "";

    public KineticConfigScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void init() {
        this.buttons.clear();

        int centerX = this.width / 2;
        int tabWidth = 76;
        int tabStartX = centerX - ((tabWidth * 5 + 16) / 2);

        this.buttons.add(makeTabButton(1, tabStartX, 26, tabWidth, "General", activeTab == TAB_GENERAL));
        this.buttons.add(makeTabButton(2, tabStartX + (tabWidth + 4), 26, tabWidth, "Render/GPU", activeTab == TAB_RENDER));
        this.buttons.add(makeTabButton(3, tabStartX + (tabWidth + 4) * 2, 26, tabWidth, "Chunks/Light", activeTab == TAB_CHUNKS));
        this.buttons.add(makeTabButton(4, tabStartX + (tabWidth + 4) * 3, 26, tabWidth, "Ent/Drops", activeTab == TAB_ENTITIES));
        this.buttons.add(makeTabButton(5, tabStartX + (tabWidth + 4) * 4, 26, tabWidth, "CPU & HUD", activeTab == TAB_SYSTEM));

        int colLeft = centerX - 155;
        int colRight = centerX + 5;
        int btnW = 150;
        int y0 = 54;
        int stepY = 23;

        switch (activeTab) {
            case TAB_GENERAL -> {
                this.buttons.add(new ButtonWidget(100, colLeft, y0, btnW, 20,
                    toggleText("Kinetic Mod", KineticConfig.modEnabled)));
                this.buttons.add(new ButtonWidget(101, colRight, y0, btnW, 20,
                    "Preset: " + KineticConfig.activePreset.getDisplayName()));
                this.buttons.add(new ButtonWidget(102, colLeft, y0 + stepY, btnW, 20,
                    toggleText("Compat Mode", KineticConfig.compatibilityMode)));
                this.buttons.add(new ButtonWidget(103, colRight, y0 + stepY, btnW, 20,
                    toggleText("Safe Mode (Restart)", KineticConfig.safeMode)));
                this.buttons.add(new ButtonWidget(104, colLeft, y0 + stepY * 2, btnW, 20,
                    toggleText("Unfocused Limiter", KineticConfig.limitUnfocusedFps)));
                this.buttons.add(new ButtonWidget(105, colRight, y0 + stepY * 2, btnW, 20,
                    "Unfocused Cap: " + KineticConfig.unfocusedFpsTarget + " FPS"));
                this.buttons.add(new ButtonWidget(106, centerX - 100, y0 + stepY * 3 + 4, 200, 20,
                    "Reset All Settings to Defaults"));
            }
            case TAB_RENDER -> {
                this.buttons.add(new ButtonWidget(200, colLeft, y0, btnW, 20,
                    toggleText("GL State Cache", KineticConfig.optimizeStateChanges)));
                this.buttons.add(new ButtonWidget(201, colRight, y0, btnW, 20,
                    toggleText("Tex Bind Dedup", KineticConfig.deduplicateTextureBinds)));
                this.buttons.add(new ButtonWidget(202, colLeft, y0 + stepY, btnW, 20,
                    toggleText("Fast Frustum Cull", KineticConfig.fastFrustumCulling)));
                this.buttons.add(new ButtonWidget(203, colRight, y0 + stepY, btnW, 20,
                    toggleText("Skip 0-Vert Draws", KineticConfig.minimizeRedundantBufferUploads)));
                this.buttons.add(new ButtonWidget(204, colLeft, y0 + stepY * 2, btnW, 20,
                    toggleText("Smart Anim Sprites", KineticConfig.smartAnimatedTextures)));
                this.buttons.add(new ButtonWidget(205, colRight, y0 + stepY * 2, btnW, 20,
                    toggleText("Cull TileEntities", KineticConfig.cullHiddenTileEntities)));
                this.buttons.add(new ButtonWidget(206, colLeft, y0 + stepY * 3, btnW, 20,
                    toggleText("Text Width Cache", KineticConfig.cacheTextRendererWidths)));
                this.buttons.add(new ButtonWidget(207, colRight, y0 + stepY * 3, btnW, 20,
                    toggleText("Weather Guard", KineticConfig.optimizeWeatherRendering)));
            }
            case TAB_CHUNKS -> {
                this.buttons.add(new ButtonWidget(300, colLeft, y0, btnW, 20,
                    toggleText("Chunk Dedup (64b)", KineticConfig.deduplicateChunkUpdates)));
                this.buttons.add(new ButtonWidget(301, colRight, y0, btnW, 20,
                    toggleText("Adaptive Upload Cap", KineticConfig.optimizeChunkRebuildQueue)));
                this.buttons.add(new ButtonWidget(303, colLeft, y0 + stepY, btnW, 20,
                    toggleText("Light Dedup (64b)", KineticConfig.deduplicateLightUpdates)));
                this.buttons.add(new ButtonWidget(304, colRight, y0 + stepY, btnW, 20,
                    toggleText("Fluid Dir Cache", KineticConfig.cacheEnumFacingValues)));
                this.buttons.add(new ButtonWidget(302, centerX - 100, y0 + stepY * 2, 200, 20,
                    "Max Chunk Uploads/Frame: " + KineticConfig.maxChunkUploadsPerFrame));
            }
            case TAB_ENTITIES -> {
                this.buttons.add(new ButtonWidget(400, colLeft, y0, btnW, 20,
                    toggleText("Fast Entity Cull", KineticConfig.fastEntityVisibility)));
                this.buttons.add(new ButtonWidget(401, colRight, y0, btnW, 20,
                    toggleText("Skip Invis Stands", KineticConfig.skipInvisibleArmorStands)));
                int distBlocks = (int) Math.round(Math.sqrt(KineticConfig.maxEntityRenderDistanceSq));
                this.buttons.add(new ButtonWidget(402, colLeft, y0 + stepY, btnW, 20,
                    "Entity Range: " + distBlocks + "m"));
                this.buttons.add(new ButtonWidget(403, colRight, y0 + stepY, btnW, 20,
                    toggleText("Cull Offscreen Part.", KineticConfig.cullOffscreenParticles)));
                this.buttons.add(new ButtonWidget(404, colLeft, y0 + stepY * 2, btnW, 20,
                    "Max Particles: " + KineticConfig.maxActiveParticles));
                this.buttons.add(new ButtonWidget(406, colRight, y0 + stepY * 2, btnW, 20,
                    toggleText("Crammed Push Cap", KineticConfig.throttleCrammedEntityCollisions)));
                this.buttons.add(new ButtonWidget(407, colLeft, y0 + stepY * 3, btnW, 20,
                    toggleText("Clumped Item Cap", KineticConfig.throttleClumpedItemsAndXp)));
                this.buttons.add(new ButtonWidget(408, colRight, y0 + stepY * 3, btnW, 20,
                    toggleText("Nametag Dist Cull", KineticConfig.cullDistantNametags)));
            }
            case TAB_SYSTEM -> {
                this.buttons.add(new ButtonWidget(500, colLeft, y0, btnW, 20,
                    toggleText("16KB FastMath LUT", KineticConfig.fastMathLookup)));
                this.buttons.add(new ButtonWidget(501, colRight, y0, btnW, 20,
                    toggleText("Java 25 FFM Arena", KineticConfig.reuseRenderBuffers)));
                this.buttons.add(new ButtonWidget(502, colLeft, y0 + stepY, btnW, 20,
                    toggleText("HUD Overlay (F8)", KineticConfig.showPerformanceOverlay)));
                this.buttons.add(new ButtonWidget(503, colRight, y0 + stepY, btnW, 20,
                    toggleText("Bottleneck Analyzer", KineticConfig.enableBottleneckAnalyzer)));
                this.buttons.add(new ButtonWidget(504, colLeft, y0 + stepY * 2, btnW, 20,
                    "Export Diag Report"));
                this.buttons.add(new ButtonWidget(505, colRight, y0 + stepY * 2, btnW, 20,
                    "Run 60s CSV Benchmark"));
            }
        }

        this.buttons.add(new ButtonWidget(999, centerX - 100, this.height - 26, 200, 20, "Save & Done"));
    }

    private ButtonWidget makeTabButton(int id, int x, int y, int w, String label, boolean selected) {
        ButtonWidget btn = new ButtonWidget(id, x, y, w, 20, selected ? ("[" + label + "]") : label);
        btn.active = !selected;
        return btn;
    }

    private static String toggleText(String label, boolean enabled) {
        return label + ": " + (enabled ? "ON" : "OFF");
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button == null || !button.active) {
            return;
        }
        statusBanner = "";

        switch (button.id) {
            case 1 -> activeTab = TAB_GENERAL;
            case 2 -> activeTab = TAB_RENDER;
            case 3 -> activeTab = TAB_CHUNKS;
            case 4 -> activeTab = TAB_ENTITIES;
            case 5 -> activeTab = TAB_SYSTEM;

            case 100 -> KineticConfig.modEnabled = !KineticConfig.modEnabled;
            case 101 -> {
                PerformancePreset next = switch (KineticConfig.activePreset) {
                    case BALANCED -> PerformancePreset.PERFORMANCE;
                    case PERFORMANCE -> PerformancePreset.EXTREME;
                    case EXTREME -> PerformancePreset.ULTRA;
                    case ULTRA, CUSTOM -> PerformancePreset.BALANCED;
                };
                KineticConfig.applyPreset(next);
            }
            case 102 -> KineticConfig.compatibilityMode = !KineticConfig.compatibilityMode;
            case 103 -> KineticConfig.safeMode = !KineticConfig.safeMode;
            case 104 -> { KineticConfig.limitUnfocusedFps = !KineticConfig.limitUnfocusedFps; KineticConfig.markCustom(); }
            case 105 -> {
                KineticConfig.unfocusedFpsTarget = switch (KineticConfig.unfocusedFpsTarget) {
                    case 10 -> 15;
                    case 15 -> 30;
                    case 30 -> 60;
                    default -> 10;
                };
                KineticConfig.markCustom();
            }
            case 106 -> {
                KineticConfig.resetToDefaults();
                statusBanner = "Restored clean default configuration.";
            }

            case 200 -> { KineticConfig.optimizeStateChanges = !KineticConfig.optimizeStateChanges; KineticConfig.markCustom(); }
            case 201 -> { KineticConfig.deduplicateTextureBinds = !KineticConfig.deduplicateTextureBinds; KineticConfig.markCustom(); }
            case 202 -> { KineticConfig.fastFrustumCulling = !KineticConfig.fastFrustumCulling; KineticConfig.markCustom(); }
            case 203 -> { KineticConfig.minimizeRedundantBufferUploads = !KineticConfig.minimizeRedundantBufferUploads; KineticConfig.markCustom(); }
            case 204 -> { KineticConfig.smartAnimatedTextures = !KineticConfig.smartAnimatedTextures; KineticConfig.markCustom(); }
            case 205 -> { KineticConfig.cullHiddenTileEntities = !KineticConfig.cullHiddenTileEntities; KineticConfig.markCustom(); }
            case 206 -> { KineticConfig.cacheTextRendererWidths = !KineticConfig.cacheTextRendererWidths; KineticConfig.markCustom(); }
            case 207 -> { KineticConfig.optimizeWeatherRendering = !KineticConfig.optimizeWeatherRendering; KineticConfig.markCustom(); }

            case 300 -> { KineticConfig.deduplicateChunkUpdates = !KineticConfig.deduplicateChunkUpdates; KineticConfig.markCustom(); }
            case 301 -> { KineticConfig.optimizeChunkRebuildQueue = !KineticConfig.optimizeChunkRebuildQueue; KineticConfig.markCustom(); }
            case 302 -> {
                KineticConfig.maxChunkUploadsPerFrame = switch (KineticConfig.maxChunkUploadsPerFrame) {
                    case 2 -> 4;
                    case 4 -> 6;
                    case 6 -> 8;
                    case 8 -> 12;
                    default -> 2;
                };
                KineticConfig.markCustom();
            }
            case 303 -> { KineticConfig.deduplicateLightUpdates = !KineticConfig.deduplicateLightUpdates; KineticConfig.markCustom(); }
            case 304 -> { KineticConfig.cacheEnumFacingValues = !KineticConfig.cacheEnumFacingValues; KineticConfig.markCustom(); }

            case 400 -> { KineticConfig.fastEntityVisibility = !KineticConfig.fastEntityVisibility; KineticConfig.markCustom(); }
            case 401 -> { KineticConfig.skipInvisibleArmorStands = !KineticConfig.skipInvisibleArmorStands; KineticConfig.markCustom(); }
            case 402 -> {
                KineticConfig.maxEntityRenderDistanceSq = switch (KineticConfig.maxEntityRenderDistanceSq) {
                    case 1024 -> 2304;
                    case 2304 -> 3025;
                    case 3025 -> 4096;
                    case 4096 -> 9216;
                    default -> 1024;
                };
                KineticConfig.markCustom();
            }
            case 403 -> { KineticConfig.cullOffscreenParticles = !KineticConfig.cullOffscreenParticles; KineticConfig.markCustom(); }
            case 404 -> {
                KineticConfig.maxActiveParticles = switch (KineticConfig.maxActiveParticles) {
                    case 500 -> 1000;
                    case 1000 -> 1500;
                    case 1500 -> 3000;
                    case 3000 -> 4000;
                    default -> 500;
                };
                KineticConfig.markCustom();
            }
            case 406 -> { KineticConfig.throttleCrammedEntityCollisions = !KineticConfig.throttleCrammedEntityCollisions; KineticConfig.markCustom(); }
            case 407 -> { KineticConfig.throttleClumpedItemsAndXp = !KineticConfig.throttleClumpedItemsAndXp; KineticConfig.markCustom(); }
            case 408 -> { KineticConfig.cullDistantNametags = !KineticConfig.cullDistantNametags; KineticConfig.markCustom(); }

            case 500 -> { KineticConfig.fastMathLookup = !KineticConfig.fastMathLookup; KineticConfig.markCustom(); }
            case 501 -> { KineticConfig.reuseRenderBuffers = !KineticConfig.reuseRenderBuffers; KineticConfig.markCustom(); }
            case 502 -> KineticConfig.showPerformanceOverlay = !KineticConfig.showPerformanceOverlay;
            case 503 -> KineticConfig.enableBottleneckAnalyzer = !KineticConfig.enableBottleneckAnalyzer;
            case 504 -> {
                KineticDiagnosticsEngine.getInstance().exportSnapshotToDisk();
                statusBanner = "Saved config/kinetic_diagnostics_report.txt";
            }
            case 505 -> {
                KineticDiagnosticsEngine engine = KineticDiagnosticsEngine.getInstance();
                engine.getBenchmarkSession().startSession(engine.getMemoryAndGcTracker());
                statusBanner = "Started 60-second CSV benchmark session!";
            }

            case 999 -> {
                KineticConfig.save();
                Minecraft.getInstance().openScreen(this.parent);
                return;
            }
        }

        KineticConfig.save();
        this.init();
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        this.renderBackground();
        int centerX = this.width / 2;
        this.drawCenteredString(
            Minecraft.getInstance().textRenderer,
            String.format(Locale.ROOT, "Kinetic Optimizations v%s — Settings [F7]", KineticOptimizations.MOD_VERSION),
            centerX,
            10,
            0x55FFFF
        );

        String summary = KineticDiagnosticsEngine.getInstance().getFormattedSummaryLine();
        this.drawCenteredString(
            Minecraft.getInstance().textRenderer,
            summary,
            centerX,
            this.height - 40,
            0xA0E0A0
        );

        if (!statusBanner.isEmpty()) {
            this.drawCenteredString(
                Minecraft.getInstance().textRenderer,
                statusBanner,
                centerX,
                this.height - 52,
                0xFFFF55
            );
        }

        super.render(mouseX, mouseY, tickDelta);
    }
}
