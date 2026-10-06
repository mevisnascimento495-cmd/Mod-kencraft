package br.mevis.kencraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class KenCraftGuideScreen extends Screen {
    private static final int W = 520;
    private static final int H = 360;
    private int left;
    private int top;
    private int section = 0;

    private static final String[] TITLES = {
            "INÍCIO", "RAÇAS", "SISTEMAS", "ESTRUTURAS", "ENTIDADES", "CONTROLES"
    };

    private static final String[][] CONTENT = {
            {
                    "Bem-vindo ao Manual do KenCraft.",
                    "Este é o painel de referência do mod. Ele foi separado",
                    "do menu de Status para que o manual tenha uma interface própria.",
                    "Use as categorias para consultar os principais conteúdos do jogo."
            },
            {
                    "RAÇAS E PROGRESSÃO",
                    "Humano — acesso à ARF e ao sistema Jio.",
                    "Rinka — evolução própria, Kikan e progressão física.",
                    "Híbrido e Jashin possuem combinações e caminhos especiais.",
                    "A progressão altera os sistemas disponíveis para cada personagem."
            },
            {
                    "SISTEMAS",
                    "Status — evolução de atributos e XP.",
                    "Jio — técnica, habilidade e estado espiritual.",
                    "Kikan — capacidades ligadas aos Rinkas.",
                    "Loja de Pontos — recursos especiais de progressão.",
                    "Menu principal — aberto pela tecla R."
            },
            {
                    "ESTRUTURAS",
                    "Base da ARF — complexo fortificado de operações.",
                    "Hospital Abandonado — instalação médica deteriorada e perigosa.",
                    "Minamori — estabelecimento reconstruído com áreas internas e externas.",
                    "As estruturas podem conter NPCs, comércio, loot e encontros."
            },
            {
                    "ENTIDADES",
                    "ARF: Investigadores e General Akio Ginshō.",
                    "Minamori: Shin Homare e Kaori Homare.",
                    "Hospital: Rinka Faminta.",
                    "Outras entidades do mod aparecem conforme a progressão e exploração."
            },
            {
                    "CONTROLES",
                    "R — Menu principal do KenCraft.",
                    "Z — Habilidade Kikan Z.",
                    "N — Habilidade Kikan C.",
                    "C — Uso de talento.",
                    "F / G — Funções Jio.",
                    "V — Estado espiritual.",
                    "B — Treinamento espiritual."
            }
    };

    public KenCraftGuideScreen() {
        super(Component.literal("Manual do KenCraft"));
    }

    @Override
    protected void init() {
        super.init();
        left = (width - W) / 2;
        top = (height - H) / 2;

        int startX = left + 18;
        for (int i = 0; i < TITLES.length; i++) {
            final int target = i;
            int x = startX + i * 81;
            int buttonWidth = i == TITLES.length - 1 ? 82 : 79;
            addRenderableWidget(Button.builder(Component.literal(TITLES[i]), b -> {
                section = target;
                rebuildWidgets();
            }).bounds(x, top + 48, buttonWidth, 22).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                .bounds(left + 200, top + H - 34, 120, 22).build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF0B0F14);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left, top, left + W, top + H, 0xFF18212B);
        graphics.fill(left + 10, top + 10, left + W - 10, top + 39, 0xFF25313C);
        graphics.fill(left + 18, top + 82, left + W - 18, top + H - 48, 0xFF11171D);
        graphics.fill(left + 28, top + 92, left + W - 28, top + 124, 0xFF1D2832);

        graphics.drawCenteredString(font, Component.literal("MANUAL DO KENCRAFT"), width / 2, top + 20, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal(TITLES[section]), width / 2, top + 101, 0xFF7AD7FF);

        int y = top + 137;
        for (String line : CONTENT[section]) {
            graphics.drawString(font, Component.literal(line), left + 32, y, 0xFFE6EDF3);
            y += 24;
        }

        graphics.drawString(font, Component.literal("Seção " + (section + 1) + " / " + TITLES.length),
                left + 22, top + H - 44, 0xFF91A4B5);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
