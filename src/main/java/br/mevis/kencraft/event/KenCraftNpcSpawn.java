package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.ArfGeneralEntity;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RankCRinkaEntity;
import br.mevis.kencraft.entity.RinkaEntity;
import br.mevis.kencraft.entity.RishinEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.concurrent.ThreadLocalRandom;

/** Natural KenCraft NPC spawning. Rinkas are exclusive to night; ARF is exclusive to day. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcSpawn {
    // Chunk-load events can fire for many chunks while a player travels. Keep both
    // the per-chunk chance and the nearby population bounded so exploration cannot
    // continuously accumulate KenCraft NPCs.
    private static final double RANK_C_RINKA_CHANCE = 0.03D;
    private static final double RINKA_CHANCE = 0.07D;
    private static final double RISHIN_CHANCE = 0.0D;
    private static final double ARF_CHANCE = 0.08D;
    private static final double GENERAL_CHANCE = 0.01D;

    private static final int SPAWN_RADIUS_CHUNKS = 4;
    private static final int MAX_NIGHT_RINKAS_NEARBY = 3;
    private static final int MAX_RANK_C_RINKAS_NEARBY = 1;
    private static final int MAX_ARF_INVESTIGATORS_NEARBY = 2;
    private static final int MAX_ARF_GENERALS_NEARBY = 1;

    private KenCraftNpcSpawn() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        if (!level.dimensionType().natural()) return;
        if (level.getDifficulty().getId() == 0) return;

        boolean night = isNight(level);
        ChunkPos chunkPos = chunk.getPos();
        var nearby = nearbyBounds(level, chunkPos);
        double roll = ThreadLocalRandom.current().nextDouble();

        if (night) {
            int nearbyRinkas = level.getEntitiesOfClass(RinkaEntity.class, nearby, e -> true).size();
            int nearbyRankCRinkas = level.getEntitiesOfClass(RankCRinkaEntity.class, nearby, e -> true).size();

            if (nearbyRankCRinkas < MAX_RANK_C_RINKAS_NEARBY
                    && nearbyRinkas + nearbyRankCRinkas < MAX_NIGHT_RINKAS_NEARBY
                    && roll < RANK_C_RINKA_CHANCE
                    && level.getEntitiesOfClass(RankCRinkaEntity.class, chunkBounds(level, chunkPos), e -> true).isEmpty()) {
                spawnRankCRinka(level, chunkPos);
                return;
            }

            if (roll < RANK_C_RINKA_CHANCE + RISHIN_CHANCE
                    && level.getEntitiesOfClass(RishinEntity.class, chunkBounds(level, chunkPos), e -> true).isEmpty()) {
                spawnRishin(level, chunkPos);
                return;
            }

            if (nearbyRinkas + nearbyRankCRinkas < MAX_NIGHT_RINKAS_NEARBY
                    && roll < RANK_C_RINKA_CHANCE + RISHIN_CHANCE + RINKA_CHANCE
                    && level.getEntitiesOfClass(RinkaEntity.class, chunkBounds(level, chunkPos), e -> true).isEmpty()) {
                spawnRinka(level, chunkPos);
            }
            return;
        }

        // Daytime: remove both normal and Rank C Rinkas from newly loaded chunks.
        for (Entity entity : level.getEntitiesOfClass(RinkaEntity.class, chunkBounds(level, chunkPos), e -> true)) {
            entity.discard();
        }
        for (Entity entity : level.getEntitiesOfClass(RankCRinkaEntity.class, chunkBounds(level, chunkPos), e -> true)) {
            entity.discard();
        }

        int nearbyInvestigators = level.getEntitiesOfClass(ArfInvestigatorEntity.class, nearby, e -> true).size();
        int nearbyGenerals = level.getEntitiesOfClass(ArfGeneralEntity.class, nearby, e -> true).size();

        if (nearbyGenerals < MAX_ARF_GENERALS_NEARBY
                && roll < GENERAL_CHANCE) {
            spawnGeneral(level, chunkPos);
            return;
        }

        if (nearbyInvestigators < MAX_ARF_INVESTIGATORS_NEARBY
                && roll < GENERAL_CHANCE + ARF_CHANCE
                && level.getEntitiesOfClass(ArfInvestigatorEntity.class, chunkBounds(level, chunkPos), e -> true).isEmpty()) {
            spawnInvestigator(level, chunkPos);
        }
    }

    private static void spawnRankCRinka(ServerLevel level, ChunkPos chunkPos) {
        BlockPos pos = findSpawnPosition(level, chunkPos);
        if (pos == null) return;
        RankCRinkaEntity entity = KenCraftEntities.RANK_C_RINKA.get().create(level);
        if (entity == null) return;
        entity.moveTo(pos, ThreadLocalRandom.current().nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
        level.addFreshEntity(entity);
    }

    private static void spawnRinka(ServerLevel level, ChunkPos chunkPos) {
        BlockPos pos = findSpawnPosition(level, chunkPos);
        if (pos == null) return;
        RinkaEntity entity = KenCraftEntities.RINKA.get().create(level);
        if (entity == null) return;
        entity.moveTo(pos, ThreadLocalRandom.current().nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
        level.addFreshEntity(entity);
    }

    private static void spawnRishin(ServerLevel level, ChunkPos chunkPos) {
        BlockPos pos = findSpawnPosition(level, chunkPos);
        if (pos == null) return;
        RishinEntity entity = KenCraftEntities.RISHIN.get().create(level);
        if (entity == null) return;
        entity.moveTo(pos, ThreadLocalRandom.current().nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
        level.addFreshEntity(entity);
    }

    private static void spawnInvestigator(ServerLevel level, ChunkPos chunkPos) {
        BlockPos pos = findSpawnPosition(level, chunkPos);
        if (pos == null) return;
        ArfInvestigatorEntity entity = KenCraftEntities.ARF_INVESTIGATOR.get().create(level);
        if (entity == null) return;
        entity.moveTo(pos, ThreadLocalRandom.current().nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
        level.addFreshEntity(entity);
    }

    private static void spawnGeneral(ServerLevel level, ChunkPos chunkPos) {
        if (!level.getEntitiesOfClass(ArfGeneralEntity.class, chunkBounds(level, chunkPos), e -> true).isEmpty()) return;
        BlockPos pos = findSpawnPosition(level, chunkPos);
        if (pos == null) return;
        ArfGeneralEntity entity = KenCraftEntities.ARF_GENERAL.get().create(level);
        if (entity == null) return;
        entity.moveTo(pos, ThreadLocalRandom.current().nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
        level.addFreshEntity(entity);
    }

    private static boolean isNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time >= 13000L && time < 23000L;
    }

    private static net.minecraft.world.phys.AABB chunkBounds(ServerLevel level, ChunkPos pos) {
        return new net.minecraft.world.phys.AABB(pos.getMinBlockX(), level.getMinBuildHeight(), pos.getMinBlockZ(),
                pos.getMaxBlockX() + 1, level.getMaxBuildHeight(), pos.getMaxBlockZ() + 1);
    }

    private static net.minecraft.world.phys.AABB nearbyBounds(ServerLevel level, ChunkPos center) {
        int minX = (center.x - SPAWN_RADIUS_CHUNKS) << 4;
        int minZ = (center.z - SPAWN_RADIUS_CHUNKS) << 4;
        int maxX = ((center.x + SPAWN_RADIUS_CHUNKS + 1) << 4);
        int maxZ = ((center.z + SPAWN_RADIUS_CHUNKS + 1) << 4);
        return new net.minecraft.world.phys.AABB(minX, level.getMinBuildHeight(), minZ,
                maxX, level.getMaxBuildHeight(), maxZ);
    }

    private static BlockPos findSpawnPosition(ServerLevel level, ChunkPos chunkPos) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = chunkPos.getMinBlockX() + ThreadLocalRandom.current().nextInt(16);
            int z = chunkPos.getMinBlockZ() + ThreadLocalRandom.current().nextInt(16);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos.below()).isSolid() && level.getBlockState(pos).isAir()) return pos;
        }
        return null;
    }
}
