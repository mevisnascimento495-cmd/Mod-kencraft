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

public class JinsuikakuItem extends Item {
    public JinsuikakuItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getData(ModAttachments.PLAYER_DATA).race().equals(Race.RINKA)) {
            player.sendSystemMessage(Component.literal("A Jinsuikaku só pode ser consumida por um Rinka."));
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
            if (data.race() == Race.RINKA) {
                player.setData(ModAttachments.PLAYER_DATA, data.withRinkaClass("E"));
                player.sendSystemMessage(Component.literal("Você ganhou um aumento na sua classe, aumentando seu perigo, classe E"));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
