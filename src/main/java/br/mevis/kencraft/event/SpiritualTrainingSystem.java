package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.SpiritualState;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/** Real spiritual dimension and Interior Spirit training for The Paradise and The King of Lies. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpiritualTrainingSystem {
    public static final ResourceKey<Level> SPIRITUAL_WORLD = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "spiritual_world"));
    private static final String TRAINING = "kencraft_spirit_training";
    private static final String TRAINING_TECHNIQUE = "kencraft_spirit_training_technique";
    private static final String ORIGIN_DIM = "kencraft_spirit_origin_dimension";
    private static final String ORIGIN_X = "kencraft_spirit_origin_x";
    private static final String ORIGIN_Y = "kencraft_spirit_origin_y";
    private static final String ORIGIN_Z = "kencraft_spirit_origin_z";
    private static final String ORIGIN_YAW = "kencraft_spirit_origin_yaw";
    private static final String ORIGIN_PITCH = "kencraft_spirit_origin_pitch";
    private static final String SUJO_EXPIRY = "kencraft_sujo_expiry";
    private static final String SPIRIT_TAG = "kencraft_interior_spirit";
    private static final String PARADISE = "The Paradise";
    private static final String KING = "The King of Lies";

    private SpiritualTrainingSystem() {}

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getPersistentData().getBoolean(TRAINING)) {
            tickTraining(player);
            return;
        }

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        String technique = PlayerData.normalizeTechnique(data.jioTechnique());
        if (!player.getData(ModAttachments.SPIRITUAL_STATE).isSujo()) {
            if (player.getPersistentData().getLong(SUJO_EXPIRY) != 0L) player.getPersistentData().putLong(SUJO_EXPIRY, 0L);
            return;
        }

        if ((PARADISE.equals(technique) || KING.equals(technique)) && !isTrained(player, technique)) {
            player.setData(ModAttachments.SPIRITUAL_STATE, SpiritualState.DEFAULT);
            player.getPersistentData().putLong(SUJO_EXPIRY, 0L);
            startTraining(player, technique);
            return;
        }

        long expiry = player.getPersistentData().getLong(SUJO_EXPIRY);
        if (expiry == 0L) {
            int duration = (PARADISE.equals(technique) || KING.equals(technique)) ? 2000 : 800;
            player.getPersistentData().putLong(SUJO_EXPIRY, player.serverLevel().getGameTime() + duration);
        } else if (player.serverLevel().getGameTime() >= expiry) {
            player.setData(ModAttachments.SPIRITUAL_STATE, SpiritualState.DEFAULT);
            player.getPersistentData().putLong(SUJO_EXPIRY, 0L);
            player.sendSystemMessage(Component.literal("Seu Estado Sujo terminou. O poder voltou ao estado normal."));
        }
    }

    private static boolean isTrained(ServerPlayer player, String technique) {
        return player.getPersistentData().getBoolean("kencraft_spirit_trained_" + safe(technique));
    }
    private static String safe(String technique) { return technique.toLowerCase().replaceAll("[^a-z0-9]+", "_"); }

    private static void startTraining(ServerPlayer player, String technique) {
        if (player.getServer() == null) return;
        ServerLevel level = player.getServer().getLevel(SPIRITUAL_WORLD);
        if (level == null) {
            player.sendSystemMessage(Component.literal("O Mundo Espiritual não está disponível. Reinicie o mundo para carregar a nova dimensão."));
            return;
        }
        saveOrigin(player);
        player.getPersistentData().putBoolean(TRAINING, true);
        player.getPersistentData().putString(TRAINING_TECHNIQUE, technique);
        ensureArena(level);
        for (RinkaEntity old : level.getEntitiesOfClass(RinkaEntity.class, new net.minecraft.world.phys.AABB(-32, 64, -32, 32, 90, 32), e -> e.getTags().contains(SPIRIT_TAG))) old.discard();

        RinkaEntity spirit = KenCraftEntities.RINKA.get().create(level);
        if (spirit == null) { clearTraining(player); return; }
        spirit.moveTo(0.5D, 66.0D, 8.5D, 180.0F, 0.0F);
        spirit.addTag(SPIRIT_TAG);
        spirit.setCustomName(Component.literal("Espírito Interior — " + technique));
        spirit.setCustomNameVisible(true);
        spirit.setPersistenceRequired();
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500.0D);
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(16.0D);
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(8.0D);
        spirit.setHealth(500.0F);
        level.addFreshEntity(spirit);
        player.teleportTo(level, 0.5D, 66.0D, -8.5D, player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.literal("§5Seu Espírito Interior apareceu. Vença-o para dominar o Estado Sujo por 100 segundos."));
        player.sendSystemMessage(Component.literal("§7Técnica do Espírito: " + technique));
    }

    private static void tickTraining(ServerPlayer player) {
        if (player.serverLevel().dimension() != SPIRITUAL_WORLD) {
            if (player.getServer() != null) {
                ServerLevel level = player.getServer().getLevel(SPIRITUAL_WORLD);
                if (level != null) player.teleportTo(level, 0.5D, 66.0D, -8.5D, player.getYRot(), player.getXRot());
            }
            return;
        }
        String technique = player.getPersistentData().getString(TRAINING_TECHNIQUE);
        List<RinkaEntity> spirits = player.serverLevel().getEntitiesOfClass(RinkaEntity.class, player.getBoundingBox().inflate(32.0D), e -> e.getTags().contains(SPIRIT_TAG) && e.isAlive());
        if (spirits.isEmpty()) { startReplacementSpirit(player, technique); return; }
        if (player.tickCount % 40 == 0) useSpiritTechnique(player, spirits.get(0), technique);
    }

    private static void startReplacementSpirit(ServerPlayer player, String technique) {
        RinkaEntity spirit = KenCraftEntities.RINKA.get().create(player.serverLevel());
        if (spirit == null) return;
        spirit.moveTo(0.5D, 66.0D, 8.5D, 180.0F, 0.0F);
        spirit.addTag(SPIRIT_TAG);
        spirit.setCustomName(Component.literal("Espírito Interior — " + technique));
        spirit.setCustomNameVisible(true);
        spirit.setPersistenceRequired();
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500.0D);
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(16.0D);
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(8.0D);
        spirit.setHealth(500.0F);
        player.serverLevel().addFreshEntity(spirit);
    }

    private static void useSpiritTechnique(ServerPlayer player, LivingEntity spirit, String technique) {
        if (PARADISE.equals(technique)) {
            player.addEffect(new MobEffectInstance(KenCraftEffects.SUFOCO, 80, 0, false, true, true));
            player.hurt(player.damageSources().mobAttack(spirit), 12.0F);
            player.sendSystemMessage(Component.literal("§5O Espírito Interior usa The Paradise."));
        } else if (KING.equals(technique)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 3, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true));
            player.hurt(player.damageSources().mobAttack(spirit), 10.0F);
            player.sendSystemMessage(Component.literal("§5O Espírito Interior usa The King of Lies."));
        }
    }

    @SubscribeEvent
    public static void onSpiritDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof RinkaEntity spirit) || !spirit.getTags().contains(SPIRIT_TAG)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !player.getPersistentData().getBoolean(TRAINING)) return;
        String technique = player.getPersistentData().getString(TRAINING_TECHNIQUE);
        player.getPersistentData().putBoolean("kencraft_spirit_trained_" + safe(technique), true);
        clearTraining(player);
        player.setData(ModAttachments.SPIRITUAL_STATE, new SpiritualState(SpiritualState.SUJO));
        player.getPersistentData().putLong(SUJO_EXPIRY, player.serverLevel().getGameTime() + 2000L);
        returnToOrigin(player);
        player.sendSystemMessage(Component.literal("§aVocê venceu seu Espírito Interior! " + technique + " — Estado Sujo desbloqueado por 100 segundos."));
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getPersistentData().getBoolean(TRAINING)) {
            clearTraining(player);
            player.setData(ModAttachments.SPIRITUAL_STATE, SpiritualState.DEFAULT);
        }
    }

    private static void saveOrigin(ServerPlayer player) {
        var nbt = player.getPersistentData();
        nbt.putString(ORIGIN_DIM, player.serverLevel().dimension().location().toString());
        nbt.putDouble(ORIGIN_X, player.getX()); nbt.putDouble(ORIGIN_Y, player.getY()); nbt.putDouble(ORIGIN_Z, player.getZ());
        nbt.putFloat(ORIGIN_YAW, player.getYRot()); nbt.putFloat(ORIGIN_PITCH, player.getXRot());
    }
    private static void returnToOrigin(ServerPlayer player) {
        if (player.getServer() == null) return;
        ResourceLocation id = ResourceLocation.tryParse(player.getPersistentData().getString(ORIGIN_DIM));
        if (id == null) return;
        ResourceKey<Level> key = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id);
        ServerLevel level = player.getServer().getLevel(key);
        if (level == null) return;
        var nbt = player.getPersistentData();
        player.teleportTo(level, nbt.getDouble(ORIGIN_X), nbt.getDouble(ORIGIN_Y), nbt.getDouble(ORIGIN_Z), nbt.getFloat(ORIGIN_YAW), nbt.getFloat(ORIGIN_PITCH));
    }
    private static void clearTraining(ServerPlayer player) {
        var nbt = player.getPersistentData();
        nbt.putBoolean(TRAINING, false); nbt.remove(TRAINING_TECHNIQUE); nbt.remove(ORIGIN_DIM);
        nbt.remove(ORIGIN_X); nbt.remove(ORIGIN_Y); nbt.remove(ORIGIN_Z); nbt.remove(ORIGIN_YAW); nbt.remove(ORIGIN_PITCH);
    }

    /** Closed 41x21x41 spiritual cube with blue/deepslate decoration. */
    public static void ensureArena(ServerLevel level) {
        for (int x = -20; x <= 20; x++) for (int z = -20; z <= 20; z++) {
            level.setBlock(new BlockPos(x, 64, z), Blocks.OBSIDIAN.defaultBlockState(), 3);
            level.setBlock(new BlockPos(x, 84, z), Blocks.OBSIDIAN.defaultBlockState(), 3);
        }
        for (int y = 65; y <= 83; y++) for (int p = -20; p <= 20; p++) {
            level.setBlock(new BlockPos(-20, y, p), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
            level.setBlock(new BlockPos(20, y, p), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
            level.setBlock(new BlockPos(p, y, -20), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
            level.setBlock(new BlockPos(p, y, 20), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 3);
        }
        for (int y = 65; y <= 83; y++) for (int p : new int[]{-12, 0, 12}) {
            level.setBlock(new BlockPos(-19, y, p), Blocks.BLUE_CONCRETE.defaultBlockState(), 3);
            level.setBlock(new BlockPos(19, y, p), Blocks.BLUE_CONCRETE.defaultBlockState(), 3);
            level.setBlock(new BlockPos(p, y, -19), Blocks.BLUE_CONCRETE.defaultBlockState(), 3);
            level.setBlock(new BlockPos(p, y, 19), Blocks.BLUE_CONCRETE.defaultBlockState(), 3);
        }
        for (int x = -16; x <= 16; x += 8) for (int z = -16; z <= 16; z += 8)
            level.setBlock(new BlockPos(x, 65, z), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
        for (int x = -18; x <= 18; x += 6) {
            level.setBlock(new BlockPos(x, 83, -19), Blocks.SEA_LANTERN.defaultBlockState(), 3);
            level.setBlock(new BlockPos(x, 83, 19), Blocks.SEA_LANTERN.defaultBlockState(), 3);
        }
    }
}
