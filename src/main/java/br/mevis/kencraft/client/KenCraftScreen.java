package br.mevis.kencraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class KenCraftScreen extends Screen {
    private static final int PANEL_WIDTH = 330;
    private static final int PANEL_HEIGHT = 220;

    private int panelLeft;
    private int panelTop;

    public KenCraftScreen() {
        super(Component.translatable("screen.kencraft.title"));
    }

    @Override
    protected void init() {
        super.init();

        panelLeft = (this.width - PANEL_WIDTH) / 2;
        panelTop = (this.height - PANEL_HEIGHT) / 2;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.kencraft.race"),
                        button -> {}
                )
                .bounds(panelLeft + 16, panelTop + 62, 92, 24)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.kencraft.jio"),
                        button -> {}
                )
                .bounds(panelLeft + 118, panelTop + 62, 92, 24)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.kencraft.kikan"),
                        button -> {}
                )
                .bounds(panelLeft + 220, panelTop + 62, 94, 24)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.kencraft.close"),
                        button -> this.onClose()
                )
                .bounds(panelLeft + 104, panelTop + 174, 122, 24)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // NeoForge/Minecraft 1.21.1 in this build environment exposes
        // Screen#renderBackground with the full render signature.
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xE8101018);
        graphics.fill(panelLeft + 1, panelTop + 1, panelLeft + PANEL_WIDTH - 1, panelTop + 2, 0xFF7AD7FF);
        graphics.fill(panelLeft + 1, panelTop + PANEL_HEIGHT - 2, panelLeft + PANEL_WIDTH - 1, panelTop + PANEL_HEIGHT - 1, 0xFF263746);
        graphics.fill(panelLeft + 8, panelTop + 48, panelLeft + PANEL_WIDTH - 8, panelTop + 49, 0xFF263746);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 14, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.kencraft.subtitle"), this.width / 2, panelTop + 31, 0xFFB8C5D1);

        // R is the selected/default key for this menu.
        graphics.fill(panelLeft + PANEL_WIDTH - 52, panelTop + 12, panelLeft + PANEL_WIDTH - 20, panelTop + 40, 0xFF2B89B8);
        graphics.drawCenteredString(this.font, Component.literal("R"), panelLeft + PANEL_WIDTH - 36, panelTop + 20, 0xFFFFFFFF);

        // The first tab is visually selected in this Alpha.
        graphics.fill(panelLeft + 16, panelTop + 88, panelLeft + 314, panelTop + 142, 0xFF172531);
        graphics.drawString(this.font, Component.translatable("screen.kencraft.selected"), panelLeft + 26, panelTop + 98, 0xFF7AD7FF);
        graphics.drawString(this.font, Component.translatable("screen.kencraft.race_info"), panelLeft + 26, panelTop + 116, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.kencraft.r_info"), panelLeft + 26, panelTop + 132, 0xFFB8C5D1);

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
