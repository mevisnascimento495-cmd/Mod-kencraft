package br.mevis.kencraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class KenCraftGuideBookItem extends Item {
    public KenCraftGuideBookItem(Properties properties){super(properties.stacksTo(1));}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        ItemStack stack=player.getItemInHand(hand);
        if(!level.isClientSide){
            player.sendSystemMessage(Component.literal("§6§lKENCRAFT — GUIA DO INÍCIO"));
            player.sendSystemMessage(Component.literal("§f1. §eEscolha sua raça: §fRinka ou Humano."));
            player.sendSystemMessage(Component.literal("§f2. §eAbra o menu com R§f para descobrir seus sistemas e atributos."));
            player.sendSystemMessage(Component.literal("§f3. §eExplore o mundo§f: estruturas escondem NPCs, inimigos e recompensas."));
            player.sendSystemMessage(Component.literal("§f4. §eConverse com NPCs§f: eles iniciam missões e caminhos diferentes da lore."));
            player.sendSystemMessage(Component.literal("§f5. §eRinkas§f podem evoluir com Jinsuikaku e enfrentar desafios especiais."));
            player.sendSystemMessage(Component.literal("§f6. §eHumanos§f podem entrar na ARF, subir de classe e aprender sobre Jio."));
            player.sendSystemMessage(Component.literal("§f7. §eProcure estruturas§f e siga as pistas para descobrir a história."));
            player.sendSystemMessage(Component.literal("§7Sua escolha inicia a jornada. O mundo fará o resto."));
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
    }
}
