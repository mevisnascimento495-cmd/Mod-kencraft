package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.KikakogouState;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Locale;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KikakogouSystem {
    private static final int DURATION_TICKS = 120 * 20;
    private static final int COOLDOWN_TICKS = 150 * 20;
    private static final int HALLUCINATION_TICKS = 10 * 20;
    private static final int SCORPION_STING_CONTACT_TICKS = 10;
    private static final ResourceLocation SCALE_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "kikakogou_crocodile_scale");
    private static final ResourceLocation ARMOR_LOCK_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "kikakogou_armor_lock");
    private static final ResourceLocation CROCODILE_SLOW_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "kikakogou_crocodile_slow");
    private static final String SLAM_ARMED = "kencraft_kikakogou_slam_armed";
    private static final String LAST_Z = "kencraft_kikakogou_last_z";
    private static final String LAST_C = "kencraft_kikakogou_last_c";
    private static final String HALLUCINATION = "kencraft_kikakogou_hallucination";
    private static final String HALLUCINATION_TARGET_ID = "kencraft_kikakogou_hallucination_target";
    private static final String SCORPION_STING_TICKS = "kencraft_kikakogou_scorpion_sting_ticks";
    private static final String SCORPION_STING_TARGET_ID = "kencraft_kikakogou_scorpion_sting_target";
    private static final String ARMOR_AMOUNT = "kencraft_kikakogou_armor_amount";

    private KikakogouSystem() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("kencraft")
                .then(Commands.literal("kikakogou")
                        .then(Commands.literal("toggle").executes(context -> toggle(context.getSource())))
                        .then(Commands.literal("ability")
                                .then(Commands.argument("slot", StringArgumentType.word())
                                        .executes(context -> ability(context.getSource(), StringArgumentType.getString(context, "slot")))))));
    }

    private static int toggle(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
        if (state.active()) {
            deactivate(player, true);
            return 1;
        }
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.RINKA) {
            player.sendSystemMessage(Component.literal("A Kikakogou só pode ser usada por Rinkas."));
            return 0;
        }
        if (!"A".equalsIgnoreCase(data.rinkaClass()) && !"S".equalsIgnoreCase(data.rinkaClass())) {
            player.sendSystemMessage(Component.literal("Você precisa alcançar o Rank A para usar a Kikakogou."));
            return 0;
        }
        if (!KikakogouProgress.isUnlocked(player)) {
            player.sendSystemMessage(Component.literal("Você ainda não concluiu a missão de Aodai para desbloquear a Kikakogou."));
            return 0;
        }
        if (state.cooldownTicks() > 0) {
            player.sendSystemMessage(Component.literal("Kikakogou em recarga: " + ((state.cooldownTicks() + 19) / 20) + "s."));
            return 0;
        }
        String form = normalizeForm(data.kikanType());
        if ("NONE".equals(form)) {
            player.sendSystemMessage(Component.literal("Sua Kikan atual não possui um Kikakogou implementado."));
            return 0;
        }
        player.getPersistentData().putInt(ARMOR_AMOUNT, player.getArmorValue());
        player.setData(ModAttachments.KIKAKOGOU_STATE, new KikakogouState(form, true, DURATION_TICKS, 0));
        applyTransformationAttributes(player, form);
        player.getPersistentData().putBoolean(SLAM_ARMED, false);
        player.getPersistentData().putInt(HALLUCINATION, 0);
        player.getPersistentData().putInt(HALLUCINATION_TARGET_ID, -1);
        player.getPersistentData().putInt(SCORPION_STING_TICKS, 0);
        player.getPersistentData().putInt(SCORPION_STING_TARGET_ID, -1);
        player.sendSystemMessage(Component.literal("Kikakogou ativado: " + displayForm(form) + ". Duração: 120s."));
        return 1;
    }

    private static int ability(CommandSourceStack source, String slot) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
        if (!state.active()) return 0;
        String form = normalizeForm(state.type());
        if ("crocodile".equals(form)) {
            if ("z".equalsIgnoreCase(slot)) return crocodileBarragem(player);
            if ("c".equalsIgnoreCase(slot)) return crocodileSuperJump(player);
        } else if ("scorpion".equals(form)) {
            if ("z".equalsIgnoreCase(slot)) return scorpionSting(player);
            if ("c".equalsIgnoreCase(slot)) return scorpionHallucination(player);
        } else if ("tentacle".equals(form)) {
            if ("z".equalsIgnoreCase(slot)) return tentacleWhip(player);
            if ("c".equalsIgnoreCase(slot)) return tentacleGrab(player);
        }
        return 0;
    }

    private static int crocodileBarragem(ServerPlayer player) {
        long now = player.tickCount;
        if (now - player.getPersistentData().getLong(LAST_Z) < 10) return 0;
        player.getPersistentData().putLong(LAST_Z, now);
        AABB area = player.getBoundingBox().inflate(3.0D, 1.5D, 3.0D);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area, target -> target != player && target.isAlive());
        if (targets.isEmpty()) return 0;
        float damage = 4.0F + Math.max(0, player.getData(ModAttachments.PLAYER_DATA).strength() - 1) * 0.35F;
        for (LivingEntity target : targets) for (int i = 0; i < 5; i++) target.hurt(player.damageSources().playerAttack(player), damage);
        player.serverLevel().sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.6D, 0.7D, 0.6D, 0.05D);
        return 1;
    }

    private static int crocodileSuperJump(ServerPlayer player) {
        long now = player.tickCount;
        if (now - player.getPersistentData().getLong(LAST_C) < 20 || !player.onGround()) return 0;
        player.getPersistentData().putLong(LAST_C, now);
        player.getPersistentData().putBoolean(SLAM_ARMED, true);
        player.setDeltaMovement(player.getDeltaMovement().x, 1.15D, player.getDeltaMovement().z);
        player.hurtMarked = true;
        return 1;
    }

    private static int scorpionSting(ServerPlayer player) {
        long now = player.tickCount;
        if (now - player.getPersistentData().getLong(LAST_Z) < 20) return 0;

        LivingEntity target = nearestTarget(player, 4.5D);
        if (target == null) return 0;

        player.getPersistentData().putLong(LAST_Z, now);
        player.getPersistentData().putInt(SCORPION_STING_TARGET_ID, target.getId());
        player.getPersistentData().putInt(SCORPION_STING_TICKS, SCORPION_STING_CONTACT_TICKS);
        return 1;
    }

    private static void resolveScorpionSting(ServerPlayer player) {
        int targetId = player.getPersistentData().getInt(SCORPION_STING_TARGET_ID);
        player.getPersistentData().putInt(SCORPION_STING_TARGET_ID, -1);
        LivingEntity target = findLivingEntity(player, targetId);
        if (target == null || !target.isAlive() || player.distanceToSqr(target) > 6.0D * 6.0D) return;

        float damage = 5.0F + Math.max(0, player.getData(ModAttachments.PLAYER_DATA).strength() - 1) * 0.30F;
        target.hurt(player.damageSources().playerAttack(player), damage);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1));
        target.setDeltaMovement(target.getDeltaMovement().x, 0.45D, target.getDeltaMovement().z);
        target.hurtMarked = true;
        player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 14, 0.35D, 0.45D, 0.35D, 0.05D);
    }

    private static int scorpionHallucination(ServerPlayer player) {
        if (player.getPersistentData().getInt(HALLUCINATION) > 0) return 0;

        LivingEntity target = nearestTarget(player, 10.0D);
        if (target == null) {
            player.sendSystemMessage(Component.literal("Alucinações: nenhum inimigo próximo."));
            return 0;
        }

        player.getPersistentData().putInt(HALLUCINATION, HALLUCINATION_TICKS);
        player.getPersistentData().putInt(HALLUCINATION_TARGET_ID, target.getId());
        player.sendSystemMessage(Component.literal("Alucinações aplicadas ao alvo mais próximo por 10s."));
        return 1;
    }

    private static int tentacleWhip(ServerPlayer player) {
        long now = player.tickCount;
        if (now - player.getPersistentData().getLong(LAST_Z) < 15) return 0;
        player.getPersistentData().putLong(LAST_Z, now);
        AABB area = player.getBoundingBox().inflate(5.0D, 1.8D, 5.0D);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area, target -> target != player && target.isAlive());
        if (targets.isEmpty()) return 0;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), 6.0F);
            Vec3 away = target.position().subtract(player.position());
            if (away.lengthSqr() > 0.0001D) away = away.normalize().scale(0.9D);
            target.setDeltaMovement(away.x, 0.25D, away.z);
            target.hurtMarked = true;
        }
        player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0D, player.getZ(), 3, 1.2D, 0.6D, 1.2D, 0.1D);
        return 1;
    }

    private static int tentacleGrab(ServerPlayer player) {
        long now = player.tickCount;
        if (now - player.getPersistentData().getLong(LAST_C) < 20) return 0;
        player.getPersistentData().putLong(LAST_C, now);
        LivingEntity target = nearestTarget(player, 8.0D);
        if (target == null) return 0;
        Vec3 delta = player.position().add(0.0D, 1.0D, 0.0D).subtract(target.position());
        if (delta.lengthSqr() < 0.0001D) return 0;
        Vec3 pull = delta.normalize().scale(Math.min(1.4D, Math.max(0.4D, delta.length() * 0.22D)));
        target.setDeltaMovement(pull.x, 0.35D, pull.z);
        target.hurtMarked = true;
        target.hurt(player.damageSources().playerAttack(player), 3.0F);
        return 1;
    }

    private static LivingEntity nearestTarget(ServerPlayer player, double radius) {
        AABB area = player.getBoundingBox().inflate(radius);
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player && entity.isAlive())) {
            double distance = player.distanceToSqr(target);
            if (distance < best) {
                best = distance;
                nearest = target;
            }
        }
        return nearest;
    }

    private static LivingEntity findLivingEntity(ServerPlayer player, int entityId) {
        if (entityId < 0) return null;
        var entity = player.serverLevel().getEntity(entityId);
        return entity instanceof LivingEntity living ? living : null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;

        int hallucination = player.getPersistentData().getInt(HALLUCINATION);
        if (hallucination > 0) {
            player.getPersistentData().putInt(HALLUCINATION, hallucination - 1);
            LivingEntity target = findLivingEntity(player, player.getPersistentData().getInt(HALLUCINATION_TARGET_ID));
            if (target != null && target.isAlive()) {
                target.setDeltaMovement(Vec3.ZERO);
                target.hurtMarked = true;
                if (player.tickCount % 10 == 0) {
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 25, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0));
                }
            } else {
                player.getPersistentData().putInt(HALLUCINATION, 0);
                player.getPersistentData().putInt(HALLUCINATION_TARGET_ID, -1);
            }
        } else {
            player.getPersistentData().putInt(HALLUCINATION_TARGET_ID, -1);
        }

        int stingTicks = player.getPersistentData().getInt(SCORPION_STING_TICKS);
        if (stingTicks > 0) {
            stingTicks--;
            player.getPersistentData().putInt(SCORPION_STING_TICKS, stingTicks);
            if (stingTicks == 0) resolveScorpionSting(player);
        }

        KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
        if (state.active()) {
            if (player.tickCount % 5 == 0) applyTransformationAttributes(player, state.type());
            if ("crocodile".equals(state.type()) && player.getPersistentData().getBoolean(SLAM_ARMED) && player.onGround() && player.getDeltaMovement().y <= 0.05D) resolveCrocodileSlam(player);
            if (player.tickCount % 20 == 0) {
                int remaining = state.remainingTicks() - 20;
                if (remaining <= 0) deactivate(player, false);
                else player.setData(ModAttachments.KIKAKOGOU_STATE, new KikakogouState(state.type(), true, remaining, 0));
            }
        } else if (state.cooldownTicks() > 0 && player.tickCount % 20 == 0) {
            player.setData(ModAttachments.KIKAKOGOU_STATE, new KikakogouState(state.type(), false, 0, Math.max(0, state.cooldownTicks() - 20)));
        }
    }

    private static void resolveCrocodileSlam(ServerPlayer player) {
        player.getPersistentData().putBoolean(SLAM_ARMED, false);
        AABB area = player.getBoundingBox().inflate(4.0D, 1.0D, 4.0D);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player && entity.isAlive())) target.hurt(player.damageSources().playerAttack(player), 8.0F);
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
            if (state.active()) {
                if ("crocodile".equals(state.type())) event.getContainer().setNewDamage(event.getNewDamage() * 0.70F);
                if ("scorpion".equals(state.type())) event.getContainer().setNewDamage(event.getNewDamage() * 0.85F);
            }
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
            if (state.active()) {
                if ("crocodile".equals(state.type())) event.getContainer().setNewDamage(event.getNewDamage() * 1.45F);
                if ("scorpion".equals(state.type())) event.getContainer().setNewDamage(event.getNewDamage() * 1.20F);
            }
        }
    }

    private static void applyTransformationAttributes(ServerPlayer player, String form) {
        var armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null && !armor.hasModifier(ARMOR_LOCK_ID)) {
            double amount = player.getPersistentData().getInt(ARMOR_AMOUNT);
            armor.addOrUpdateTransientModifier(new AttributeModifier(ARMOR_LOCK_ID, -amount, AttributeModifier.Operation.ADD_VALUE));
        }
        var scale = player.getAttribute(Attributes.SCALE);
        if (scale != null && "crocodile".equals(form) && !scale.hasModifier(SCALE_ID)) scale.addOrUpdateTransientModifier(new AttributeModifier(SCALE_ID, 0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && "crocodile".equals(form) && !speed.hasModifier(CROCODILE_SLOW_ID)) speed.addOrUpdateTransientModifier(new AttributeModifier(CROCODILE_SLOW_ID, -0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void removeTransformationAttributes(ServerPlayer player) {
        var armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.removeModifier(ARMOR_LOCK_ID);
        var scale = player.getAttribute(Attributes.SCALE);
        if (scale != null) scale.removeModifier(SCALE_ID);
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(CROCODILE_SLOW_ID);
        player.getPersistentData().putBoolean(SLAM_ARMED, false);
        player.getPersistentData().putInt(HALLUCINATION, 0);
        player.getPersistentData().putInt(HALLUCINATION_TARGET_ID, -1);
        player.getPersistentData().putInt(SCORPION_STING_TICKS, 0);
        player.getPersistentData().putInt(SCORPION_STING_TARGET_ID, -1);
    }

    private static void deactivate(ServerPlayer player, boolean manual) {
        KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
        removeTransformationAttributes(player);
        player.setData(ModAttachments.KIKAKOGOU_STATE, new KikakogouState(state.type(), false, 0, COOLDOWN_TICKS));
        player.sendSystemMessage(Component.literal(manual ? "Kikakogou desativado. Recarga iniciada: 150s." : "Kikakogou terminou. Recarga iniciada: 150s."));
    }

    public static boolean isActive(ServerPlayer player) {
        return player.getData(ModAttachments.KIKAKOGOU_STATE).active();
    }

    public static String normalizeForm(String kikanType) {
        String type = kikanType == null ? "" : kikanType.trim().toLowerCase(Locale.ROOT);
        if (type.contains("crocod")) return "crocodile";
        if (type.contains("escorp") || type.contains("scorpion")) return "scorpion";
        if (type.contains("tent")) return "tentacle";
        return "NONE";
    }

    private static String displayForm(String form) {
        if ("crocodile".equals(form)) return "Homem-Crocodilo";
        if ("scorpion".equals(form)) return "Escorpião";
        if ("tentacle".equals(form)) return "Kikan Tentáculo";
        return form;
    }
}
