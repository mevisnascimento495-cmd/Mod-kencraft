package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftStatEffects {
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "status_speed");
    private static final ResourceLocation LIFE_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "status_life");
    private static final ResourceLocation REACH_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "status_perception");
    private KenCraftStatEffects() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;
        if (player.tickCount % 5 != 0) return;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        boolean kikakogouActive = player.getData(ModAttachments.KIKAKOGOU_STATE).active();
        var speed = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        speed.addOrUpdateTransientModifier(new AttributeModifier(SPEED_ID, kikakogouActive ? 0.0D : (data.speed() - 1) * 0.035D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        var health = player.getAttributes().getInstance(Attributes.MAX_HEALTH);
        health.addOrUpdateTransientModifier(new AttributeModifier(LIFE_ID, (data.life() - 1) * 1.0D, AttributeModifier.Operation.ADD_VALUE));
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
        if (player.getAttributes().hasAttribute(Attributes.ENTITY_INTERACTION_RANGE)) {
            var reach = player.getAttributes().getInstance(Attributes.ENTITY_INTERACTION_RANGE);
            reach.addOrUpdateTransientModifier(new AttributeModifier(REACH_ID, (data.perception() - 1) * 0.12D, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer victim) {
            PlayerData data = victim.getData(ModAttachments.PLAYER_DATA);
            float reduction = Math.min(0.60F, Math.max(0F, (data.defense() - 1) * 0.025F));
            event.getContainer().setNewDamage(event.getNewDamage() * (1.0F - reduction));
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            PlayerData data = attacker.getData(ModAttachments.PLAYER_DATA);
            float bonus = Math.max(0F, (data.strength() - 1) * 0.40F);
            event.getContainer().setNewDamage(event.getNewDamage() + bonus);
        }
    }
}
