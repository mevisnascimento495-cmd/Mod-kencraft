package br.mevis.kencraft.world;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaHungryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Deque;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class AbandonedHospitalStructureGenerator {
    private static final int CHANCE_DENOMINATOR = 128;
    private static final int WIDTH = 31;
    private static final int DEPTH = 25;
    private static final int HEIGHT = 15;
    private static final int MAX_TERRAIN_VARIATION = 6;
    private static final int MAX_PENDING = 8;
    private static final Deque<PendingGeneration> PENDING = new ArrayDeque<>();
    private static int cooldown;

    public static int CHANCE_DENOMINATOR() { return CHANCE_DENOMINATOR; }
    private AbandonedHospitalStructureGenerator() {}

    @SubscribeEvent
    public static void onNewChunk(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) return;
        ChunkAccess chunk = event.getChunk();
        if (Math.floorMod(hashForChunk(level.getSeed(), chunk.getPos().x, chunk.getPos().z), CHANCE_DENOMINATOR) != 0) return;
        if (PENDING.size() < MAX_PENDING) PENDING.addLast(new PendingGeneration(level, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ()));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (cooldown > 0) { cooldown--; return; }
        if (PENDING.isEmpty() || event.getServer().getTickCount() % 20 != 0) return;
        PendingGeneration p = PENDING.pollFirst();
        if (p == null || p.level().getServer() != event.getServer()) return;
        if (!footprintLoaded(p.level(), p.x(), p.z())) { PENDING.addLast(p); return; }
        if (generateAt(p.level(), p.x(), p.z())) cooldown = 20;
    }

    public static long hashForChunk(long seed, int chunkX, int chunkZ) {
        long v = seed ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L);
        v ^= v >>> 33; v *= 0xff51afd7ed558ccdL; v ^= v >>> 33; v *= 0xc4ceb9fe1a85ec53L; v ^= v >>> 33;
        return v;
    }

    public static boolean generateAt(ServerLevel level, int centerX, int centerZ) {
        if (!footprintLoaded(level, centerX, centerZ)) return false;
        if (!StructurePlacementRules.isFarEnough(level, centerX, centerZ)) return false;
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        if (groundY < level.getMinBuildHeight() || groundY > level.getMaxBuildHeight() - HEIGHT - 3) return false;

        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        for (int x = -15; x <= 15; x++) {
            for (int z = -12; z <= 12; z++) {
                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX + x, centerZ + z) - 1;
                minSurface = Math.min(minSurface, surface);
                maxSurface = Math.max(maxSurface, surface);
            }
        }
        if (maxSurface - minSurface > MAX_TERRAIN_VARIATION) return false;

        BlockPos marker = new BlockPos(centerX, groundY, centerZ);
        if (level.getBlockState(marker).is(Blocks.LODESTONE)) return true;

        BlockPos o = new BlockPos(centerX - 15, groundY, centerZ - 12);
        prepareGround(level, o, groundY);
        build(level, o);
        StructureDecoration.addEntrance(level, o, WIDTH);
        level.setBlock(marker, Blocks.LODESTONE.defaultBlockState(), 3);
        spawnBoss(level, new BlockPos(centerX, groundY + 2, centerZ + 4));
        MerchantStructureSpawner.trySpawn(level, centerX, centerZ, groundY, WIDTH, DEPTH);
        StructureLoot.hospital(level, o.offset(8, 2, 4), o.offset(19, 2, 4), o.offset(8, 2, 18), o.offset(19, 2, 18));
        return true;
    }

    private static boolean footprintLoaded(ServerLevel level, int x, int z) {
        int minX = Math.floorDiv(x - 10, 16), maxX = Math.floorDiv(x + 10, 16);
        int minZ = Math.floorDiv(z - 10, 16), maxZ = Math.floorDiv(z + 10, 16);
        for (int cx=minX; cx<=maxX; cx++) for (int cz=minZ; cz<=maxZ; cz++)
            if (level.getChunkSource().getChunkNow(cx, cz) == null) return false;
        return true;
    }

    private static boolean validGround(ServerLevel level, int x, int y, int z) {
        var state = level.getBlockState(new BlockPos(x, y, z));
        return state.isSolid() && !state.is(Blocks.WATER) && !state.is(Blocks.LAVA);
    }

    private static void prepareGround(ServerLevel level, BlockPos o, int baseY) {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                int worldX = o.getX() + x;
                int worldZ = o.getZ() + z;
                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ) - 1;
                if (surface < baseY) {
                    for (int y = surface + 1; y <= baseY; y++)
                        set(level, new BlockPos(worldX, y, worldZ), Blocks.STONE_BRICKS);
                } else if (surface > baseY) {
                    for (int y = baseY + 1; y <= surface + 1; y++)
                        set(level, new BlockPos(worldX, y, worldZ), Blocks.AIR);
                }
            }
        }



        // REWORK HOSPITAL: segundo piso, observação e ala de isolamento
        for (int y = 11; y <= 14; y++) {
            for (int x = 4; x <= 26; x++) {
                set(level, o.offset(x, y, 4), Blocks.MOSSY_STONE_BRICKS);
                set(level, o.offset(x, y, 20), Blocks.MOSSY_STONE_BRICKS);
            }
            for (int z = 4; z <= 20; z++) {
                set(level, o.offset(4, y, z), Blocks.MOSSY_STONE_BRICKS);
                set(level, o.offset(26, y, z), Blocks.MOSSY_STONE_BRICKS);
            }
        }
        for (int x = 5; x <= 25; x++) for (int z = 5; z <= 19; z++)
            if ((x + z) % 3 == 0) set(level, o.offset(x, 11, z), Blocks.POLISHED_ANDESITE);
        for (int x = 6; x <= 24; x += 3) {
            for (int y = 12; y <= 13; y++) {
                set(level, o.offset(x, y, 4), Blocks.GLASS_PANE);
                set(level, o.offset(x, y, 20), Blocks.GLASS_PANE);
            }
        }
        for (int z = 6; z <= 18; z += 3) {
            for (int y = 12; y <= 13; y++) {
                set(level, o.offset(4, y, z), Blocks.GLASS_PANE);
                set(level, o.offset(26, y, z), Blocks.GLASS_PANE);
            }
        }
        for (int x = 9; x <= 21; x++) {
            set(level, o.offset(x, 12, 12), Blocks.IRON_BARS);
            set(level, o.offset(x, 13, 12), Blocks.IRON_BARS);
        }
        for (int x = 11; x <= 19; x++) {
            set(level, o.offset(x, 12, 11), Blocks.RED_CONCRETE);
            set(level, o.offset(x, 13, 11), Blocks.WHITE_CONCRETE);
        }
        for (int x = 6; x <= 24; x++) {
            set(level, o.offset(x, 14, 4), Blocks.DEEPSLATE_TILES);
            set(level, o.offset(x, 14, 20), Blocks.DEEPSLATE_TILES);
        }
        for (int z = 5; z <= 19; z++) {
            set(level, o.offset(4, 14, z), Blocks.DEEPSLATE_TILES);
            set(level, o.offset(26, 14, z), Blocks.DEEPSLATE_TILES);
        }
        // Anexo de isolamento visível pela fachada lateral.
        for (int x = 27; x <= 29; x++) for (int y = 2; y <= 6; y++)
            set(level, o.offset(x, y, 7), Blocks.IRON_BARS);
        for (int x = 27; x <= 29; x++) {
            set(level, o.offset(x, 1, 6), Blocks.POLISHED_ANDESITE);
            set(level, o.offset(x, 1, 8), Blocks.POLISHED_ANDESITE);
        }
        set(level, o.offset(28, 2, 7), Blocks.REDSTONE_LAMP);
        set(level, o.offset(28, 3, 7), Blocks.RED_CONCRETE);

    }

    private static void bed(ServerLevel level, BlockPos o, int x, int z, boolean hasIV) {
        set(level, o.offset(x, 1, z), Blocks.RED_BED);
        set(level, o.offset(x + 1, 1, z), Blocks.RED_BED);
        set(level, o.offset(x, 1, z + 1), Blocks.BARREL);
        set(level, o.offset(x + 1, 1, z + 1), Blocks.BARREL);

        if (hasIV) {
            set(level, o.offset(x - 1, 2, z), Blocks.CHAIN);
            set(level, o.offset(x - 1, 3, z), Blocks.LANTERN);
        }
    }

    private static void spawnBoss(ServerLevel level, BlockPos pos) {
        if (!level.getEntitiesOfClass(RinkaHungryEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(4.0D), Entity::isAlive).isEmpty()) return;
        RinkaHungryEntity boss = KenCraftEntities.RINKA_HUNGRY.get().create(level);
        if (boss == null) return;
        boss.moveTo(pos.getX()+0.5D,pos.getY(),pos.getZ()+0.5D,180.0F,0.0F);
        boss.setCustomName(net.minecraft.network.chat.Component.literal("Rinka Faminta - Hospital Abandonado"));
        boss.setCustomNameVisible(true);
        level.addFreshEntity(boss);
    }

    private static void set(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.Block block) {
        level.setBlock(pos, block.defaultBlockState(), 3);
    }

    private record PendingGeneration(ServerLevel level,int x,int z) {}
}
