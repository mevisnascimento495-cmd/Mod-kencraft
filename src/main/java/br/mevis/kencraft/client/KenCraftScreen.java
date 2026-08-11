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
    private static final int PANEL_WIDTH = 470;
    private static final int PANEL_HEIGHT = 350;

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

        PlayerData data = currentData();
        addTab(Component.translatable("screen.kencraft.race"), panelLeft + 16, true);
        addTab(Component.translatable("screen.kencraft.jio"), panelLeft + 134, true);
        addTab(Component.translatable("screen.kencraft.kikan"), panelLeft + 252, true);

        if (data.race() == Race.RINKA) {
            addStatusButton("strength", panelTop + 122, data.physicalXp() > 0 && data.strength() < PlayerData.MAX_STATUS);
            addStatusButton("defense", panelTop + 144, data.physicalXp() > 0 && data.defense() < PlayerData.MAX_STATUS);
            addStatusButton("intelligence", panelTop + 166, data.mentalXp() > 0 && data.intelligence() < PlayerData.MAX_STATUS);
            addStatusButton("speed", panelTop + 188, data.physicalXp() > 0 && data.speed() < PlayerData.MAX_STATUS);
            addStatusButton("genetics", panelTop + 210, data.physicalXp() > 0 && data.genetics() < PlayerData.MAX_STATUS);
        } else if (data.race() == Race.HUMAN) {
            addStatusButton("strength", panelTop + 122, data.physicalXp() > 0 && data.strength() < PlayerData.MAX_STATUS);
            addStatusButton("life", panelTop + 144, data.physicalXp() > 0 && data.life() < PlayerData.MAX_STATUS);
            addStatusButton("perception", panelTop + 166, data.mentalXp() > 0 && data.perception() < PlayerData.MAX_STATUS);
            addStatusButton("spiritual", panelTop + 188, data.mentalXp() > 0 && data.spiritualDevelopment() < PlayerData.MAX_STATUS);
            addStatusButton("speed", panelTop + 210, data.physicalXp() > 0 && data.speed() < PlayerData.MAX_STATUS);
        }

        this.addRenderableWidget(Button.builder(
                        Component.literal("Fechar"),
                        button -> this.onClose())
                .bounds(panelLeft + 174, panelTop + 311, 122, 24)
                .build());
    }

    private void addTab(Component text, int x, boolean active) {
        this.addRenderableWidget(Button.builder(text, button -> {})
                .bounds(x, panelTop + 62, 112, 24)
                .build());
    }

    private void addStatusButton(String attribute, int y, boolean enabled) {
        Button button = Button.builder(Component.literal("+"), b -> addStatus(attribute))
                .bounds(panelLeft + 392, y - 2, 28, 18)
                .build();
        button.active = enabled;
        this.addRenderableWidget(button);
    }

    private void addStatus(String attribute) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.connection != null) {
            minecraft.player.connection.sendCommand("kencraft status add " + attribute);
            this.rebuildWidgets();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xE8101018);
        graphics.fill(panelLeft + 1, panelTop + 1, panelLeft + PANEL_WIDTH - 1, panelTop + 2, 0xFF7AD7FF);
        graphics.fill(panelLeft + 1, panelTop + PANEL_HEIGHT - 2, panelLeft + PANEL_WIDTH - 1, panelTop + PANEL_HEIGHT - 1, 0xFF263746);
        graphics.fill(panelLeft + 8, panelTop + 48, panelLeft + PANEL_WIDTH - 8, panelTop + 49, 0xFF263746);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop + 14, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Status por raça • 1 XP = 1 ponto"), this.width / 2, panelTop + 31, 0xFFB8C5D1);
        graphics.fill(panelLeft + PANEL_WIDTH - 52, panelTop + 12, panelLeft + PANEL_WIDTH - 20, panelTop + 40, 0xFF2B89B8);
        graphics.drawCenteredString(this.font, Component.literal("R"), panelLeft + PANEL_WIDTH - 36, panelTop + 20, 0xFFFFFFFF);

        PlayerData data = currentData();
        graphics.fill(panelLeft + 16, panelTop + 96, panelLeft + PANEL_WIDTH - 16, panelTop + 288, 0xFF172531);
        graphics.drawString(this.font, Component.literal("Raça: " + raceName(data.race())), panelLeft + 28, panelTop + 106, 0xFF7AD7FF);

        if (data.race() == Race.RINKA) {
            drawStatusLine(graphics, "Força", data.strength(), panelTop + 122);
            drawStatusLine(graphics, "Defesa/Resistência", data.defense(), panelTop + 144);
            drawStatusLine(graphics, "Inteligência", data.intelligence(), panelTop + 166);
            drawStatusLine(graphics, "Velocidade", data.speed(), panelTop + 188);
            drawStatusLine(graphics, "Genética", data.genetics(), panelTop + 210);
            graphics.drawString(this.font, Component.literal("XP Mental: " + data.mentalXp()), panelLeft + 28, panelTop + 240, 0xFFB8C5D1);
            graphics.drawString(this.font, Component.literal("XP Física: " + data.physicalXp()), panelLeft + 28, panelTop + 255, 0xFFB8C5D1);
            graphics.drawString(this.font, Component.literal("Genética aumenta regeneração e será a base da força da Kikan."), panelLeft + 28, panelTop + 272, 0xFF7AD7FF);
        } else if (data.race() == Race.HUMAN) {
            drawStatusLine(graphics, "Força", data.strength(), panelTop + 122);
            drawStatusLine(graphics, "Vida", data.life(), panelTop + 144);
            drawStatusLine(graphics, "Percepção", data.perception(), panelTop + 166);
            drawStatusLine(graphics, "Desenvolvimento espiritual", data.spiritualDevelopment(), panelTop + 188);
            drawStatusLine(graphics, "Velocidade", data.speed(), panelTop + 210);
            graphics.drawString(this.font, Component.literal("Jio: " + data.jio() + "/" + data.calculatedHumanMaxJio()), panelLeft + 28, panelTop + 240, 0xFF7AD7FF);
            graphics.drawString(this.font, Component.literal("Multiplicador de Jio: " + String.format(java.util.Locale.ROOT, "%.0f%%", (data.jioMultiplier() - 1.0D) * 100.0D)), panelLeft + 28, panelTop + 255, 0xFFB8C5D1);
            graphics.drawString(this.font, Component.literal("XP Mental: " + data.mentalXp() + "   XP Física: " + data.physicalXp()), panelLeft + 28, panelTop + 272, 0xFFB8C5D1);
        } else {
            graphics.drawString(this.font, Component.literal("Escolha Rinka ou Humano para liberar seus status."), panelLeft + 28, panelTop + 122, 0xFFFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private PlayerData currentData() {
        return Minecraft.getInstance().player == null
                ? PlayerData.DEFAULT
                : Minecraft.getInstance().player.getData(ModAttachments.PLAYER_DATA);
    }

    private void drawStatusLine(GuiGraphics graphics, String name, int value, int y) {
        String text = name + ": " + value + "/" + PlayerData.MAX_STATUS + " (" + PlayerData.spentPoints(value) + " points)";
        graphics.drawString(this.font, Component.literal(text), panelLeft + 28, y, 0xFFFFFFFF);
    }

    private String raceName(Race race) {
        return switch (race) {
            case RINKA -> "Rinka";
            case HUMAN -> "Humano";
            case NONE -> "Sem raça";
        };
    }
}
