package br.mevis.kencraft.world;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

/** Two-storey courthouse where Akira waits in the defendant's central hearing area. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class JudicialStructureGenerator {
    private static final int CHANCE_DENOMINATOR = 192;
    private static final int WIDTH = 27, DEPTH = 23, HEIGHT = 14, MAX_PENDING = 6;
    private static final Deque<Pending> PENDING = new ArrayDeque<>();
    private static int cooldown;
    public static int CHANCE_DENOMINATOR() { return CHANCE_DENOMINATOR; }
    private JudicialStructureGenerator() {}

    @SubscribeEvent public static void onNewChunk(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) return;
        ChunkAccess chunk = event.getChunk();
        if (Math.floorMod(hashForChunk(level.getSeed(), chunk.getPos().x, chunk.getPos().z), CHANCE_DENOMINATOR) != 0) return;
        Pending p = new Pending(level, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ());
        if (!PENDING.contains(p) && PENDING.size() < MAX_PENDING) PENDING.addLast(p);
    }

    @SubscribeEvent public static void onServerTick(ServerTickEvent.Post event) {
        if (cooldown > 0) { cooldown--; return; }
        if (PENDING.isEmpty() || event.getServer().getTickCount() % 20 != 0) return;
        Pending p = PENDING.pollFirst();
        if (p == null || p.level().getServer() != event.getServer()) return;
        for (ServerPlayer player : p.level().players()) {
            if (player.isSprinting() && player.distanceToSqr(p.x(), player.getY(), p.z()) < 160.0D * 160.0D) {
                PENDING.addLast(p); return;
            }
        }
        if (!footprintLoaded(p.level(), p.x(), p.z())) { PENDING.addLast(p); return; }
        if (generateAt(p.level(), p.x(), p.z())) cooldown = 20;
    }

    public static long hashForChunk(long seed, int chunkX, int chunkZ) {
        long v = seed ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L);
        v ^= v >>> 33; v *= 0xff51afd7ed558ccdL; v ^= v >>> 33; v *= 0xc4ceb9fe1a85ec53L; return v ^ (v >>> 33);
    }

    public static boolean generateAt(ServerLevel level, int centerX, int centerZ) {
        if (!footprintLoaded(level, centerX, centerZ) || !StructurePlacementRules.isFarEnough(level, centerX, centerZ)) return false;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        if (y < level.getMinBuildHeight() + 3 || y > level.getMaxBuildHeight() - HEIGHT - 2) return false;
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int x = -13; x <= 13; x += 6) for (int z = -11; z <= 11; z += 5) {
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX+x, centerZ+z)-1;
            min = Math.min(min, surface); max = Math.max(max, surface);
        }
        if (max-min > 3) return false;
        if (level.getBlockState(new BlockPos(centerX, y, centerZ)).is(Blocks.LODESTONE)) return true;
        BlockPos origin = new BlockPos(centerX-13, y+1, centerZ-11);
        for (int x : new int[]{-13,0,13}) for (int z : new int[]{-11,0,11}) {
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX+x, centerZ+z)-1;
            BlockState ground = level.getBlockState(new BlockPos(centerX+x,surface,centerZ+z));
            if (Math.abs(surface-y)>2 || ground.is(Blocks.WATER) || ground.is(Blocks.LAVA) || !ground.isSolid()) return false;
        }
        build(level, origin);
        Entity akira = KenCraftEntities.RINKA_RANK_B_AKIRA.get().create(level);
        if (akira != null) {
            akira.moveTo(centerX+0.5D, y+3, centerZ+2.5D, 180.0F, 0.0F);
            level.addFreshEntity(akira);
        }
        return true;
    }

    private static boolean footprintLoaded(ServerLevel level, int x, int z) {
        for (int cx=Math.floorDiv(x-13,16); cx<=Math.floorDiv(x+13,16); cx++)
            for (int cz=Math.floorDiv(z-11,16); cz<=Math.floorDiv(z+11,16); cz++) {
                LevelChunk chunk=level.getChunkSource().getChunkNow(cx,cz);
                if (chunk==null) return false;
            }
        return true;
    }

    private static void build(ServerLevel level, BlockPos o) {
        // Foundation and lower courtroom floor.
        for(int x=0;x<WIDTH;x++) for(int z=0;z<DEPTH;z++) {
            set(level,o.offset(x,0,z),Blocks.DEEPSLATE_BRICKS);
            set(level,o.offset(x,1,z),(x==0||x==WIDTH-1||z==0||z==DEPTH-1)?Blocks.POLISHED_ANDESITE:Blocks.SMOOTH_STONE);
        }
        // Ground-floor walls and formal front facade.
        for(int y=2;y<=5;y++) {
            for(int x=0;x<WIDTH;x++){set(level,o.offset(x,y,0),Blocks.POLISHED_ANDESITE);set(level,o.offset(x,y,DEPTH-1),Blocks.POLISHED_ANDESITE);}
            for(int z=0;z<DEPTH;z++){set(level,o.offset(0,y,z),Blocks.POLISHED_ANDESITE);set(level,o.offset(WIDTH-1,y,z),Blocks.POLISHED_ANDESITE);}
        }
        for(int x=2;x<=24;x++) for(int y=3;y<=4;y++) set(level,o.offset(x,y,0),Blocks.BLACK_STAINED_GLASS);
        for(int x=11;x<=15;x++) for(int y=2;y<=4;y++) set(level,o.offset(x,y,0),Blocks.AIR);
        for(int x : new int[]{2,6,20,24}) for(int y=2;y<=5;y++) set(level,o.offset(x,y,0),Blocks.QUARTZ_PILLAR);
        // Two-storey courtroom chamber with an open central floor area.
        for(int x=1;x<WIDTH-1;x++) for(int z=1;z<DEPTH-1;z++) set(level,o.offset(x,6,z),Blocks.DEEPSLATE_TILES);
        for(int x=2;x<=24;x++) for(int z=2;z<=20;z++) {
            if ((x+z)%4==0) set(level,o.offset(x,1,z),Blocks.GRAY_CARPET);
        }
        // Defendant's dock at the center, facing the judge's bench.
        for(int x=9;x<=17;x++) for(int z=10;z<=14;z++) set(level,o.offset(x,2,z),Blocks.RED_CARPET);
        for(int x=10;x<=16;x++) set(level,o.offset(x,2,8),Blocks.DARK_OAK_PLANKS);
        for(int x=10;x<=16;x++) set(level,o.offset(x,3,8),Blocks.DARK_OAK_SLAB);
        set(level,o.offset(13,2,8),Blocks.LECTERN);
        for(int x : new int[]{9,17}) for(int z=9;z<=15;z++) set(level,o.offset(x,2,z),Blocks.IRON_BARS);
        for(int x=8;x<=18;x++) {set(level,o.offset(x,2,16),Blocks.DARK_OAK_PLANKS);set(level,o.offset(x,3,16),Blocks.SPRUCE_SLAB);}
        for(int x : new int[]{8,18}) for(int y=2;y<=4;y++) set(level,o.offset(x,y,16),Blocks.QUARTZ_PILLAR);
        // Public gallery benches and lights.
        for(int z : new int[]{4,6,18}) for(int x=3;x<=7;x++) {set(level,o.offset(x,2,z),Blocks.SPRUCE_STAIRS);set(level,o.offset(19+(x-3),2,z),Blocks.SPRUCE_STAIRS);}
        for(int x : new int[]{4,8,18,22}) for(int z : new int[]{4,18}) {set(level,o.offset(x,4,z),Blocks.CHAIN);set(level,o.offset(x,3,z),Blocks.LANTERN);}
        // Stairwell to the upper floor, left side.
        for(int i=0;i<7;i++) {
            set(level,o.offset(3,2+i/2,9+i),Blocks.SPRUCE_STAIRS);
            set(level,o.offset(4,2+i/2,9+i),Blocks.SPRUCE_STAIRS);
        }
        // Upper floor, balcony railings and offices.
        for(int x=1;x<WIDTH-1;x++) for(int z=1;z<DEPTH-1;z++) {
            if (x<8 || x>18 || z<7 || z>16) set(level,o.offset(x,7,z),Blocks.SMOOTH_STONE);
        }
        for(int y=8;y<=11;y++) {
            for(int x=0;x<WIDTH;x++){set(level,o.offset(x,y,0),Blocks.WHITE_CONCRETE);set(level,o.offset(x,y,DEPTH-1),Blocks.WHITE_CONCRETE);}
            for(int z=0;z<DEPTH;z++){set(level,o.offset(0,y,z),Blocks.WHITE_CONCRETE);set(level,o.offset(WIDTH-1,y,z),Blocks.WHITE_CONCRETE);}
        }
        for(int x=3;x<=23;x++) for(int y=9;y<=10;y++) set(level,o.offset(x,y,0),Blocks.GLASS_PANE);
        for(int x=2;x<=24;x++) for(int z=2;z<=20;z++) if((x+z)%3==0) set(level,o.offset(x,7,z),Blocks.BROWN_CARPET);
        // Judge chambers, records and administrative offices upstairs.
        for(int x=2;x<=7;x++) for(int z=2;z<=6;z++) set(level,o.offset(x,8,z),Blocks.BOOKSHELF);
        for(int x=19;x<=24;x++) for(int z=2;z<=6;z++) set(level,o.offset(x,8,z),Blocks.BOOKSHELF);
        for(int x=9;x<=17;x++) for(int z=17;z<=20;z++) set(level,o.offset(x,8,z),Blocks.DARK_OAK_PLANKS);
        for(int x=9;x<=17;x++) set(level,o.offset(x,9,18),Blocks.SPRUCE_SLAB);
        for(int x : new int[]{3,7,19,23}) for(int z : new int[]{9,13,17}) {set(level,o.offset(x,9,z),Blocks.CHAIN);set(level,o.offset(x,8,z),Blocks.LANTERN);}
        // Roof, trim and a strong institutional silhouette.
        for(int x=0;x<WIDTH;x++) for(int z=0;z<DEPTH;z++) set(level,o.offset(x,12,z),Blocks.DEEPSLATE_TILES);
        for(int x=0;x<WIDTH;x+=2){set(level,o.offset(x,13,0),Blocks.POLISHED_BLACKSTONE_BRICKS);set(level,o.offset(x,13,DEPTH-1),Blocks.BLACKSTONE_BRICKS);}
        for(int z=0;z<DEPTH;z+=2){set(level,o.offset(0,13,z),Blocks.BLACKSTONE_BRICKS);set(level,o.offset(WIDTH-1,13,z),Blocks.BLACKSTONE_BRICKS);}
        // Mark the structure to prevent duplicates and leave the court's central floor open.
        set(level,o.offset(13,-1,11),Blocks.LODESTONE);
    }
    private static void set(ServerLevel level, BlockPos pos, Block block) { level.setBlock(pos,block.defaultBlockState(),3); }
    private record Pending(ServerLevel level,int x,int z) {}
}
