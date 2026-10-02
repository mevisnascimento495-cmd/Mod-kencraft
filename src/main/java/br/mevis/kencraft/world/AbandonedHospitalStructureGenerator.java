package br.mevis.kencraft.world;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.RinkaHungryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
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
    private static final int WIDTH = 21;
    private static final int DEPTH = 21;
    private static final int HEIGHT = 7;
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
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        if (groundY < level.getMinBuildHeight() + 3 || groundY > level.getMaxBuildHeight() - HEIGHT - 2) return false;
        BlockPos marker = new BlockPos(centerX, groundY, centerZ);
        if (level.getBlockState(marker).is(Blocks.LODESTONE)) return true;
        if (!validGround(level, centerX, groundY, centerZ)) return false;

        BlockPos o = new BlockPos(centerX - 10, groundY + 1, centerZ - 10);
        build(level, o);
        level.setBlock(marker, Blocks.LODESTONE.defaultBlockState(), 3);
        spawnBoss(level, o.offset(10, 1, 10));
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

    private static void build(ServerLevel level, BlockPos o) {
        for (int x=0;x<WIDTH;x++) for (int z=0;z<DEPTH;z++) set(level,o.offset(x,0,z),Blocks.CRACKED_STONE_BRICKS);
        for (int y=1;y<=5;y++) {
            for (int x=0;x<WIDTH;x++) { set(level,o.offset(x,y,0),Blocks.STONE_BRICKS); set(level,o.offset(x,y,DEPTH-1),Blocks.STONE_BRICKS); }
            for (int z=0;z<DEPTH;z++) { set(level,o.offset(0,y,z),Blocks.STONE_BRICKS); set(level,o.offset(WIDTH-1,y,z),Blocks.STONE_BRICKS); }
        }
        for (int x=2;x<19;x++) for (int z=2;z<19;z++) set(level,o.offset(x,1,z),Blocks.POLISHED_ANDESITE);
        for (int x=0;x<WIDTH;x++) set(level,o.offset(x,6,0),Blocks.DARK_OAK_PLANKS);
        for (int x=0;x<WIDTH;x++) for (int z=0;z<DEPTH;z++) if ((x+z)%5==0) set(level,o.offset(x,5,z),Blocks.COBWEB);

        // Entrada destruída.
        for (int y=1;y<=3;y++) for (int x=8;x<=12;x++) set(level,o.offset(x,y,0),Blocks.AIR);
        for (int x=7;x<=13;x++) set(level,o.offset(x,4,0),Blocks.IRON_BARS);

        // Corredor e salas.
        for (int x=3;x<=17;x++) { set(level,o.offset(x,2,10),Blocks.CRACKED_STONE_BRICKS); set(level,o.offset(x,2,11),Blocks.CRACKED_STONE_BRICKS); }
        for (int z=3;z<=17;z+=7) for (int x=3;x<=17;x++) set(level,o.offset(x,2,z),Blocks.CRACKED_STONE_BRICKS);

        bed(level,o,4,4); bed(level,o,14,4); bed(level,o,4,15); bed(level,o,14,15);
        set(level,o.offset(6,1,5),Blocks.CHEST); set(level,o.offset(13,1,5),Blocks.CHEST);
        set(level,o.offset(6,1,16),Blocks.CHEST); set(level,o.offset(13,1,16),Blocks.CHEST);
        for (int x : new int[]{3,17}) for (int z : new int[]{3,17}) set(level,o.offset(x,1,z),Blocks.IRON_BARS);
        for (int x=2;x<=18;x+=4) set(level,o.offset(x,1,2),Blocks.REDSTONE_TORCH);
        set(level,o.offset(10,1,10),Blocks.BLACK_CONCRETE);
        set(level,o.offset(10,2,10),Blocks.IRON_BLOCK);
    }

    private static void bed(ServerLevel level, BlockPos o, int x, int z) {
        set(level,o.offset(x,1,z),Blocks.RED_BED);
        set(level,o.offset(x+1,1,z),Blocks.RED_BED);
        set(level,o.offset(x,1,z+1),Blocks.BARREL);
        set(level,o.offset(x+1,1,z+1),Blocks.BARREL);
    }

    private static void spawnBoss(ServerLevel level, BlockPos pos) {
        if (!level.getEntitiesOfClass(RinkaHungryEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(4.0D), Entity::isAlive).isEmpty()) return;
        RinkaHungryEntity boss = KenCraftEntities.RINKA_HUNGRY.get().create(level);
        if (boss == null) return;
        boss.moveTo(pos.getX()+0.5D,pos.getY(),pos.getZ()+0.5D,180.0F,0.0F);
        boss.setCustomName(net.minecraft.network.chat.Component.literal("Rinka Hungry"));
        boss.setCustomNameVisible(true);
        level.addFreshEntity(boss);
    }

    private static void set(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.Block block) {
        level.setBlock(pos, block.defaultBlockState(), 3);
    }

    private record PendingGeneration(ServerLevel level,int x,int z) {}
}
