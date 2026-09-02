package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.SpiritualState;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpiritualTrainingSystem {
    public static final ResourceKey<Level> SPIRITUAL_WORLD = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "spiritual_world"));
    public static final ResourceKey<Level> PARADISE_TRAINING = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "paradise_training"));
    public static final ResourceKey<Level> KING_TRAINING = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "king_of_lies_training"));

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

    /** Called by the B key command. Training is deliberately not started by the V/Sujo state. */
    public static int startTraining(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        String technique = PlayerData.normalizeTechnique(data.jioTechnique());

        if (technique.equals(JioSystem.SEISHIN) || technique.equals(JioSystem.HAKAI) || technique.equals(JioSystem.KATA)) {
            player.sendSystemMessage(Component.literal("Sua técnica não tem treinamento."));
            return 0;
        }
        if (!PARADISE.equals(technique) && !KING.equals(technique)) {
            player.sendSystemMessage(Component.literal("Você ainda não possui uma técnica com treinamento espiritual."));
            return 0;
        }
        if (data.arfClass() < 2) {
            player.sendSystemMessage(Component.literal("Você precisa estar na Segunda Classe para iniciar seu treinamento espiritual."));
            return 0;
        }
        if (player.getPersistentData().getBoolean(TRAINING)) {
            player.sendSystemMessage(Component.literal("Você já está em um treinamento espiritual."));
            return 0;
        }
        if (player.getServer() == null) return 0;

        ResourceKey<Level> destination = PARADISE.equals(technique) ? PARADISE_TRAINING : KING_TRAINING;
        ServerLevel level = player.getServer().getLevel(destination);
        if (level == null) {
            player.sendSystemMessage(Component.literal("A dimensão do seu treinamento ainda não está disponível. Reinicie o mundo para carregá-la."));
            return 0;
        }

        saveOrigin(player);
        player.getPersistentData().putBoolean(TRAINING, true);
        player.getPersistentData().putString(TRAINING_TECHNIQUE, technique);
        ensureArena(level, technique);

        for (RinkaEntity old : level.getEntitiesOfClass(RinkaEntity.class,
                new AABB(-32, 0, -32, 32, 128, 32), e -> e.getTags().contains(SPIRIT_TAG))) {
            old.discard();
        }

        RinkaEntity spirit = KenCraftEntities.RINKA.get().create(level);
        if (spirit == null) {
            clearTraining(player);
            return 0;
        }
        spirit.moveTo(10.5D, 2.0D, 5.5D, 0.0F, 0.0F);
        spirit.addTag(SPIRIT_TAG);
        spirit.setCustomName(Component.literal("Espírito Interior — " + technique));
        spirit.setCustomNameVisible(true);
        spirit.setPersistenceRequired();
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500.0D);
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(16.0D);
        spirit.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(8.0D);
        spirit.setHealth(500.0F);
        level.addFreshEntity(spirit);

        player.teleportTo(level, 10.5D, 2.0D, 10.5D, player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.literal("Ótimo, vamos para seu treinamento, consiga me derrotar aqui e agora e vou te dar o potencial total do seu estado sujo."));
        return 1;
    }

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
            if (player.getPersistentData().getLong(SUJO_EXPIRY) != 0L) {
                player.getPersistentData().putLong(SUJO_EXPIRY, 0L);
            }
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

    private static void tickTraining(ServerPlayer player) {
        if (player.getServer() == null) return;
        String technique = player.getPersistentData().getString(TRAINING_TECHNIQUE);
        ResourceKey<Level> expected = PARADISE.equals(technique) ? PARADISE_TRAINING : KING_TRAINING;
        if (player.serverLevel().dimension() != expected) {
            ServerLevel level = player.getServer().getLevel(expected);
            if (level != null) player.teleportTo(level, 10.5D, 2.0D, 10.5D, player.getYRot(), player.getXRot());
            return;
        }

        List<RinkaEntity> spirits = player.serverLevel().getEntitiesOfClass(RinkaEntity.class,
                player.getBoundingBox().inflate(32.0D), e -> e.getTags().contains(SPIRIT_TAG) && e.isAlive());
        if (spirits.isEmpty()) {
            startReplacementSpirit(player, technique);
            return;
        }
        if (player.tickCount % 40 == 0) useSpiritTechnique(player, spirits.get(0), technique);
    }

    private static void startReplacementSpirit(ServerPlayer player, String technique) {
        RinkaEntity spirit = KenCraftEntities.RINKA.get().create(player.serverLevel());
        if (spirit == null) return;
        spirit.moveTo(10.5D, 2.0D, 5.5D, 0.0F, 0.0F);
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
            player.setDeltaMovement(player.getDeltaMovement().x, 0.65D, player.getDeltaMovement().z);
            player.hurtMarked = true;
        } else if (KING.equals(technique)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 3, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
            player.hurt(player.damageSources().mobAttack(spirit), 10.0F);
        }
    }

    @SubscribeEvent
    public static void onSpiritDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof RinkaEntity spirit) || !spirit.getTags().contains(SPIRIT_TAG)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!player.getPersistentData().getBoolean(TRAINING)) return;

        String technique = player.getPersistentData().getString(TRAINING_TECHNIQUE);
        player.getPersistentData().putBoolean("kencraft_spirit_trained_" + safe(technique), true);
        clearTraining(player);
        player.setData(ModAttachments.SPIRITUAL_STATE, new SpiritualState(SpiritualState.SUJO));
        player.getPersistentData().putLong(SUJO_EXPIRY, player.serverLevel().getGameTime() + 2000L);
        returnToOrigin(player);
        player.sendSystemMessage(Component.literal("Você venceu seu Espírito Interior! " + technique + " — Estado Sujo desbloqueado."));
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
        nbt.putDouble(ORIGIN_X, player.getX());
        nbt.putDouble(ORIGIN_Y, player.getY());
        nbt.putDouble(ORIGIN_Z, player.getZ());
        nbt.putFloat(ORIGIN_YAW, player.getYRot());
        nbt.putFloat(ORIGIN_PITCH, player.getXRot());
    }

    private static void returnToOrigin(ServerPlayer player) {
        if (player.getServer() == null) return;
        ResourceLocation id = ResourceLocation.tryParse(player.getPersistentData().getString(ORIGIN_DIM));
        if (id == null) return;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);
        ServerLevel level = player.getServer().getLevel(key);
        if (level == null) return;
        var nbt = player.getPersistentData();
        player.teleportTo(level, nbt.getDouble(ORIGIN_X), nbt.getDouble(ORIGIN_Y), nbt.getDouble(ORIGIN_Z),
                nbt.getFloat(ORIGIN_YAW), nbt.getFloat(ORIGIN_PITCH));
    }

    private static void clearTraining(ServerPlayer player) {
        var nbt = player.getPersistentData();
        nbt.putBoolean(TRAINING, false);
        nbt.remove(TRAINING_TECHNIQUE);
        nbt.remove(ORIGIN_DIM);
        nbt.remove(ORIGIN_X);
        nbt.remove(ORIGIN_Y);
        nbt.remove(ORIGIN_Z);
        nbt.remove(ORIGIN_YAW);
        nbt.remove(ORIGIN_PITCH);
    }

    /** Builds the requested 20x20 training room in the selected training dimension. */
    public static void ensureArena(ServerLevel level, String technique) {
        boolean paradise = PARADISE.equals(technique);
        for (int x = 0; x < 20; x++) {
            for (int z = 0; z < 20; z++) {
                level.setBlock(new BlockPos(x, 0, z), paradise ? Blocks.DIRT.defaultBlockState() : Blocks.NETHER_BRICKS.defaultBlockState(), 3);
                for (int y = 1; y <= 20; y++) {
                    boolean wall = x == 0 || x == 19 || z == 0 || z == 19;
                    level.setBlock(new BlockPos(x, y, z), wall
                            ? (paradise ? Blocks.WHITE_CONCRETE.defaultBlockState() : Blocks.RED_CONCRETE.defaultBlockState())
                            : Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        if (paradise) {
            for (int x = 2; x < 18; x += 3) {
                for (int z = 2; z < 18; z += 4) {
                    level.setBlock(new BlockPos(x, 1, z), Blocks.BLUE_ORCHID.defaultBlockState(), 3);
                    if (x + 1 < 19) level.setBlock(new BlockPos(x + 1, 1, z), Blocks.POPPY.defaultBlockState(), 3);
                }
            }
        } else {
            int[][] barrels = {{3, 3}, {16, 3}, {3, 16}, {16, 16}, {5, 14}, {14, 5}};
            for (int[] p : barrels) level.setBlock(new BlockPos(p[0], 1, p[1]), Blocks.BARREL.defaultBlockState(), 3);
        }
    }

    /** Compatibility helper retained for the older spiritual-world system. */
    public static void ensureArena(ServerLevel level) {
        ensureArena(level, PARADISE);
    }

    private static String safe(String technique) {
        return technique.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }
}
