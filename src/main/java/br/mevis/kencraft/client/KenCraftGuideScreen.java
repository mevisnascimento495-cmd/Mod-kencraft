package br.mevis.kencraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class KenCraftGuideScreen extends Screen {
    private int panelWidth, panelHeight, left, top;
    private int section = 0, subsection = 0;

    private static final String[] TITLES = {
            "INÍCIO", "RAÇAS", "PROGRESSÃO", "SISTEMAS", "ESTRUTURAS", "ENTIDADES", "CONTROLES"
    };
    private static final String[][] SUBTITLES = {
            {"Visão geral", "Primeiros passos"},
            {"Rinka", "Humano", "Híbrido e Jashin"},
            {"Rinka progressão", "ARF progressão", "Atributos"},
            {"Jio", "Kikan", "Talentos e loja"},
            {"Base da ARF", "Hospital", "Minamori"},
            {"Rinkas", "ARF", "NPCs e personagens"},
            {"Teclas", "Comandos", "Dicas de uso"}
    };
    private static final String[][][] CONTENT = {
        {
            {"O Manual do KenCraft é uma enciclopédia dentro do jogo.",
             "Escolha uma categoria na primeira fileira e depois uma subaba.",
             "As informações descrevem os sistemas identificados no projeto.",
             "Conteúdos futuros não devem ser considerados recursos disponíveis."},
            {"Ao entrar sem raça, siga as instruções de seleção no chat.",
             "Digite Rinka ou Humano quando o jogo solicitar.",
             "Depois, pressione R para abrir o menu principal.",
             "O manual possui interface própria e é separado do menu de Status."}
        },
        {
            {"O perfil registra classe Rinka, consumo de Jinsuikaku e tipo de Kikan.",
             "Há um registro separado para o consumo de Jinsuikaku Rank C.",
             "Esses dados permitem acompanhar etapas da evolução entre sessões.",
             "Consulte as subabas Progressão e Kikan para os sistemas relacionados."},
            {"Humanos estão ligados aos sistemas Jio e ARF.",
             "O perfil guarda valores de Jio, técnica escolhida e espaço de habilidade.",
             "A progressão ARF também registra classe e eliminações em missões.",
             "As opções disponíveis dependem das regras ativas no jogo."},
            {"O modelo de dados reconhece Rinka, Humano, Híbrido e Jashin.",
             "Habilidades e caminhos podem variar conforme a raça escolhida.",
             "Nem todas as raças compartilham o mesmo conjunto de técnicas.",
             "Confira as opções mostradas pelo menu do KenCraft."}
        },
        {
            {"RINKA PROGRESSÃO",
             "O perfil registra classe Rinka e consumo de Jinsuikaku, incluindo Rank C.",
             "O tipo de Kikan também é salvo no perfil do personagem.",
             "Use o menu do jogo para conferir as opções disponíveis.",
             "Leia as descrições dos itens antes de usá-los em sua evolução."},
            {"ARF PROGRESSÃO",
             "A progressão ARF registra a classe do jogador e eliminações em missões.",
             "A organização inclui investigadores e o General Akio Ginshō.",
             "Investigadores podem detectar Rinkas e compartilhar alvos com agentes próximos.",
             "Confira objetivos e interações no jogo para identificar tarefas ativas."},
            {"O perfil mantém Força, Defesa, Inteligência, Velocidade, Genética,",
             "Percepção, Desenvolvimento Espiritual e Vida.",
             "Também há dados de experiência mental e física.",
             "Os efeitos exatos dependem das mecânicas aplicadas pelo mod em combate e evolução."}
        },
        {
            {"Jio é um sistema importante ligado aos humanos.",
             "O perfil guarda técnica escolhida, valores de Jio e espaço de habilidade.",
             "O menu principal permite consultar dados do personagem.",
             "Algumas funções Jio usam F e G; veja a subaba Controles."},
            {"Kikan está ligado aos caminhos de habilidade dos Rinkas.",
             "O perfil guarda o tipo de Kikan e o menu reconhece variações como",
             "cauda de crocodilo, tentáculo e cauda de escorpião, entre outras.",
             "Os atalhos documentados para Kikan são Z e N."},
            {"O mod possui uso de talento e Loja de Pontos.",
             "As opções podem depender da raça e dos dados do personagem.",
             "Confira as informações no menu antes de gastar pontos.",
             "A configuração documentada associa C ao uso de talento."}
        },
        {
            {"A base da ARF possui fachada reforçada, área de comando e elementos de quartel.",
             "Ela está associada à presença de investigadores da organização.",
             "Explore os acessos e o interior com cuidado.",
             "A estrutura pode ser ajustada em futuras atualizações."},
            {"O Hospital Abandonado possui áreas internas e setor de isolamento.",
             "A entidade Rinka Faminta está associada a esse local.",
             "Procure entradas e salas laterais ao explorar.",
             "Inimigos e recompensas podem variar conforme a geração."},
            {"Minamori é um estabelecimento com áreas internas e externas,",
             "balcão, mesas e elementos decorativos.",
             "Shin Homare e Kaori Homare estão associados ao local.",
             "Use o comando de localização para encontrá-lo no mundo."}
        },
        {
            {"O projeto inclui Rinka, Rinka Rank C e Rinka Faminta, além de variantes.",
             "O comportamento e o perigo variam de uma entidade para outra.",
             "Observe o ambiente e prepare-se antes de entrar em áreas hostis."},
            {"A ARF inclui investigadores e o General Akio Ginshō.",
             "Investigadores podem detectar Rinkas e compartilhar alvos com agentes próximos.",
             "Um confronto pode atrair outros membros da organização."},
            {"Personagens associados ao mod incluem Akio Ginshō, Onoki,",
             "Shin Homare e Kaori Homare.",
             "Alguns NPCs estão ligados a estruturas, sistemas ou eventos.",
             "Interaja com eles quando houver opções disponíveis na versão instalada."}
        },
        {
            {"R — menu principal do KenCraft.",
             "Z — habilidade Kikan Z.",
             "N — habilidade Kikan C.",
             "C — uso de talento.",
             "F / G — funções Jio.",
             "V — estado espiritual; B — treinamento espiritual."},
            {"/kencraft locate minamori",
             "/kencraft locate hospital",
             "/kencraft locate arf",
             "O projeto também possui comando para gerar entidades.",
             "Consulte a ajuda do jogo para detalhes dos comandos disponíveis."},
            {"Se um atalho não funcionar, confira se a ação está disponível para sua raça.",
             "Verifique também se outra função está usando a mesma tecla.",
             "Use os comandos de localização para procurar estruturas.",
             "O manual será ampliado conforme os sistemas forem revisados."}
        }
    };

    public KenCraftGuideScreen() {
        super(Component.literal("Manual do KenCraft"));
    }

    @Override
    protected void init() {
        super.init();
        panelWidth = Math.min(520, width - 12);
        panelHeight = Math.min(360, height - 12);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        int gap = 3;
        int usable = panelWidth - 28;
        int buttonWidth = (usable - gap * 3) / 4;
        for (int i = 0; i < TITLES.length; i++) {
            final int target = i;
            int row = i < 4 ? 0 : 1;
            int col = i < 4 ? i : i - 4;
            int count = row == 0 ? 4 : 3;
            int bw = row == 0 ? buttonWidth : Math.min(buttonWidth + 8, (usable - gap * 2) / 3);
            int total = count * bw + (count - 1) * gap;
            int x = left + (panelWidth - total) / 2 + col * (bw + gap);
            addRenderableWidget(Button.builder(Component.literal(TITLES[i]), b -> {
                section = target;
                subsection = 0;
                rebuildWidgets();
            }).bounds(x, top + 42 + row * 23, bw, 20).build());
        }

        String[] subs = SUBTITLES[section];
        int subGap = 4;
        int subWidth = Math.min(150, (usable - (subs.length - 1) * subGap) / subs.length);
        int total = subs.length * subWidth + (subs.length - 1) * subGap;
        int startX = left + (panelWidth - total) / 2;
        for (int i = 0; i < subs.length; i++) {
            final int target = i;
            addRenderableWidget(Button.builder(Component.literal(subs[i]), b -> {
                subsection = target;
                rebuildWidgets();
            }).bounds(startX + i * (subWidth + subGap), top + 91, subWidth, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                .bounds(left + (panelWidth - 100) / 2, top + panelHeight - 26, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF0B0F14);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFF18212B);
        graphics.fill(left + 8, top + 8, left + panelWidth - 8, top + 34, 0xFF25313C);
        graphics.fill(left + 12, top + 117, left + panelWidth - 12, top + panelHeight - 34, 0xFF11171D);
        graphics.drawCenteredString(font, Component.literal("MANUAL DO KENCRAFT"),
                width / 2, top + 17, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal(TITLES[section] + " / " + SUBTITLES[section][subsection]),
                width / 2, top + 124, 0xFF7AD7FF);

        int y = top + 143;
        int maxWidth = panelWidth - 44;
        for (String paragraph : CONTENT[section][subsection]) {
            List<FormattedCharSequence> lines = font.split(Component.literal(paragraph), maxWidth);
            for (FormattedCharSequence line : lines) {
                if (y > top + panelHeight - 40) break;
                graphics.drawString(font, line, left + 22, y, 0xFFE6EDF3);
                y += 12;
            }
            y += 4;
            if (y > top + panelHeight - 40) break;
        }
        graphics.drawString(font, Component.literal("Aba " + (section + 1) + "/" + TITLES.length
                + " • Subaba " + (subsection + 1) + "/" + SUBTITLES[section].length),
                left + 14, top + panelHeight - 29, 0xFF91A4B5);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
