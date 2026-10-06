package br.mevis.kencraft.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.List;

public final class KenCraftGuideBookItem extends WrittenBookItem {
    public KenCraftGuideBookItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createGuideBook() {
        ItemStack stack = new ItemStack(KenCraftItems.KENCRAFT_GUIDE.get());
        List<Filterable<Component>> pages = List.of(
            page("§lKENCRAFT\n\n§0Bem-vindo ao mundo de KenCraft!\n\nEste livro é o guia oficial do mod. Aqui você encontra os principais sistemas, controles, entidades, estruturas, itens e formas de explorar o conteúdo."),
            page("§lCOMEÇANDO\n\n§0Ao entrar pela primeira vez, escolha sua raça pelo sistema de seleção. Depois, pressione §lR§r para abrir o menu principal do KenCraft.\n\nO menu reúne informações e sistemas disponíveis para o jogador."),
            page("§lRAÇAS E PROGRESSÃO\n\n§0A escolha inicial define parte da sua experiência. Humanos possuem caminhos ligados à ARF e ao sistema Jio. Rinkas possuem sistemas próprios de evolução e progressão.\n\nExplore e converse com NPCs para descobrir novos caminhos."),
            page("§lCONTROLES\n\n§0§lR§r — Menu do KenCraft\n§lZ§r — Habilidade Kikan Z\n§lN§r — Habilidade Kikan C\n§lC§r — Uso de talento\n§lF / G§r — Funções Jio\n§lV§r — Sujo espiritual\n§lB§r — Treinamento espiritual\n\nOs controles podem ser alterados nas configurações."),
            page("§lJIO E HABILIDADES\n\n§0O sistema Jio faz parte da progressão de personagens humanos. Técnicas e habilidades são administradas pelos sistemas do mod e podem abrir novas possibilidades durante a jornada.\n\nUse o menu para acompanhar o que estiver disponível para seu personagem."),
            page("§lKIKAN E TALENTOS\n\n§0O KenCraft possui habilidades e talentos ativados por teclas próprias. Kikan e talentos possuem atalhos separados para evitar conflitos.\n\nNovas habilidades podem ser adicionadas ao longo do desenvolvimento do mod."),
            page("§lENTIDADES\n\n§0Entre as entidades atuais estão Rinka, Rinka Rank C, Rinka Faminta, Rishin, Aodai, Investigador da ARF, General da ARF, Akio Ginshō, Onoki, Shin Homare, Kaori Homare, Espírito Interior e o NPC de loja de artefatos.\n\nCada entidade possui comportamento e função próprios."),
            page("§lARF E IA\n\n§0A ARF possui NPCs e sistemas de IA próprios. Investigadores podem detectar Rinkas e compartilhar alvos com outros membros próximos da ARF.\n\nA organização também possui estruturas, equipamentos e uma progressão própria."),
            page("§lESTRUTURAS\n\n§0O mundo pode conter estruturas especiais, incluindo a base da ARF, hospitais abandonados e estruturas Minamori.\n\nElas podem conter NPCs, recursos e recompensas. Explore o mundo para encontrá-las ou use os recursos de localização quando disponíveis."),
            page("§lRECOMPENSAS E ITENS\n\n§0O KenCraft possui alimentos e recursos especiais, uniformes da ARF, artefatos, fragmentos e outros itens ligados à progressão.\n\nO loot das estruturas varia conforme o tipo de estrutura e pode incluir recompensas especiais."),
            page("§lCOMANDOS\n\n§0Comandos do mod começam com §l/kencraft§r.\n\n§l/kencraft locate minamori\n/kencraft locate hospital\n/kencraft locate arf\n\nTambém existe um comando de spawn de entidades para quem possui permissão adequada."),
            page("§lDICAS DE EXPLORAÇÃO\n\n§0Converse com NPCs, observe as estruturas, experimente os sistemas do menu e acompanhe sua progressão.\n\nAlguns conteúdos estão ligados à história e podem ser descobertos conforme você explora o mundo."),
            page("§lATUALIZAÇÕES\n\n§0Este manual faz parte do próprio KenCraft. Conforme novos sistemas, entidades, estruturas e mecânicas forem adicionados ao mod, o conteúdo do guia poderá ser expandido.\n\n§lBoa jornada!\n\n§0— Equipe KenCraft")
        );
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT,
            new WrittenBookContent(
                Filterable.passThrough("Manual do KenCraft"),
                "KenCraft",
                0,
                pages,
                true
            )
        );
        return stack;
    }

    private static Filterable<Component> page(String text) {
        return Filterable.passThrough(Component.literal(text));
    }
}
