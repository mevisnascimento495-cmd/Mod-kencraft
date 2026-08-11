package br.mevis.kencraft.client;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class KenCraftScreen extends Screen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 290;

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
                .bounds(panelLeft + 16, panelTop + 62, 112, 24)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.kencraft.jio"),
                        button -> {}
                )
                .bounds(panelLeft + 134, panelTop + 62, 112, 24)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.kencraft.kikan"),
                        button -> {}
                )
                .bounds(panelLeft + 252, panelTop + 62, 112, 24)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.kencraft.close"),
                        button -> this.onClose()
                )
                .bounds(panelLeft + 154, panelTop + 250, 122, 24)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xE8101018);
        graphics.fill(panelLeft + 1, panelTop + 1, panelLeft + PANEL_WIDTH - 1, panelTop + 2, 0xFF7AD7FF);
        graphics.fill(panelLeft + 1, panelTop + PANEL_HEIGHT - 2, panelLeft + PANEL_WIDTH - 1, panelTop + PANEL_HEIGHT - 1, 0xFF263746);
        graphics.fill(panelLeft + 8, panelTop + 48, panelLeft + PANEL_WIDTH - 8, panelTop + 49, 0xFF263746);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 14, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.kencraft.subtitle"), this.width / 2, panelTop + 31, 0xFFB8C5D1);

        graphics.fill(panelLeft + PANEL_WIDTH - 52, panelTop + 12, panelLeft + PANEL_WIDTH - 20, panelTop + 40, 0xFF2B89B8);
        graphics.drawCenteredString(this.font, Component.literal("R"), panelLeft + PANEL_WIDTH - 36, panelTop + 20, 0xFFFFFFFF);

        PlayerData data = Minecraft.getInstance().player == null
                ? PlayerData.DEFAULT
                : Minecraft.getInstance().player.getData(ModAttachments.PLAYER_DATA);

        graphics.fill(panelLeft + 16, panelTop + 96, panelLeft + PANEL_WIDTH - 16, panelTop + 224, 0xFF172531);
        graphics.drawString(this.font, Component.literal("Raça: " + raceName(data.race())), panelLeft + 28, panelTop + 106, 0xFF7AD7FF);

        int line = panelTop + 122;
        if (data.race() == Race.RINKA) {
            line = drawStatusLine(graphics, "Força", data.strength(), line);
            line = drawStatusLine(graphics, "Defesa/Resistência", data.defense(), line);
            line = drawStatusLine(graphics, "Inteligência", data.intelligence(), line);
            line = drawStatusLine(graphics, "Velocidade", data.speed(), line);
            line = drawStatusLine(graphics, "Genética", data.genetics(), line);
            graphics.drawString(this.font, Component.literal("XP Mental: " + data.mentalXp()), panelLeft + 238, panelTop + 106, 0xFFB8C5D1);
            graphics.drawString(this.font, Component.literal("XP Física: " + data.physicalXp()), panelLeft + 238, panelTop + 122, 0xFFB8C5D1);
        } else if (data.race() == Race.HUMAN) {
            line = drawStatusLine(graphics, "Força", data.strength(), line);
            line = drawStatusLine(graphics, "Vida", (int) Math.ceil(Minecraft.getInstance().player.getMaxHealth()), line);
            line = drawStatusLine(graphics, "Percepção", data.perception(), line);
            line = drawStatusLine(graphics, "Desenvolvimento espiritual", data.spiritualDevelopment(), line);
            line = drawStatusLine(graphics, "Velocidade", data.speed(), line);
            graphics.drawString(this.font, Component.literal("Jio: " + data.jio() + "/" + data.calculatedHumanMaxJio()), panelLeft + 238, panelTop + 106, 0xFF7AD7FF);
            graphics.drawString(this.font, Component.literal("XP Mental: " + data.mentalXp()), panelLeft + 238, panelTop + 122, 0xFFB8C5D1);
            graphics.drawString(this.font, Component.literal("XP Física: " + data.physicalXp()), panelLeft + 238, panelTop + 138, 0xFFB8C5D1);
        } else {
            graphics.drawString(this.font, Component.literal("Escolha Rinka ou Humano para liberar seus status."), panelLeft + 28, line, 0xFFFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int drawStatusLine(GuiGraphics graphics, String name, int value, int y) {
        String text = name + ": " + value + "/" + PlayerData.MAX_STATUS + " (" + PlayerData.spentPoints(value) + " points)";
        graphics.drawString(this.font, Component.literal(text), panelLeft + 28, y, 0xFFFFFFFF);
        return y + 17;
    }

    private String raceName(Race race) {
        return switch (race) {
            case RINKA -> "Rinka";
            case HUMAN -> "Humano";
            case NONE -> "Sem raça";
        };
    }
}
