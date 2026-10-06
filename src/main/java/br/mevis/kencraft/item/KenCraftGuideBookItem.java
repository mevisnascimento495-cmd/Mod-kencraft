package br.mevis.kencraft.item;

import br.mevis.kencraft.client.KenCraftGuideClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

import java.util.List;

public final class KenCraftGuideBookItem extends WrittenBookItem {
    public KenCraftGuideBookItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, guideContent());
        }

        if (level.isClientSide) {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> KenCraftGuideClient::open);
            return InteractionResultHolder.sidedSuccess(stack);
        }

        return InteractionResultHolder.consume(stack);
    }

    public static ItemStack createGuideBook() {
        ItemStack stack = new ItemStack(KenCraftItems.KENCRAFT_GUIDE.get());
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, guideContent());
        return stack;
    }

    private static WrittenBookContent guideContent() {
        List<Filterable<Component>> pages = List.of(
            page("§lKENCRAFT\n\n§0Bem-vindo ao mundo de KenCraft!\n\nEste livro é o guia oficial do mod. Aqui você encontra os principais sistemas, controles, entidades, estruturas, itens e funções do jogo."),
            page("§lCOMEÇANDO\n\n§0Ao entrar pela primeira vez, escolha sua raça pelo sistema de seleção. Depois, pressione §lR§r para abrir o menu principal do KenCraft.\n\nO menu reúne todas as informações principais do mod e ajuda no controle de status, jio, kikan e progressão."),
            page("§lRAÇAS E PROGRESSÃO\n\n§0A escolha inicial define parte da sua experiência. Humanos possuem caminhos ligados à ARF e ao sistema Jio. Rinkas possuem sistemas próprios de evolução e Kikan."),
            page("§lCONTROLES\n\n§0§lR§r — Menu do KenCraft\n§lZ§r — Habilidade Kikan Z\n§lN§r — Habilidade Kikan C\n§lC§r — Uso de talento\n§lF / G§r — Funções Jio\n§lV§r — Estado espiritual\n§lB§r — Treinamento espiritual"),
            page("§lJIO E HABILIDADES\n\n§0O sistema Jio faz parte da progressão de personagens humanos. Técnicas e habilidades são administradas pelos sistemas do mod e podem abrir novas possibilidades de combate."),
            page("§lKIKAN E TALENTOS\n\n§0O KenCraft possui habilidades e talentos ativados por teclas próprias. Kikan e talentos possuem atalhos separados para evitar conflitos."),
            page("§lENTIDADES\n\n§0Entre as entidades atuais estão Rinka, Rinka Rank C, Rinka Faminta, Rishin, Aodai, Investigador da ARF, General da ARF, Akio Ginshō, Onoki, Shin Homare, Kaori Homare e outros NPCs do mod."),
            page("§lARF E IA\n\n§0A ARF possui NPCs e sistemas de IA próprios. Investigadores podem detectar Rinkas e compartilhar alvos com outros membros próximos da ARF."),
            page("§lESTRUTURAS\n\n§0O mundo pode conter estruturas especiais, incluindo a base da ARF, hospitais abandonados e estruturas Minamori. Elas podem conter NPCs, recursos e recompensas únicas."),
            page("§lRECOMPENSAS E ITENS\n\n§0O KenCraft possui alimentos e recursos especiais, uniformes da ARF, artefatos, fragmentos e outros itens ligados à progressão. O loot das estruturas pode ser muito valioso."),
            page("§lCOMANDOS\n\n§0Comandos do mod começam com §l/kencraft§r.\n\n§l/kencraft locate minamori\n/kencraft locate hospital\n/kencraft locate arf\n\nTambém existe um comando de spawn para entidades do mod."),
            page("§lDICAS DE EXPLORAÇÃO\n\n§0Converse com NPCs, observe as estruturas, experimente os sistemas do menu e acompanhe sua progressão. Alguns conteúdos são ligados à história e ao universo do mod."),
            page("§lATUALIZAÇÕES\n\n§0Este manual faz parte do próprio KenCraft. Conforme novos sistemas, entidades, estruturas e mecânicas forem adicionados ao mod, o conteúdo do guia pode ser expandido."),
            page("§lFIM\n\n§0Seja bem-vindo ao KenCraft. Explore, evolua e descubra os segredos do mundo do mod." )
        );

        return new WrittenBookContent(
            Filterable.passThrough("Manual do KenCraft"),
            "KenCraft",
            0,
            pages,
            true
        );
    }

    private static Filterable<Component> page(String text) {
        return Filterable.passThrough(Component.literal(text));
    }
}
