package br.mevis.kencraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public final class ArtifactItem extends Item {
    public enum Type { SPIRIT_NECKLACE, VITALITY_RING, PERCEPTION_AMULET, PARADISE_FRAGMENT }
    private final Type type;

    public ArtifactItem(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            switch (type) {
                case SPIRIT_NECKLACE -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0));
                case VITALITY_RING -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 1));
                case PERCEPTION_AMULET -> player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                case PARADISE_FRAGMENT -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
                }
            }
            player.getCooldowns().addCooldown(this, 200);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        switch (type) {
            case SPIRIT_NECKLACE -> tooltip.add(Component.literal("Regeneração breve").withStyle(ChatFormatting.AQUA));
            case VITALITY_RING -> tooltip.add(Component.literal("Concede absorção").withStyle(ChatFormatting.GOLD));
            case PERCEPTION_AMULET -> tooltip.add(Component.literal("Visão noturna").withStyle(ChatFormatting.LIGHT_PURPLE));
            case PARADISE_FRAGMENT -> tooltip.add(Component.literal("Velocidade e resistência breves").withStyle(ChatFormatting.WHITE));
        }
        tooltip.add(Component.literal("Artefato da loja").withStyle(ChatFormatting.GRAY));
    }
}