package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.TalentData;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Comparator;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class TalentSystem {
    private static final ResourceLocation STRENGTH_DAMAGE = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "talent_strength_damage");
    private static final ResourceLocation STRENGTH_ARMOR = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "talent_strength_armor");
    private static final ResourceLocation STRENGTH_HEALTH = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "talent_strength_health");
    private static final ResourceLocation STRENGTH_SPEED = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "talent_strength_speed");

    private TalentSystem() {}

    public static void use(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN) return;

        TalentData talent = player.getData(ModAttachments.TALENT_DATA);
        if (!talent.hasTalent()) {
            player.sendSystemMessage(Component.literal("§cVocê ainda não possui um talento."));
            return;
        }

        switch (talent.talent()) {
            case "FORCA" -> activateStrength(player);
            case "VELOCIDADE" -> activateSpeed(player);
            case "DEFESA" -> activateDefense(player);
            case "REGENERACAO" -> activateRegeneration(player);
            default -> player.sendSystemMessage(Component.literal("§cTalento inválido."));
        }
    }

    private static void activateStrength(ServerPlayer player) {
        TalentData data = player.getData(ModAttachments.TALENT_DATA);
        int tier = Math.min(5, Math.max(1, data.strengthTier() + 1));
        int multiplier = tier + 1;
        int durationSeconds = tier == 5 ? 47 : tier * 3;

        removeStrengthModifiers(player);
        addMultiplier(player, Attributes.ATTACK_DAMAGE, STRENGTH_DAMAGE, multiplier);
        addMultiplier(player, Attributes.ARMOR, STRENGTH_ARMOR, multiplier);
        addMultiplier(player, Attributes.MAX_HEALTH, STRENGTH_HEALTH, multiplier);
        addMultiplier(player, Attributes.MOVEMENT_SPEED, STRENGTH_SPEED, multiplier);

        player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() * multiplier));
        player.setData(ModAttachments.TALENT_DATA, data.withStrengthTier(tier).withActiveTicks(durationSeconds * 20));
        player.sendSystemMessage(Component.literal("§eTalento de Força — Faixa " + tier + " (x" + multiplier + ") por " + durationSeconds + "s."));
    }

    private static void activateSpeed(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2, false, true, true));
        player.setData(ModAttachments.TALENT_DATA, player.getData(ModAttachments.TALENT_DATA).withActiveTicks(100));
        player.sendSystemMessage(Component.literal("§bPasso Relâmpago ativado."));
    }

    private static void activateDefense(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4, false, true, true));
        player.setData(ModAttachments.TALENT_DATA, player.getData(ModAttachments.TALENT_DATA).withActiveTicks(200));
        player.sendSystemMessage(Component.literal("§7Camada Defensiva ativada."));
    }

    private static void activateRegeneration(ServerPlayer player) {
        LivingEntity target = findNearestTarget(player, 6.0D);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cNenhum alvo próximo para o Dreno Vital."));
            return;
        }
        player.setData(ModAttachments.TALENT_DATA, player.getData(ModAttachments.TALENT_DATA).withActiveTicks(100));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 255, false, true, true));
        player.sendSystemMessage(Component.literal("§dDreno Vital ativado."));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN) return;

        TalentData talent = player.getData(ModAttachments.TALENT_DATA);
        if (talent.activeTicks() <= 0) return;

        int remaining = talent.activeTicks() - 1;
        player.setData(ModAttachments.TALENT_DATA, talent.withActiveTicks(remaining));

        if ("FORCA".equals(talent.talent())) {
            if (remaining == 0) removeStrengthModifiers(player);
            return;
        }

        if ("REGENERACAO".equals(talent.talent()) && player.tickCount % 20 == 0) {
            LivingEntity target = findNearestTarget(player, 6.0D);
            if (target != null && target.isAlive()) {
                float amount = Math.min(4.0F, target.getHealth());
                if (target.hurt(player.damageSources().playerAttack(player), amount)) {
                    player.heal(amount);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Math.max(1, remaining), 255, false, true, true));
                }
            }
        }

        if ("VELOCIDADE".equals(talent.talent()) && remaining == 0) {
            LivingEntity target = findNearestTarget(player, 4.0D);
            if (target != null) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 10, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true, true));
            }
        }
    }

    private static LivingEntity findNearestTarget(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                entity -> entity != player && entity.isAlive())
                .stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
    }

    private static void addMultiplier(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, int multiplier) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.addOrUpdateTransientModifier(new AttributeModifier(id, multiplier - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void removeStrengthModifiers(ServerPlayer player) {
        removeModifier(player, Attributes.ATTACK_DAMAGE, STRENGTH_DAMAGE);
        removeModifier(player, Attributes.ARMOR, STRENGTH_ARMOR);
        removeModifier(player, Attributes.MAX_HEALTH, STRENGTH_HEALTH);
        removeModifier(player, Attributes.MOVEMENT_SPEED, STRENGTH_SPEED);
    }

    private static void removeModifier(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id) {
        var instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }
}
