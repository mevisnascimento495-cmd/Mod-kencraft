package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ArfMissionData;
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
import net.minecraft.world.phys.Vec3;
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

    public static void showTalentOptions(ServerPlayer player) {
        if (!canRoll(player)) return;
        TalentData current = player.getData(ModAttachments.TALENT_DATA);
        int roll = player.getRandom().nextInt(100);

        if (roll < 73) {
            String[] talents = {"FORCA", "VELOCIDADE", "DEFESA", "REGENERACAO"};
            String selected = talents[player.getRandom().nextInt(talents.length)];
            player.setData(ModAttachments.TALENT_DATA, current.withTalent(selected).withStrengthTier(0).withActiveTicks(0));
            player.sendSystemMessage(Component.literal("§6══ ROLETA DE TALENTOS ══"));
            player.sendSystemMessage(Component.literal("§eResultado: §f" + displayName(selected)));
            player.sendSystemMessage(Component.literal("§aVocê recebeu o talento §f" + displayName(selected) + "§a."));
            return;
        }
        if (roll < 96) {
            player.setData(ModAttachments.TALENT_DATA, current.addClanReroll());
            player.sendSystemMessage(Component.literal("§6══ ROLETA DE TALENTOS ══"));
            player.sendSystemMessage(Component.literal("§bResultado: §fReroll extra de Clã"));
            player.sendSystemMessage(Component.literal("§aRerolls de Clã disponíveis: §f" + (current.clanRerolls() + 1)));
            return;
        }
        player.setData(ModAttachments.TALENT_DATA, current.addTechniqueReroll());
        player.sendSystemMessage(Component.literal("§6══ ROLETA DE TALENTOS ══"));
        player.sendSystemMessage(Component.literal("§dResultado: §fReroll extra de Técnica"));
        player.sendSystemMessage(Component.literal("§aRerolls de Técnica disponíveis: §f" + (current.techniqueRerolls() + 1)));
    }

    private static boolean canRoll(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
        if (data.race() != Race.HUMAN) {
            player.sendSystemMessage(Component.literal("§cOs talentos da ARF são exclusivos para investigadores humanos."));
            return false;
        }
        if (data.arfClass() == 0) {
            player.sendSystemMessage(Component.literal("§cVocê precisa pertencer à ARF para usar a roleta."));
            return false;
        }
        if (!mission.mission1Completed()) {
            player.sendSystemMessage(Component.literal("§cComplete a primeira missão da ARF para desbloquear a Roleta de Talentos."));
            return false;
        }
        return true;
    }

    public static void use(ServerPlayer player) {
        if (player.getData(ModAttachments.PLAYER_DATA).race() != Race.HUMAN) return;
        TalentData talent = player.getData(ModAttachments.TALENT_DATA);
        if (!talent.hasTalent()) {
            player.sendSystemMessage(Component.literal("§cVocê ainda não possui um talento. Fale com Akio e use a opção Talentos para girar a roleta."));
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
        int durationSeconds = 35 + ((tier - 1) * 3);
        removeStrengthModifiers(player);
        addMultiplier(player, Attributes.ATTACK_DAMAGE, STRENGTH_DAMAGE, multiplier);
        addMultiplier(player, Attributes.ARMOR, STRENGTH_ARMOR, multiplier);
        addMultiplier(player, Attributes.MAX_HEALTH, STRENGTH_HEALTH, multiplier);
        addMultiplier(player, Attributes.MOVEMENT_SPEED, STRENGTH_SPEED, multiplier);
        player.setData(ModAttachments.TALENT_DATA, data.withStrengthTier(tier).withActiveTicks(durationSeconds * 20));
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() * multiplier));
        player.sendSystemMessage(Component.literal("§eForça — Faixa " + tier + " | x" + multiplier + " | " + durationSeconds + "s"));
    }

    private static void activateSpeed(ServerPlayer player) {
        int ticks = 100;
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ticks, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ticks, 2, false, true, true));
        player.setData(ModAttachments.TALENT_DATA, player.getData(ModAttachments.TALENT_DATA).withActiveTicks(ticks));
        player.sendSystemMessage(Component.literal("§bPasso Relâmpago ativado por 5 segundos."));
    }

    private static void activateDefense(ServerPlayer player) {
        int ticks = 200;
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, 4, false, true, true));
        player.setData(ModAttachments.TALENT_DATA, player.getData(ModAttachments.TALENT_DATA).withActiveTicks(ticks));
        player.sendSystemMessage(Component.literal("§7Camada Defensiva ativada por 10 segundos."));
    }

    private static void activateRegeneration(ServerPlayer player) {
        LivingEntity target = findNearestTarget(player, 6.0D);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cNenhum alvo próximo para o Dreno Vital."));
            return;
        }
        int ticks = 100;
        player.setData(ModAttachments.TALENT_DATA, player.getData(ModAttachments.TALENT_DATA).withActiveTicks(ticks));
        target.getPersistentData().putInt("kencraft_talent_drain_owner", player.getId());
        target.getPersistentData().putInt("kencraft_talent_drain_ticks", ticks);
        player.getPersistentData().putInt("kencraft_talent_drain_target", target.getId());
        player.sendSystemMessage(Component.literal("§dDreno Vital prendeu o alvo por 5 segundos."));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getData(ModAttachments.PLAYER_DATA).race() != Race.HUMAN) return;
        TalentData talent = player.getData(ModAttachments.TALENT_DATA);
        if (talent.activeTicks() <= 0) return;

        int remaining = talent.activeTicks() - 1;
        player.setData(ModAttachments.TALENT_DATA, talent.withActiveTicks(remaining));

        if ("FORCA".equals(talent.talent())) {
            if (remaining == 0) removeStrengthModifiers(player);
            return;
        }
        if ("REGENERACAO".equals(talent.talent())) updateLifeDrain(player, remaining);
        if ("VELOCIDADE".equals(talent.talent()) && remaining == 0) {
            LivingEntity target = findNearestTarget(player, 4.0D);
            if (target != null) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 10, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true, true));
            }
        }
    }

    private static void updateLifeDrain(ServerPlayer player, int remaining) {
        int targetId = player.getPersistentData().getInt("kencraft_talent_drain_target");
        if (targetId <= 0) return;

        net.minecraft.world.entity.Entity entity = player.level().getEntity(targetId);
        if (!(entity instanceof LivingEntity)) {
            player.getPersistentData().remove("kencraft_talent_drain_target");
            return;
        }
        LivingEntity target = (LivingEntity) entity;
        if (!target.isAlive() || remaining <= 0) {
            clearLifeDrain(player, target);
            return;
        }

        target.getPersistentData().putInt("kencraft_talent_drain_ticks", remaining);
        Vec3 hold = player.position().add(player.getLookAngle().scale(1.55D)).add(0.0D, 0.45D, 0.0D);
        target.teleportTo(hold.x, hold.y, hold.z);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;

        if (player.tickCount % 20 == 0) {
            float amount = Math.min(4.0F, target.getHealth());
            if (amount > 0.0F && target.hurt(player.damageSources().playerAttack(player), amount)) player.heal(amount);
        }
        if (remaining == 1) clearLifeDrain(player, target);
    }

    private static void clearLifeDrain(ServerPlayer player, LivingEntity target) {
        if (target != null) {
            target.getPersistentData().remove("kencraft_talent_drain_owner");
            target.getPersistentData().remove("kencraft_talent_drain_ticks");
        }
        player.getPersistentData().remove("kencraft_talent_drain_target");
    }

    private static String displayName(String talent) {
        return switch (talent) {
            case "FORCA" -> "Força";
            case "VELOCIDADE" -> "Velocidade";
            case "DEFESA" -> "Defesa";
            case "REGENERACAO" -> "Regeneração";
            default -> "Nenhum";
        };
    }

    private static LivingEntity findNearestTarget(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), entity -> entity != player && entity.isAlive())
                .stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
    }

    private static void addMultiplier(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, int multiplier) {
        var instance = player.getAttribute(attribute);
        if (instance != null) instance.addOrUpdateTransientModifier(new AttributeModifier(id, multiplier - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
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