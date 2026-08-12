package br.mevis.kencraft.item;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Rank C organ dropped by elite Rinkas. Five unlock Rank B; fifteen unlock Rank A. */
public final class JinsuikakuRankCItem extends Item {
    public JinsuikakuRankCItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getData(ModAttachments.PLAYER_DATA).race() != Race.RINKA) {
            player.sendSystemMessage(Component.literal("A Jinsuikaku Rank C só pode ser consumida por um Rinka."));
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
            if (data.race() == Race.RINKA) {
                int consumed = data.jinsuikakuRankCConsumed() + 1;
                String oldClass = data.rinkaClass();
                String newClass = rankClassForConsumed(consumed, oldClass);
                player.setData(ModAttachments.PLAYER_DATA,
                        data.withJinsuikakuRankCConsumed(consumed).withRinkaClass(newClass));
                if (!newClass.equals(oldClass)) {
                    player.sendSystemMessage(Component.literal(
                            "Você ganhou um aumento na sua classe, aumentando seu perigo, classe " + newClass));
                    if ("A".equals(newClass)) {
                        player.sendSystemMessage(Component.literal(
                                "Você atingiu a Classe A. Já pode evoluir sua Kikan para uma Kikakogou."));
                    }
                } else {
                    player.sendSystemMessage(Component.literal(
                            "Jinsuikaku Rank C devorada: " + consumed + "."));
                }
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    private static String rankClassForConsumed(int consumed, String current) {
        if (consumed >= 15) return "A";
        if (consumed >= 5) return "B";
        return current == null || "NONE".equals(current) ? "C" : current;
    }
}
