package br.mevis.kencraft.world;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Deque;

/** Naturally generates the large underground Rishin coven and its visible surface hatch. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class RishinCovenantStructureGenerator {
    private static final int CHANCE_DENOMINATOR = 112;
    private static final int SIZE = 45;
    private static final int HALF = SIZE / 2;
    private static final Deque<Pending> PENDING = new ArrayDeque<>();
    private static int cooldown;

    private RishinCovenantStructureGenerator() {}

    @SubscribeEvent
    public static void onNewChunk(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD) return;
        ChunkAccess chunk = event.getChunk();
        if (Math.floorMod(hashForChunk(level.getSeed(), chunk.getPos().x, chunk.getPos().z),
                CHANCE_DENOMINATOR) != 0) return;
        Pending pending = new Pending(level, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ());
        if (!PENDING.contains(pending) && PENDING.size() < 8) PENDING.addLast(pending);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (cooldown > 0) { cooldown--; return; }
        if (PENDING.isEmpty() || event.getServer().getTickCount() % 20 != 0) return;
        Pending pending = PENDING.pollFirst();
        if (pending == null || pending.level().getServer() != event.getServer()) return;
        if (!footprintLoaded(pending.level(), pending.x(), pending.z())) { PENDING.addLast(pending); return; }
        if (generateAt(pending.level(), pending.x(), pending.z())) cooldown = 40;
    }

    public static long hashForChunk(long seed, int chunkX, int chunkZ) {
        long value = seed ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L);
        value ^= value >>> 33; value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33; value *= 0xc4ceb9fe1a85ec53L;
        return value ^ (value >>> 33);
    }

    public static int chanceDenominator() { return CHANCE_DENOMINATOR; }

    public static boolean generateAt(ServerLevel level, int centerX, int centerZ) {
        if (level.dimension() != Level.OVERWORLD || !footprintLoaded(level, centerX, centerZ)) return false;
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        int floorY = surfaceY - 20;
        if (floorY < level.getMinBuildHeight() + 8 || surfaceY > level.getMaxBuildHeight() - 8) return false;

        BlockPos marker = new BlockPos(centerX + 3, surfaceY - 1, centerZ);
        if (level.getBlockState(marker).is(Blocks.LODESTONE)) return true;
        if (!StructurePlacementRules.isFarEnough(level, centerX, centerZ)) return false;

        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int dx = -HALF; dx <= HALF; dx += 11) {
            for (int dz = -HALF; dz <= HALF; dz += 11) {
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX + dx, centerZ + dz) - 1;
                minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                BlockState ground = level.getBlockState(new BlockPos(centerX + dx, y, centerZ + dz));
                if (!ground.isSolid() || ground.is(Blocks.WATER) || ground.is(Blocks.LAVA)) return false;
            }
        }
        if (maxY - minY > 3) return false;

        build(level, centerX, surfaceY, floorY, centerZ);
        level.setBlock(marker, Blocks.LODESTONE.defaultBlockState(), 3);
        return true;
    }

    private static boolean footprintLoaded(ServerLevel level, int x, int z) {
        for (int cx = Math.floorDiv(x - HALF, 16); cx <= Math.floorDiv(x + HALF, 16); cx++) {
            for (int cz = Math.floorDiv(z - HALF, 16); cz <= Math.floorDiv(z + HALF, 16); cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) return false;
            }
        }
        return true;
    }

    private static void build(ServerLevel level, int centerX, int surfaceY, int floorY, int centerZ) {
        BlockPos origin = new BlockPos(centerX - HALF, floorY, centerZ - HALF);
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                set(level, origin.offset(x, 0, z), Blocks.DEEPSLATE_BRICKS);
                set(level, origin.offset(x, 5, z), Blocks.DEEPSLATE_TILES);
                for (int y = 1; y <= 4; y++) {
                    boolean perimeter = x == 0 || x == SIZE - 1 || z == 0 || z == SIZE - 1;
                    set(level, origin.offset(x, y, z), perimeter ? Blocks.DEEPSLATE_BRICKS : Blocks.AIR);
                }
            }
        }

        // Internal walls leave branching corridors and several side chambers.
        wallX(level, origin, 10, 1, 43, new int[][]{{7,11},{31,35}});
        wallX(level, origin, 34, 1, 43, new int[][]{{21,25},{36,40}});
        wallZ(level, origin, 10, 1, 9, new int[][]{{4,6}});
        wallZ(level, origin, 34, 1, 9, new int[][]{{4,6}});
        wallZ(level, origin, 34, 11, 33, new int[][]{{31,33}});
        wallZ(level, origin, 10, 35, 43, new int[][]{{38,40}});

        // The corner guard room has a single narrow doorway into the tunnels.
        for (int x = 35; x <= 43; x++) {
            for (int y = 1; y <= 4; y++) set(level, origin.offset(x, y, 34), Blocks.DEEPSLATE_BRICKS);
        }
        for (int x = 36; x <= 40; x++) for (int y = 1; y <= 4; y++) set(level, origin.offset(x, y, 34), Blocks.AIR);
        for (int z = 35; z <= 43; z++) {
            for (int y = 1; y <= 4; y++) set(level, origin.offset(34, y, z), Blocks.DEEPSLATE_BRICKS);
        }
        for (int z = 36; z <= 40; z++) for (int y = 1; y <= 4; y++) set(level, origin.offset(34, y, z), Blocks.AIR);

        // Embedded lights keep the maze navigable without exposing it from the surface.
        for (int x : new int[]{5, 16, 22, 28, 39}) {
            for (int z : new int[]{5, 16, 22, 28, 39}) {
                set(level, origin.offset(x, 4, z), Blocks.SHROOMLIGHT);
            }
        }

        // The shaft is next to a crimson hatch so it has solid ground support.
        for (int y = floorY + 1; y < surfaceY; y++) {
            set(level, new BlockPos(centerX, y, centerZ), Blocks.AIR);
            set(level, new BlockPos(centerX, y, centerZ - 1), Blocks.DEEPSLATE_BRICKS);
            set(level, new BlockPos(centerX, y, centerZ + 1), Blocks.DEEPSLATE_BRICKS);
            set(level, new BlockPos(centerX, y, centerZ + 2), Blocks.DEEPSLATE_BRICKS);
            set(level, new BlockPos(centerX, y, centerZ), Blocks.LADDER.defaultBlockState()
                    .setValue(LadderBlock.FACING, Direction.NORTH));
        }
        set(level, new BlockPos(centerX, surfaceY, centerZ), Blocks.AIR);
        set(level, new BlockPos(centerX, surfaceY, centerZ + 1),
                Blocks.CRIMSON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH)
                        .setValue(TrapDoorBlock.OPEN, false));

        spawnRishin(level, centerX - 3, floorY + 1, centerZ);
        spawnRishin(level, centerX + 3, floorY + 1, centerZ - 2);
        spawnRishin(level, centerX, floorY + 1, centerZ + 3);

        Entity kenki = KenCraftEntities.KENKI_SENKO.get().create(level);
        if (kenki != null) {
            kenki.moveTo(centerX + 17.5D, floorY + 1, centerZ + 17.5D, 180.0F, 0.0F);
            kenki.setCustomName(net.minecraft.network.chat.Component.literal("Kenki Senkō"));
            kenki.setCustomNameVisible(true);
            kenki.getPersistentData().putBoolean("kencraft_rishin_coven_kenki", true);
            if (level.noCollision(kenki)) level.addFreshEntity(kenki);
        }
    }

    private static void spawnRishin(ServerLevel level, int x, int y, int z) {
        Entity mob = KenCraftEntities.RISHIN.get().create(level);
        if (mob == null) return;
        mob.moveTo(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
        mob.setCustomName(net.minecraft.network.chat.Component.literal("Rishin"));
        mob.setCustomNameVisible(true);
        mob.getPersistentData().putBoolean("kencraft_rishin_coven_central", true);
        if (level.noCollision(mob)) level.addFreshEntity(mob);
    }

    private static void wallX(ServerLevel level, BlockPos origin, int x, int zMin, int zMax, int[][] doorways) {
        for (int z = zMin; z <= zMax; z++) {
            boolean doorway = false;
            for (int[] range : doorways) if (z >= range[0] && z <= range[1]) doorway = true;
            if (doorway) continue;
            for (int y = 1; y <= 4; y++) set(level, origin.offset(x, y, z), Blocks.DEEPSLATE_BRICKS);
        }
    }

    private static void wallZ(ServerLevel level, BlockPos origin, int z, int xMin, int xMax, int[][] doorways) {
        for (int x = xMin; x <= xMax; x++) {
            boolean doorway = false;
            for (int[] range : doorways) if (x >= range[0] && x <= range[1]) doorway = true;
            if (doorway) continue;
            for (int y = 1; y <= 4; y++) set(level, origin.offset(x, y, z), Blocks.DEEPSLATE_BRICKS);
        }
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) {
        level.setBlock(pos, block.defaultBlockState(), 3);
    }

    private record Pending(ServerLevel level, int x, int z) {}
}
