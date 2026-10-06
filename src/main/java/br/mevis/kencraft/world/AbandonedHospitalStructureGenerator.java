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
    private static final int HEIGHT = 11;
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
    }

    private static void build(ServerLevel level, BlockPos o) {
        // Fundação com blocos degradados
        for (int x = 0; x < WIDTH; x++) for (int z = 0; z < DEPTH; z++) {
            set(level, o.offset(x, 0, z), Blocks.CRACKED_STONE_BRICKS);
            set(level, o.offset(x, 1, z), Blocks.POLISHED_ANDESITE);
        }

        // Hospital de dois pavimentos com estrutura mais degradada
        for (int y = 2; y <= 10; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, o.offset(x, y, 0), Blocks.LIGHT_GRAY_CONCRETE);
                set(level, o.offset(x, y, DEPTH - 1), Blocks.LIGHT_GRAY_CONCRETE);
            }
            for (int z = 1; z < DEPTH - 1; z++) {
                set(level, o.offset(0, y, z), Blocks.LIGHT_GRAY_CONCRETE);
                set(level, o.offset(WIDTH - 1, y, z), Blocks.LIGHT_GRAY_CONCRETE);
            }
        }

        // Laje do segundo pavimento com detalhes
        for (int x = 1; x < WIDTH - 1; x++)
            for (int z = 1; z < DEPTH - 1; z++)
                set(level, o.offset(x, 5, z), (x + z) % 4 == 0 ? Blocks.MOSSY_STONE_BRICKS : Blocks.LIGHT_GRAY_CONCRETE);

        // Corredor central do térreo com padrão hospitalar
        for (int x = 4; x <= 26; x++)
            for (int z = 11; z <= 13; z++)
                set(level, o.offset(x, 2, z), (x + z) % 3 == 0 ? Blocks.DARK_GRAY_CONCRETE : Blocks.LIGHT_GRAY_CONCRETE);

        // Recepção e salas médicas com estrutura mais hospitalaren
        for (int x = 13; x <= 17; x++) set(level, o.offset(x, 2, 3), Blocks.QUARTZ_BLOCK);

        // Quartos com mobiliário médico
        bed(level, o, 5, 5, true);
        bed(level, o, 22, 5, true);
        bed(level, o, 5, 17, true);
        bed(level, o, 22, 17, true);

        // Estações médicas
        for (int x : new int[]{8, 19}) {
            set(level, o.offset(x, 2, 4), Blocks.CHEST);
            set(level, o.offset(x, 2, 5), Blocks.COMPOSTER); // Recipiente médico
            set(level, o.offset(x, 2, 18), Blocks.BARREL);
        }

        // Abertura da escada com mais realismo
        for (int y = 2; y <= 4; y++)
            for (int x = 14; x <= 16; x++)
                set(level, o.offset(x, y, 14), Blocks.AIR);
        for (int i = 0; i < 4; i++)
            set(level, o.offset(12 + i, 2 + i, 14), Blocks.OAK_STAIRS);

        // Segundo pavimento com corredor médico
        for (int x = 4; x <= 26; x++)
            for (int z = 11; z <= 13; z++)
                set(level, o.offset(x, 6, z), (x + z) % 2 == 0 ? Blocks.LIGHT_GRAY_CONCRETE : Blocks.POLISHED_ANDESITE);

        // Janelas amplas com detalhes de abandono
        for (int x = 3; x <= 27; x += 4) {
            for (int y = 2; y <= 4; y++) {
                set(level, o.offset(x, y, 0), Blocks.GLASS_PANE);
                set(level, o.offset(x, y, DEPTH - 1), Blocks.GLASS_PANE);
            }
            for (int y = 6; y <= 9; y++) {
                set(level, o.offset(x, y, 0), Blocks.GLASS_PANE);
                set(level, o.offset(x, y, DEPTH - 1), Blocks.GLASS_PANE);
            }
        }
        for (int z = 4; z <= 20; z += 4) {
            for (int y = 2; y <= 4; y++) {
                set(level, o.offset(0, y, z), Blocks.GLASS_PANE);
                set(level, o.offset(WIDTH - 1, y, z), Blocks.GLASS_PANE);
            }
            for (int y = 6; y <= 9; y++) {
                set(level, o.offset(0, y, z), Blocks.GLASS_PANE);
                set(level, o.offset(WIDTH - 1, y, z), Blocks.GLASS_PANE);
            }
        }

        // Entrada principal hospitalaren
        for (int y = 2; y <= 4; y++)
            for (int x = 12; x <= 18; x++)
                set(level, o.offset(x, y, 0), Blocks.AIR);
        for (int x = 11; x <= 19; x++)
            set(level, o.offset(x, 5, 0), Blocks.IRON_BARS);

        // Cobertura da entrada com estrutura de ferro
        for (int x = 10; x <= 20; x++) {
            set(level, o.offset(x, 2, -1), Blocks.IRON_BLOCK);
            set(level, o.offset(x, 3, -1), Blocks.IRON_BLOCK);
        }

        // Símbolo hospitalar grande e visível - Cruz vermelha
        for (int y = 7; y <= 9; y++) set(level, o.offset(15, y, 0), Blocks.RED_CONCRETE);
        for (int x = 13; x <= 17; x++) set(level, o.offset(x, 8, 0), Blocks.RED_CONCRETE);
        
        // Detalhes adicionais da cruz
        set(level, o.offset(15, 7, 0), Blocks.RED_CONCRETE_POWDER);
        set(level, o.offset(15, 9, 0), Blocks.RED_CONCRETE_POWDER);
        set(level, o.offset(13, 8, 0), Blocks.RED_CONCRETE_POWDER);
        set(level, o.offset(17, 8, 0), Blocks.RED_CONCRETE_POWDER);

        // Telhado degradado com sinais de abandono
        for (int x = 0; x < WIDTH; x++)
            for (int z = 0; z < DEPTH; z++)
                set(level, o.offset(x, 10, z), Blocks.DEEPSLATE_TILES);
        
        // Telhado danificado
        for (int x = 2; x < WIDTH - 2; x += 3)
            for (int z = 2; z < DEPTH - 2; z += 3)
                set(level, o.offset(x, 10, z), Blocks.AIR);

        // Sinais de abandono e degradação
        for (int x : new int[]{3, 27})
            for (int z : new int[]{3, 21})
                set(level, o.offset(x, 2, z), Blocks.COBWEB);

        // Cômodo de isolamento com materiais específicos
        for (int x = 27; x <= 30; x++) {
            for (int y = 2; y <= 5; y++) {
                set(level, o.offset(x, y, 5), Blocks.PURPUR_BLOCK);
            }
        }
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
        boss.setCustomName(net.minecraft.network.chat.Component.literal("§cRinka Faminta - Hospital Abandonado"));
        boss.setCustomNameVisible(true);
        level.addFreshEntity(boss);
    }

    private static void set(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.Block block) {
        level.setBlock(pos, block.defaultBlockState(), 3);
    }

    private record PendingGeneration(ServerLevel level,int x,int z) {}
}
