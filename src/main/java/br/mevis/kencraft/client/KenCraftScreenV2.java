package br.mevis.kencraft.client;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.SpiritualState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class KenCraftScreenV2 extends Screen {
    private static final int W = 420;
    private static final int H = 320;
    private int left;
    private int top;
    private int tab = 0;
    private int shopChoice = 0;

    public KenCraftScreenV2() { super(Component.literal("KenCraft")); }

    @Override
    protected void init() {
        super.init();
        left = (width - W) / 2;
        top = (height - H) / 2;
        addRenderableWidget(Button.builder(Component.literal("Status"), b -> { tab = 0; rebuildWidgets(); }).bounds(left + 20, top + 52, 90, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Jio"), b -> { tab = 1; rebuildWidgets(); }).bounds(left + 115, top + 52, 90, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Kikan"), b -> { tab = 2; rebuildWidgets(); }).bounds(left + 210, top + 52, 90, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Loja de Pontos"), b -> { tab = 3; shopChoice = 0; rebuildWidgets(); }).bounds(left + 305, top + 52, 95, 22).build());
        switch (tab) {
            case 0 -> initStatus();
            case 1 -> initJio();
            case 2 -> initKikan();
            case 3 -> initPointShop();
        }
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose()).bounds(left + 150, top + 286, 120, 22).build());
    }

    private void initStatus() {
        PlayerData d = data();
        String[] attrs = d.race() == Race.RINKA ? new String[]{"strength","defense","intelligence","speed","genetics"} : new String[]{"strength","life","perception","spiritual","speed"};
        int y = top + 112;
        for (String attr : attrs) {
            int value = valueOf(d, attr);
            boolean mental = attr.equals("intelligence") || attr.equals("perception") || attr.equals("spiritual");
            boolean hasXp = mental ? d.mentalXp() > 0 : d.physicalXp() > 0;
            Button plus = Button.builder(Component.literal("+"), b -> command("kencraft status add " + attr)).bounds(left + 340, y - 3, 24, 18).build();
            plus.active = hasXp && value < PlayerData.MAX_STATUS;
            addRenderableWidget(plus);
            y += 28;
        }
    }

    private void initJio() {
        PlayerData d = data();
        String technique = PlayerData.normalizeTechnique(d.jioTechnique());
        boolean canJio = d.race() == Race.HUMAN || d.race() == Race.HYBRID || d.race() == Race.JASHIN;
        if (canJio && "NONE".equals(technique) && (d.race() != Race.HUMAN || d.arfClass() >= 4)) addRenderableWidget(Button.builder(Component.literal("GIRAR TÉCNICA JIO"), b -> command("kencraftjio roll")).bounds(left + 125, top + 230, 170, 24).build());
        if (canJio && !"NONE".equals(technique)) {
            SpiritualState state = Minecraft.getInstance().player.getData(ModAttachments.SPIRITUAL_STATE);
            boolean classReady = d.race() != Race.HUMAN || d.arfClass() >= 2;
            String label = state.isSujo() ? "DESATIVAR SUJO" : (classReady ? "ATIVAR ESTADO SUJO" : "SUJO — SEGUNDA CLASSE");
            Button button = Button.builder(Component.literal(label), b -> command("kencraftjio sujo")).bounds(left + 110, top + 214, 200, 24).build();
            button.active = classReady || state.isSujo();
            addRenderableWidget(button);
        }
    }

    private void initKikan() {
        PlayerData d = data();
        if ((d.race() == Race.RINKA || d.race() == Race.HYBRID) && d.canUseKikan() && "NONE".equals(d.kikanType())) addRenderableWidget(Button.builder(Component.literal("ALEATÓRIO"), b -> command("kencraft kikan random")).bounds(left + 140, top + 230, 140, 24).build());
    }

    private void initPointShop() {
        if (shopChoice == 0) {
            addRenderableWidget(Button.builder(Component.literal("Humano"), b -> { shopChoice = 1; rebuildWidgets(); }).bounds(left + 90, top + 145, 110, 28).build());
            addRenderableWidget(Button.builder(Component.literal("Rinka"), b -> { shopChoice = 2; rebuildWidgets(); }).bounds(left + 220, top + 145, 110, 28).build());
        } else if (shopChoice == 1) {
            addRenderableWidget(Button.builder(Component.literal("Reset de Técnica Jio — 100 XP Mental"), b -> { chat("kencraft loja pontos humano"); onClose(); }).bounds(left + 55, top + 145, 310, 28).build());
            addRenderableWidget(Button.builder(Component.literal("Voltar"), b -> { shopChoice = 0; rebuildWidgets(); }).bounds(left + 150, top + 205, 120, 22).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("Reset de Kikan — 100 XP Física"), b -> { chat("kencraft loja pontos rinka"); onClose(); }).bounds(left + 55, top + 145, 310, 28).build());
            addRenderableWidget(Button.builder(Component.literal("Voltar"), b -> { shopChoice = 0; rebuildWidgets(); }).bounds(left + 150, top + 205, 120, 22).build());
        }
    }

    private void command(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) mc.player.connection.sendCommand(command);
    }

    private void chat(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) mc.player.connection.sendChat(message);
    }

    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { graphics.fill(0, 0, width, height, 0xFF101318); }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left, top, left + W, top + H, 0xFF1B222B);
        graphics.fill(left + 12, top + 12, left + W - 12, top + 40, 0xFF252D36);
        graphics.drawCenteredString(font, Component.literal("KENCRAFT"), width / 2, top + 20, 0xFFFFFFFF);
        PlayerData d = data();
        graphics.drawString(font, Component.literal("Raça: " + raceName(d.race())), left + 20, top + 84, 0xFF7AD7FF);
        if (tab == 0) renderStatus(graphics, d); else if (tab == 1) renderJio(graphics, d); else if (tab == 2) renderKikan(graphics, d); else renderPointShop(graphics, d);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderStatus(GuiGraphics g, PlayerData d) {
        String[] names = d.race() == Race.RINKA ? new String[]{"Força","Defesa/Resistência","Inteligência","Velocidade","Genética"} : new String[]{"Força","Vida","Percepção","Desenvolvimento espiritual","Velocidade"};
        String[] attrs = d.race() == Race.RINKA ? new String[]{"strength","defense","intelligence","speed","genetics"} : new String[]{"strength","life","perception","spiritual","speed"};
        int y = top + 112;
        for (int i = 0; i < names.length; i++) { int v = valueOf(d, attrs[i]); g.drawString(font, Component.literal(names[i] + ": " + v + "/" + PlayerData.MAX_STATUS + " (" + PlayerData.spentPoints(v) + " points)"), left + 24, y, 0xFFFFFFFF); y += 28; }
        g.drawString(font, Component.literal("XP Mental: " + d.mentalXp()), left + 24, top + 252, 0xFFB8C5D1);
        g.drawString(font, Component.literal("XP Física: " + d.physicalXp()), left + 24, top + 268, 0xFFB8C5D1);
    }

    private void renderJio(GuiGraphics g, PlayerData d) {
        boolean canJio = d.race() == Race.HUMAN || d.race() == Race.HYBRID || d.race() == Race.JASHIN;
        if (!canJio) { g.drawString(font, Component.literal("Esta raça não possui acesso ao Jio."), left + 24, top + 112, 0xFFFFFFFF); return; }
        String technique = PlayerData.normalizeTechnique(d.jioTechnique());
        int maxJio = d.race() == Race.HUMAN ? d.calculatedHumanMaxJio() : d.maxJio();
        g.drawString(font, Component.literal("Jio: " + d.jio() + "/" + maxJio), left + 24, top + 112, 0xFF7AD7FF);
        g.drawString(font, Component.literal("Técnica: " + prettyJio(technique)), left + 24, top + 132, 0xFFFFFFFF);
        g.drawString(font, Component.literal("Habilidade: " + (d.jioAbilitySlot() + 1) + "/3"), left + 24, top + 152, 0xFFFFFFFF);
        g.drawString(font, Component.literal("F usa a habilidade • G troca a habilidade"), left + 24, top + 176, 0xFFB8C5D1);
        if (!"NONE".equals(technique)) {
            SpiritualState state = Minecraft.getInstance().player.getData(ModAttachments.SPIRITUAL_STATE);
            g.drawString(font, Component.literal("Estado espiritual: " + (state.isSujo() ? "SUJO" : "NORMAL")), left + 24, top + 196, 0xFFFFAA66);
        } else g.drawString(font, Component.literal("Gire uma técnica uma única vez."), left + 24, top + 196, 0xFFFFAA66);
    }

    private void renderKikan(GuiGraphics g, PlayerData d) {
        if (d.race() != Race.RINKA && d.race() != Race.HYBRID) { g.drawString(font, Component.literal("Kikan não está disponível para esta raça."), left + 24, top + 112, 0xFFFFFFFF); return; }
        g.drawString(font, Component.literal("Classe: " + d.rinkaClass()), left + 24, top + 112, 0xFFFF7777);
        g.drawString(font, Component.literal("Jinsuikaku: " + d.jinsuikakuConsumed()), left + 24, top + 132, 0xFFB8C5D1);
        g.drawString(font, Component.literal("Kikan: " + prettyKikan(d.kikanType())), left + 24, top + 152, 0xFF7AD7FF);
        if (!d.canUseKikan()) g.drawString(font, Component.literal("Você precisa ser Classe C ou superior."), left + 24, top + 180, 0xFFFFAA66);
    }

    private void renderPointShop(GuiGraphics g, PlayerData d) {
        if (shopChoice == 0) { g.drawCenteredString(font, Component.literal("LOJA DE PONTOS"), width / 2, top + 112, 0xFFFFFFFF); g.drawCenteredString(font, Component.literal("Escolha como deseja acessar a loja:"), width / 2, top + 128, 0xFFB8C5D1); }
        else if (shopChoice == 1) { g.drawCenteredString(font, Component.literal("LOJA HUMANO"), width / 2, top + 112, 0xFFFFFFFF); g.drawCenteredString(font, Component.literal("Reset de Técnica Jio: 100 XP Mental"), width / 2, top + 128, 0xFF7AD7FF); }
        else { g.drawCenteredString(font, Component.literal("LOJA RINKA"), width / 2, top + 112, 0xFFFFFFFF); g.drawCenteredString(font, Component.literal("Reset de Kikan: 100 XP Física"), width / 2, top + 128, 0xFF7AD7FF); }
    }

    private int valueOf(PlayerData d, String attr) { return switch (attr) { case "strength" -> d.strength(); case "defense" -> d.defense(); case "intelligence" -> d.intelligence(); case "speed" -> d.speed(); case "genetics" -> d.genetics(); case "perception" -> d.perception(); case "spiritual" -> d.spiritualDevelopment(); case "life" -> d.life(); default -> 1; }; }
    private PlayerData data() { return Minecraft.getInstance().player == null ? PlayerData.DEFAULT : Minecraft.getInstance().player.getData(ModAttachments.PLAYER_DATA); }
    private String raceName(Race r) { return switch (r) { case RINKA -> "Rinka"; case HUMAN -> "Humano"; case HYBRID -> "Híbrido"; case JASHIN -> "Jashin"; default -> "Sem raça"; }; }
    private String prettyKikan(String s) { return switch (s) { case "CROCODILE_TAIL" -> "Cauda de crocodilo"; case "TENTACLE" -> "Tentáculo"; case "SCORPION_TAIL" -> "Cauda de escorpião"; case "BUTTERFLY_TENTACLE" -> "Borboleta"; case "LIZARD_CROCODILE_TAIL" -> "Lagarto"; default -> "Nenhuma"; }; }
    private String prettyJio(String s) { return switch (s) { case "Seishin dan" -> "Seishin dan"; case "Hakai satsu Totetsu: Seimei kui" -> "Hakai satsu Totetsu: Seimei kui"; case "Kata kyoka" -> "Kata kyoka"; case "The Paradise" -> "The Paradise"; case "The King of Lies" -> "The King of Lies"; default -> "Nenhuma"; }; }
}
