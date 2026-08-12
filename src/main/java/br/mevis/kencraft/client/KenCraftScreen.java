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
    private int activeTab = 0;

    public KenCraftScreen() {
        super(Component.translatable("screen.kencraft.title"));
    }

    @Override
    protected void init() {
        super.init();
        panelLeft = (this.width - PANEL_WIDTH) / 2;
        panelTop = (this.height - PANEL_HEIGHT) / 2;

        addTab(Component.translatable("screen.kencraft.race"), panelLeft + 16, 0);
        addTab(Component.translatable("screen.kencraft.jio"), panelLeft + 134, 1);
        addTab(Component.translatable("screen.kencraft.kikan"), panelLeft + 252, 2);

        if (activeTab == 2) initKikanTab();
        else if (activeTab == 1) initJioTab();
        else initStatusTab();

        this.addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> this.onClose())
                .bounds(panelLeft + 174, panelTop + 311, 122, 24).build());
    }

    private void addTab(Component text, int x, int tab) {
        Button button = Button.builder(text, b -> { activeTab = tab; this.rebuildWidgets(); })
                .bounds(x, panelTop + 62, 112, 24).build();
        button.active = activeTab != tab;
        this.addRenderableWidget(button);
    }

    private void initStatusTab() {
        PlayerData data = currentData();
        if (data.race() == Race.RINKA) {
            addStatusButton("strength", "Força", data.strength(), panelTop + 122, data.physicalXp() > 0);
            addStatusButton("defense", "Defesa/Resistência", data.defense(), panelTop + 144, data.physicalXp() > 0);
            addStatusButton("intelligence", "Inteligência", data.intelligence(), panelTop + 166, data.mentalXp() > 0);
            addStatusButton("speed", "Velocidade", data.speed(), panelTop + 188, data.physicalXp() > 0);
            addStatusButton("genetics", "Genética", data.genetics(), panelTop + 210, data.physicalXp() > 0);
        } else if (data.race() == Race.HUMAN) {
            addStatusButton("strength", "Força", data.strength(), panelTop + 122, data.physicalXp() > 0);
            addStatusButton("life", "Vida", data.life(), panelTop + 144, data.physicalXp() > 0);
            addStatusButton("perception", "Percepção", data.perception(), panelTop + 166, data.mentalXp() > 0);
            addStatusButton("spiritual", "Desenvolvimento espiritual", data.spiritualDevelopment(), panelTop + 188, data.mentalXp() > 0);
            addStatusButton("speed", "Velocidade", data.speed(), panelTop + 210, data.physicalXp() > 0);
        }
    }

    private void initJioTab() {
        PlayerData data = currentData();
        Button random = Button.builder(Component.literal("GIRAR TÉCNICA"), b -> randomJio())
                .bounds(panelLeft + 158, panelTop + 215, 154, 28).build();
        random.active = data.race() == Race.HUMAN;
        this.addRenderableWidget(random);
    }

    private void randomJio() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.connection != null) {
            minecraft.player.connection.sendCommand("kencraft jio random");
            this.rebuildWidgets();
        }
    }

    private void initKikanTab() {
        PlayerData data = currentData();
        boolean unlocked = data.canUseKikan();
        Button random = Button.builder(Component.literal("ALEATÓRIO"), b -> randomKikan())
                .bounds(panelLeft + 158, panelTop + 215, 154, 28).build();
        random.active = unlocked && data.race() == Race.RINKA;
        this.addRenderableWidget(random);
    }

    private void randomKikan() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.connection != null) {
            minecraft.player.connection.sendCommand("kencraft kikan random");
            this.rebuildWidgets();
        }
    }

    private void addStatusButton(String attribute, String name, int value, int y, boolean hasXp) {
        String text = name + ": " + value + "/" + PlayerData.MAX_STATUS + " (" + PlayerData.spentPoints(value) + " points)";
        int textWidth = this.font.width(text);
        int buttonX = Math.min(panelLeft + 360, panelLeft + 28 + textWidth + 8);
        Button button = Button.builder(Component.literal("+"), b -> addStatus(attribute))
                .bounds(buttonX, y - 3, 28, 18).build();
        button.active = hasXp && value < PlayerData.MAX_STATUS;
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
        graphics.drawCenteredString(this.font, Component.literal("Progressão KenCraft"), this.width / 2, panelTop + 31, 0xFFB8C5D1);

        PlayerData data = currentData();
        graphics.fill(panelLeft + 16, panelTop + 96, panelLeft + PANEL_WIDTH - 16, panelTop + 288, 0xFF172531);
        graphics.drawString(this.font, Component.literal("Raça: " + raceName(data.race())), panelLeft + 28, panelTop + 106, 0xFF7AD7FF);

        if (activeTab == 2) renderKikan(graphics, data);
        else if (activeTab == 1) renderJio(graphics, data);
        else renderStatus(graphics, data);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderJio(GuiGraphics graphics, PlayerData data) {
        if (data.race() != Race.HUMAN) {
            graphics.drawString(this.font, Component.literal("O Jio é exclusivo dos humanos."), panelLeft + 28, panelTop + 122, 0xFFFFFFFF);
            return;
        }
        graphics.drawString(this.font, Component.literal("Jio: " + data.jio() + "/" + data.calculatedHumanMaxJio()), panelLeft + 28, panelTop + 122, 0xFF7AD7FF);
        graphics.drawString(this.font, Component.literal("Técnica Jio: " + prettyJio(data.jioTechnique())), panelLeft + 28, panelTop + 142, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.literal("Segure Z para carregar Jio."), panelLeft + 28, panelTop + 166, 0xFFB8C5D1);
        graphics.drawString(this.font, Component.literal("Use este botão para girar aleatoriamente sua técnica Jio."), panelLeft + 28, panelTop + 184, 0xFFB8C5D1);
    }

    private void renderStatus(GuiGraphics graphics, PlayerData data) {
        if (data.race() == Race.RINKA) {
            drawStatusLine(graphics, "Força", data.strength(), panelTop + 122);
            drawStatusLine(graphics, "Defesa/Resistência", data.defense(), panelTop + 144);
            drawStatusLine(graphics, "Inteligência", data.intelligence(), panelTop + 166);
            drawStatusLine(graphics, "Velocidade", data.speed(), panelTop + 188);
            drawStatusLine(graphics, "Genética", data.genetics(), panelTop + 210);
            graphics.drawString(this.font, Component.literal("XP Mental: " + data.mentalXp()), panelLeft + 28, panelTop + 240, 0xFFB8C5D1);
            graphics.drawString(this.font, Component.literal("XP Física: " + data.physicalXp()), panelLeft + 28, panelTop + 255, 0xFFB8C5D1);
            graphics.drawString(this.font, Component.literal("Jinsuikaku devoradas: " + data.jinsuikakuConsumed()), panelLeft + 28, panelTop + 272, 0xFF7AD7FF);
        } else if (data.race() == Race.HUMAN) {
            drawStatusLine(graphics, "Força", data.strength(), panelTop + 122);
            drawStatusLine(graphics, "Vida", data.life(), panelTop + 144);
            drawStatusLine(graphics, "Percepção", data.perception(), panelTop + 166);
            drawStatusLine(graphics, "Desenvolvimento espiritual", data.spiritualDevelopment(), panelTop + 188);
            drawStatusLine(graphics, "Velocidade", data.speed(), panelTop + 210);
            graphics.drawString(this.font, Component.literal("Jio: " + data.jio() + "/" + data.calculatedHumanMaxJio()), panelLeft + 28, panelTop + 240, 0xFF7AD7FF);
            graphics.drawString(this.font, Component.literal("XP Mental: " + data.mentalXp() + "   XP Física: " + data.physicalXp()), panelLeft + 28, panelTop + 255, 0xFFB8C5D1);
        } else {
            graphics.drawString(this.font, Component.literal("Escolha Rinka ou Humano para liberar seus status."), panelLeft + 28, panelTop + 122, 0xFFFFFFFF);
        }
    }

    private void renderKikan(GuiGraphics graphics, PlayerData data) {
        if (data.race() != Race.RINKA) {
            graphics.drawString(this.font, Component.literal("A Kikan é exclusiva dos Rinkas."), panelLeft + 28, panelTop + 122, 0xFFFFFFFF);
            return;
        }
        graphics.drawString(this.font, Component.literal("Classe atual: " + data.rinkaClass()), panelLeft + 28, panelTop + 122, 0xFFFF6666);
        graphics.drawString(this.font, Component.literal("Jinsuikaku devoradas: " + data.jinsuikakuConsumed()), panelLeft + 28, panelTop + 140, 0xFFB8C5D1);
        if (!data.canUseKikan()) {
            graphics.drawString(this.font, Component.literal("Você precisa ser Classe C ou superior."), panelLeft + 28, panelTop + 166, 0xFFFFAA66);
            graphics.drawString(this.font, Component.literal("E: 1 • D: 10 • C: 20 Jinsuikaku"), panelLeft + 28, panelTop + 184, 0xFFB8C5D1);
            return;
        }
        graphics.drawString(this.font, Component.literal("Apertei no botão abaixo para girar sua Kikan animal"), panelLeft + 28, panelTop + 160, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.literal("Por exemplo, Cauda de crocodilo, tentáculo e cauda de escorpião."), panelLeft + 28, panelTop + 178, 0xFFB8C5D1);
        graphics.drawString(this.font, Component.literal("Kikan atual: " + prettyKikan(data.kikanType())), panelLeft + 28, panelTop + 260, 0xFF7AD7FF);
    }

    private PlayerData currentData() {
        return Minecraft.getInstance().player == null ? PlayerData.DEFAULT : Minecraft.getInstance().player.getData(ModAttachments.PLAYER_DATA);
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

    private String prettyKikan(String type) {
        return switch (type) {
            case "CROCODILE_TAIL" -> "Cauda de crocodilo";
            case "TENTACLE" -> "Tentáculo";
            case "SCORPION_TAIL" -> "Cauda de escorpião";
            default -> "Nenhuma";
        };
    }

    private String prettyJio(String type) {
        return switch (type) {
            case "REFORCO" -> "Reforço";
            case "RAJADA" -> "Rajada";
            case "BARREIRA" -> "Barreira";
            default -> "Nenhuma";
        };
    }
}
