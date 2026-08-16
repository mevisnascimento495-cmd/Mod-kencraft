package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcSpawn {
    private static final double RANK_C_CHANCE = 0.03D;
    private static final double RINKA_CHANCE = 0.07D;
    private static final double RISHIN_CHANCE = 0.05D;
    private static final double AODAI_CHANCE = 0.05D;
    private static final double ARF_CHANCE = 0.08D;
    private static final double GENERAL_CHANCE = 0.01D;

    private static final int RADIUS = 4;
    private static final int MAX_NIGHT_RINKA = 3;
    private static final int MAX_RANK_C = 1;
    private static final int MAX_RISHIN = 2;
    private static final int MAX_AODAI = 1;
    private static final int MAX_ARF = 2;
    private static final int MAX_GENERAL = 1;

    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final int MAX_PLAYERS_PER_CYCLE = 1;
    private static final double PLAYER_CHECK_RADIUS = 128.0D;

    private static volatile boolean structureLocateInProgress;
    private static int cycleCursor;

    private KenCraftNpcSpawn() {}

    public static void setStructureLocateInProgress(boolean active) {
        structureLocateInProgress = active;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (structureLocateInProgress) return;
        if (event.getServer().getTickCount() % CHECK_INTERVAL_TICKS != 0) return;

        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        cycleCursor %= players.size();
        ServerPlayer player = players.get(cycleCursor++);
        if (!player.isAlive()) return;

        ServerLevel level = player.serverLevel();
        if (!level.dimensionType().natural() || level.getDifficulty().getId() == 0) return;

        ChunkPos cp = player.chunkPosition();
        double roll = ThreadLocalRandom.current().nextDouble();

        // Only one player is processed per cycle and only the selected roll
        // performs an entity search. This keeps exploration work bounded.
        if (roll < AODAI_CHANCE) {
            AABB nearby = nearbyBounds(level, cp);
            int aodai = level.getEntitiesOfClass(AodaiEntity.class, nearby, e -> true).size();
            if (aodai < MAX_AODAI && local(level, cp, AodaiEntity.class).isEmpty()) {
                spawn(level, cp, KenCraftEntities.AODAI.get());
                return;
            }
        }

        if (isNight(level)) {
            if (roll < RANK_C_CHANCE) {
                AABB nearby = nearbyBounds(level, cp);
                int r = level.getEntitiesOfClass(RinkaEntity.class, nearby, e -> true).size();
                int c = level.getEntitiesOfClass(RankCRinkaEntity.class, nearby, e -> true).size();
                if (c < MAX_RANK_C && r + c < MAX_NIGHT_RINKA && local(level, cp, RankCRinkaEntity.class).isEmpty()) {
                    spawn(level, cp, KenCraftEntities.RANK_C_RINKA.get());
                    return;
                }
            }

            if (roll < RANK_C_CHANCE + RISHIN_CHANCE) {
                AABB nearby = nearbyBounds(level, cp);
                int rs = level.getEntitiesOfClass(RishinEntity.class, nearby, e -> true).size();
                if (rs < MAX_RISHIN && local(level, cp, RishinEntity.class).isEmpty()) {
                    spawn(level, cp, KenCraftEntities.RISHIN.get());
                    return;
                }
            }

            if (roll < RANK_C_CHANCE + RISHIN_CHANCE + RINKA_CHANCE) {
                AABB nearby = nearbyBounds(level, cp);
                int r = level.getEntitiesOfClass(RinkaEntity.class, nearby, e -> true).size();
                int c = level.getEntitiesOfClass(RankCRinkaEntity.class, nearby, e -> true).size();
                if (r + c < MAX_NIGHT_RINKA && local(level, cp, RinkaEntity.class).isEmpty()) {
                    spawn(level, cp, KenCraftEntities.RINKA.get());
                }
            }
            return;
        }

        // Rinkas are no longer scanned/discarded on every chunk load during the day.
        // Existing entities are left untouched; new Rinkas simply do not spawn in daylight.
        if (roll < GENERAL_CHANCE) {
            AABB nearby = nearbyBounds(level, cp);
            int g = level.getEntitiesOfClass(ArfGeneralEntity.class, nearby, e -> true).size();
            if (g < MAX_GENERAL) {
                spawn(level, cp, KenCraftEntities.ARF_GENERAL.get());
                return;
            }
        }

        if (roll < GENERAL_CHANCE + ARF_CHANCE) {
            AABB nearby = nearbyBounds(level, cp);
            int ai = level.getEntitiesOfClass(ArfInvestigatorEntity.class, nearby, e -> true).size();
            if (ai < MAX_ARF && local(level, cp, ArfInvestigatorEntity.class).isEmpty()) {
                spawn(level, cp, KenCraftEntities.ARF_INVESTIGATOR.get());
            }
        }
    }

    private static boolean isNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time >= 13000L && time < 23000L;
    }

    private static <T extends Entity> List<T> local(ServerLevel level, ChunkPos cp, Class<T> type) {
        return level.getEntitiesOfClass(type, chunkBounds(level, cp), e -> true);
    }

    private static void spawn(ServerLevel level, ChunkPos cp, net.minecraft.world.entity.EntityType<?> type) {
        BlockPos pos = find(level, cp);
        if (pos == null) return;

        Entity entity = type.create(level);
        if (entity == null) return;

        entity.moveTo(pos, ThreadLocalRandom.current().nextFloat() * 360F, 0.0F);
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
        }
        level.addFreshEntity(entity);
    }

    private static AABB chunkBounds(ServerLevel level, ChunkPos pos) {
        return new AABB(
                pos.getMinBlockX(), level.getMinBuildHeight(), pos.getMinBlockZ(),
                pos.getMaxBlockX() + 1, level.getMaxBuildHeight(), pos.getMaxBlockZ() + 1
        );
    }

    private static AABB nearbyBounds(ServerLevel level, ChunkPos center) {
        int x = (center.x - RADIUS) << 4;
        int z = (center.z - RADIUS) << 4;
        int maxX = (center.x + RADIUS + 1) << 4;
        int maxZ = (center.z + RADIUS + 1) << 4;
        return new AABB(x, level.getMinBuildHeight(), z, maxX, level.getMaxBuildHeight(), maxZ);
    }

    private static BlockPos find(ServerLevel level, ChunkPos chunk) {
        for (int i = 0; i < 8; i++) {
            int x = chunk.getMinBlockX() + ThreadLocalRandom.current().nextInt(16);
            int z = chunk.getMinBlockZ() + ThreadLocalRandom.current().nextInt(16);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos.below()).isSolid() && level.getBlockState(pos).isAir()) return pos;
        }
        return null;
    }
}
