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
import net.minecraft.world.level.levelgen.Heightmap;
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
    private static final double ARF_CHANCE = 0.12D;
    private static final double GENERAL_CHANCE = 0.025D;

    private static final int MAX_NIGHT_RINKA = 3;
    private static final int MAX_RANK_C = 1;
    private static final int MAX_RISHIN = 2;
    private static final int MAX_AODAI = 1;
    private static final int MAX_ARF = 3;
    private static final int MAX_GENERAL = 1;

    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final double ENTITY_CHECK_RADIUS = 64.0D;
    private static final double ENTITY_CHECK_VERTICAL = 32.0D;

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

        if (roll < AODAI_CHANCE) {
            AABB nearby = nearbyBounds(player);
            if (count(level, nearby, AodaiEntity.class) < MAX_AODAI) {
                spawn(level, cp, KenCraftEntities.AODAI.get());
                return;
            }
        }

        if (isNight(level)) {
            if (roll < RANK_C_CHANCE) {
                AABB nearby = nearbyBounds(player);
                int r = count(level, nearby, RinkaEntity.class);
                int c = count(level, nearby, RankCRinkaEntity.class);
                if (c < MAX_RANK_C && r + c < MAX_NIGHT_RINKA) {
                    spawn(level, cp, KenCraftEntities.RANK_C_RINKA.get());
                    return;
                }
            }

            if (roll < RANK_C_CHANCE + RISHIN_CHANCE) {
                AABB nearby = nearbyBounds(player);
                if (count(level, nearby, RishinEntity.class) < MAX_RISHIN) {
                    spawn(level, cp, KenCraftEntities.RISHIN.get());
                    return;
                }
            }

            if (roll < RANK_C_CHANCE + RISHIN_CHANCE + RINKA_CHANCE) {
                AABB nearby = nearbyBounds(player);
                int r = count(level, nearby, RinkaEntity.class);
                int c = count(level, nearby, RankCRinkaEntity.class);
                if (r + c < MAX_NIGHT_RINKA) {
                    spawn(level, cp, KenCraftEntities.RINKA.get());
                }
            }
            return;
        }

        if (roll < GENERAL_CHANCE) {
            AABB nearby = nearbyBounds(player);
            if (count(level, nearby, ArfGeneralEntity.class) < MAX_GENERAL) {
                spawn(level, cp, KenCraftEntities.ARF_GENERAL.get());
                return;
            }
        }

        if (roll < GENERAL_CHANCE + ARF_CHANCE) {
            AABB nearby = nearbyBounds(player);
            if (count(level, nearby, ArfInvestigatorEntity.class) < MAX_ARF) {
                spawn(level, cp, KenCraftEntities.ARF_INVESTIGATOR.get());
            }
        }
    }

    private static boolean isNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time >= 13000L && time < 23000L;
    }

    private static <T extends Entity> int count(ServerLevel level, AABB bounds, Class<T> type) {
        return level.getEntitiesOfClass(type, bounds, e -> true).size();
    }

    private static AABB nearbyBounds(ServerPlayer player) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        return new AABB(
                x - ENTITY_CHECK_RADIUS, y - ENTITY_CHECK_VERTICAL, z - ENTITY_CHECK_RADIUS,
                x + ENTITY_CHECK_RADIUS, y + ENTITY_CHECK_VERTICAL, z + ENTITY_CHECK_RADIUS
        );
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

    private static BlockPos find(ServerLevel level, ChunkPos chunk) {
        for (int i = 0; i < 4; i++) {
            int x = chunk.getMinBlockX() + ThreadLocalRandom.current().nextInt(16);
            int z = chunk.getMinBlockZ() + ThreadLocalRandom.current().nextInt(16);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos.below()).isSolid() && level.getBlockState(pos).isAir()) return pos;
        }
        return null;
    }
}
