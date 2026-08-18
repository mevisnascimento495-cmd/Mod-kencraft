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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
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
    private static final ResourceLocation SCALE_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "kikakogou_crocodile_scale");
    private static final ResourceLocation ARMOR_LOCK_ID = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "kikakogou_armor_lock");
    private static final String SLAM_ARMED = "kencraft_kikakogou_slam_armed";
    private static final String LAST_Z = "kencraft_kikakogou_last_z";
    private static final String LAST_C = "kencraft_kikakogou_last_c";
    private KikakogouSystem() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("kencraft").then(Commands.literal("kikakogou")
                .then(Commands.literal("toggle").executes(context -> toggle(context.getSource())))
                .then(Commands.literal("ability").then(Commands.argument("slot", StringArgumentType.word())
                        .executes(context -> ability(context.getSource(), StringArgumentType.getString(context, "slot"))))));
    }

    private static int toggle(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
        if (state.active()) { deactivate(player, true); return 1; }
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.RINKA) { player.sendSystemMessage(Component.literal("A Kikakogou só pode ser usada por Rinkas.")); return 0; }
        if (!"A".equalsIgnoreCase(data.rinkaClass()) && !"S".equalsIgnoreCase(data.rinkaClass())) { player.sendSystemMessage(Component.literal("Você precisa alcançar o Rank A para usar a Kikakogou.")); return 0; }
        if (!KikakogouProgress.isUnlocked(player)) { player.sendSystemMessage(Component.literal("Você ainda não concluiu a missão de Aodai para desbloquear a Kikakogou.")); return 0; }
        if (state.cooldownTicks() > 0) { player.sendSystemMessage(Component.literal("Kikakogou em recarga: " + ((state.cooldownTicks() + 19) / 20) + "s.")); return 0; }
        String form = normalizeForm(data.kikanType());
        if ("NONE".equals(form)) { player.sendSystemMessage(Component.literal("Sua Kikan atual não possui um Kikakogou implementado.")); return 0; }
        player.setData(ModAttachments.KIKAKOGOU_STATE, new KikakogouState(form, true, DURATION_TICKS, 0));
        applyTransformationAttributes(player, form);
        player.getPersistentData().putBoolean(SLAM_ARMED, false);
        player.sendSystemMessage(Component.literal("Kikakogou ativado: " + displayForm(form) + ". Duração: 120s."));
        return 1;
    }

    private static int ability(CommandSourceStack source, String slot) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return 0;
        KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
        if (!state.active()) return 0;
        if ("crocodile".equals(normalizeForm(state.type()))) {
            if ("z".equalsIgnoreCase(slot)) return crocodileBarragem(player);
            if ("c".equalsIgnoreCase(slot)) return crocodileSuperJump(player);
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
        player.level().sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.6D, 0.7D, 0.6D, 0.05D);
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

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;
        KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
        if (state.active()) {
            if (player.tickCount % 5 == 0) applyTransformationAttributes(player, state.type());
            if (player.tickCount % 20 != 0) {
                if ("crocodile".equals(state.type()) && player.getPersistentData().getBoolean(SLAM_ARMED) && player.onGround() && player.getDeltaMovement().y <= 0.05D) resolveCrocodileSlam(player);
                return;
            }
            int remaining = state.remainingTicks() - 20;
            if (remaining <= 0) deactivate(player, false);
            else player.setData(ModAttachments.KIKAKOGOU_STATE, new KikakogouState(state.type(), true, remaining, 0));
            if ("crocodile".equals(state.type()) && player.getPersistentData().getBoolean(SLAM_ARMED) && player.onGround() && player.getDeltaMovement().y <= 0.05D) resolveCrocodileSlam(player);
        } else if (state.cooldownTicks() > 0 && player.tickCount % 20 == 0) {
            player.setData(ModAttachments.KIKAKOGOU_STATE, new KikakogouState(state.type(), false, 0, Math.max(0, state.cooldownTicks() - 20)));
        }
    }

    private static void resolveCrocodileSlam(ServerPlayer player) {
        player.getPersistentData().putBoolean(SLAM_ARMED, false);
        AABB area = player.getBoundingBox().inflate(4.0D, 1.0D, 4.0D);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area, target -> target != player && target.isAlive())) target.hurt(player.damageSources().playerAttack(player), 8.0F);
        player.level().sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
            if (state.active() && "crocodile".equals(state.type())) event.getContainer().setNewDamage(event.getNewDamage() * 0.70F);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
            if (state.active() && "crocodile".equals(state.type())) event.getContainer().setNewDamage(event.getNewDamage() * 1.45F);
        }
    }

    private static void applyTransformationAttributes(ServerPlayer player, String form) {
        var armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.addOrUpdateTransientModifier(new AttributeModifier(ARMOR_LOCK_ID, -player.getArmorValue(), AttributeModifier.Operation.ADD_VALUE));
        var scale = player.getAttribute(Attributes.SCALE);
        if (scale != null) scale.addOrUpdateTransientModifier(new AttributeModifier(SCALE_ID, "crocodile".equals(form) ? 0.25D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void removeTransformationAttributes(ServerPlayer player) {
        var armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.removeModifier(ARMOR_LOCK_ID);
        var scale = player.getAttribute(Attributes.SCALE);
        if (scale != null) scale.removeModifier(SCALE_ID);
        player.getPersistentData().putBoolean(SLAM_ARMED, false);
    }

    private static void deactivate(ServerPlayer player, boolean manual) {
        KikakogouState state = player.getData(ModAttachments.KIKAKOGOU_STATE);
        removeTransformationAttributes(player);
        player.setData(ModAttachments.KIKAKOGOU_STATE, new KikakogouState(state.type(), false, 0, COOLDOWN_TICKS));
        player.sendSystemMessage(Component.literal(manual ? "Kikakogou desativado. Recarga iniciada: 150s." : "Kikakogou terminou. Recarga iniciada: 150s."));
    }

    public static boolean isActive(ServerPlayer player) { return player.getData(ModAttachments.KIKAKOGOU_STATE).active(); }
    public static boolean isActiveClient(net.minecraft.client.player.LocalPlayer player) { return player.getData(ModAttachments.KIKAKOGOU_STATE).active(); }
    public static String normalizeForm(String kikanType) {
        String t = kikanType == null ? "" : kikanType.trim().toLowerCase(Locale.ROOT);
        if (t.contains("crocod")) return "crocodile";
        if (t.contains("escorp")) return "scorpion";
        if (t.contains("tent")) return "tentacle";
        return "NONE";
    }
    private static String displayForm(String form) { return switch (form) {
        case "crocodile" -> "Homem-Crocodilo";
        case "scorpion" -> "Escorpião";
        case "tentacle" -> "Kikan Tentáculo";
        default -> form;
    }; }
}
