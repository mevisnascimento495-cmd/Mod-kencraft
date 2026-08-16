package br.mevis.kencraft.world;

import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.minecraft.server.level.ServerLevel;

@EventBusSubscriber
public final class MinamoriStructureGenerator {
    private static final int CHANCE_DENOMINATOR = 96;
    private static final int WIDTH = 25;
    private static final int DEPTH = 15;
    private static final int HEIGHT = 8;

    public static int CHANCE_DENOMINATOR() { return CHANCE_DENOMINATOR; }

    private MinamoriStructureGenerator() {}

    @SubscribeEvent
    public static void onNewChunk(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) return;
        ChunkAccess chunk = event.getChunk();
        long hash = hashForChunk(level.getSeed(), chunk.getPos().x, chunk.getPos().z);
        if (Math.floorMod(hash, CHANCE_DENOMINATOR) != 0) return;
        level.getServer().execute(() -> generateAt(level, chunk.getPos().getMiddleBlockX(), chunk.getPos().getMiddleBlockZ()));
    }

    public static long hashForChunk(long seed, int chunkX, int chunkZ) {
        return mix(seed ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L));
    }

    public static boolean generateAt(ServerLevel level, int centerX, int centerZ) {
        return placeMinamori(level, centerX, centerZ);
    }

    private static boolean placeMinamori(ServerLevel level, int centerX, int centerZ) {
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        if (groundY < level.getMinBuildHeight() + 3 || groundY > level.getMaxBuildHeight() - HEIGHT - 2) return false;
        if (!validGround(level, centerX, groundY, centerZ)) return false;
        for (int x = -12; x <= 12; x += 12) for (int z = -7; z <= 7; z += 7) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX + x, centerZ + z) - 1;
            if (Math.abs(y - groundY) > 2 || !validGround(level, centerX + x, y, centerZ + z)) return false;
        }
        BlockPos origin = new BlockPos(centerX - 12, groundY + 1, centerZ - 7);
        buildCafe(level, origin);
        spawn(level, origin.offset(12, 1, 6), KenCraftEntities.SHIN_HOMARE.get());
        spawn(level, origin.offset(10, 1, 4), KenCraftEntities.KAORI_HOMARE.get());
        return true;
    }

    private static boolean validGround(ServerLevel level, int x, int y, int z) {
        BlockState s = level.getBlockState(new BlockPos(x, y, z));
        return !s.is(Blocks.WATER) && !s.is(Blocks.LAVA) && s.isSolid();
    }

    private static void buildCafe(ServerLevel level, BlockPos o) {
        // Foundation and floor.
        for (int x = 0; x < WIDTH; x++) for (int z = 0; z < DEPTH; z++)
            set(level, o.offset(x, 0, z), (x == 0 || x == WIDTH - 1 || z == 0 || z == DEPTH - 1) ? Blocks.SMOOTH_STONE : Blocks.SPRUCE_PLANKS);
        set(level, o.offset(12, -1, 6), Blocks.LODESTONE);
        for (int x = 2; x <= 22; x++) for (int z = 2; z <= 12; z++) {
            if ((x + z) % 5 == 0) set(level, o.offset(x, 1, z), Blocks.BROWN_CARPET);
        }

        // Warm cream exterior with dark-wood framing.
        for (int y = 1; y <= 5; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, o.offset(x, y, 0), Blocks.WHITE_CONCRETE);
                set(level, o.offset(x, y, DEPTH - 1), Blocks.WHITE_CONCRETE);
            }
            for (int z = 0; z < DEPTH; z++) {
                set(level, o.offset(0, y, z), Blocks.WHITE_CONCRETE);
                set(level, o.offset(WIDTH - 1, y, z), Blocks.WHITE_CONCRETE);
            }
        }
        for (int x : new int[]{2, 22}) for (int y = 1; y <= 5; y++) set(level, o.offset(x, y, 0), Blocks.DARK_OAK_LOG);
        for (int x = 3; x <= 21; x++) for (int y = 2; y <= 4; y++) set(level, o.offset(x, y, 0), Blocks.GLASS);
        for (int x = 4; x <= 20; x += 4) set(level, o.offset(x, 5, 0), Blocks.DARK_OAK_PLANKS);
        for (int x = 11; x <= 13; x++) for (int y = 1; y <= 3; y++) set(level, o.offset(x, y, 0), Blocks.AIR);

        // Roof/awning.
        for (int x = -1; x <= WIDTH; x++) for (int z = -1; z <= DEPTH; z++) set(level, o.offset(x, 6, z), Blocks.DARK_OAK_SLAB);
        for (int x = 0; x < WIDTH; x++) set(level, o.offset(x, 7, 0), Blocks.DARK_OAK_PLANKS);
        for (int x = 3; x <= 21; x += 2) set(level, o.offset(x, 6, -1), Blocks.WHITE_CARPET);

        // Minamori sign on the front.
        drawMinamori(level, o.offset(3, 1, -1));

        // Main counter and coffee/work area.
        for (int x = 4; x <= 10; x++) set(level, o.offset(x, 2, 3), Blocks.DARK_OAK_PLANKS);
        for (int x = 4; x <= 10; x++) set(level, o.offset(x, 3, 3), Blocks.SPRUCE_SLAB);
        set(level, o.offset(5, 4, 3), Blocks.SMOKER);
        set(level, o.offset(6, 4, 3), Blocks.BREWING_STAND);
        set(level, o.offset(7, 4, 3), Blocks.CAULDRON);
        set(level, o.offset(8, 4, 3), Blocks.COMPOSTER);
        for (int x : new int[]{4, 6, 8, 10}) set(level, o.offset(x, 5, 2), Blocks.BARREL);

        // Back shelves and a small kitchen/storage zone.
        for (int z = 4; z <= 10; z += 2) {
            set(level, o.offset(21, 3, z), Blocks.BARREL);
            set(level, o.offset(21, 4, z), Blocks.SPRUCE_TRAPDOOR);
        }
        for (int z = 5; z <= 9; z += 2) set(level, o.offset(20, 2, z), Blocks.DARK_OAK_SLAB);
        for (int y = 2; y <= 4; y++) set(level, o.offset(22, y, 11), Blocks.DARK_OAK_LOG);
        for (int y = 1; y <= 3; y++) set(level, o.offset(22, y, 10), Blocks.AIR);

        // Dining tables, booths and chairs.
        table(level, o, 6, 9);
        table(level, o, 15, 9);
        table(level, o, 6, 12);
        booth(level, o, 14, 4);
        booth(level, o, 18, 4);
        booth(level, o, 14, 12);
        booth(level, o, 18, 12);

        // Warm hanging lamps.
        for (int x : new int[]{6, 12, 18}) {
            set(level, o.offset(x, 5, 6), Blocks.CHAIN);
            set(level, o.offset(x, 4, 6), Blocks.LANTERN);
        }
        set(level, o.offset(12, 5, 10), Blocks.LANTERN);
        set(level, o.offset(3, 3, 12), Blocks.FLOWER_POT);
        set(level, o.offset(4, 3, 12), Blocks.FLOWER_POT);
    }

    private static void table(ServerLevel level, BlockPos o, int x, int z) {
        set(level, o.offset(x, 2, z), Blocks.SPRUCE_SLAB);
        set(level, o.offset(x + 1, 2, z), Blocks.SPRUCE_SLAB);
        set(level, o.offset(x, 1, z), Blocks.SPRUCE_FENCE);
        set(level, o.offset(x + 1, 1, z), Blocks.SPRUCE_FENCE);
        set(level, o.offset(x, 1, z + 1), Blocks.SPRUCE_STAIRS);
        set(level, o.offset(x + 1, 1, z + 1), Blocks.SPRUCE_STAIRS);
    }

    private static void booth(ServerLevel level, BlockPos o, int x, int z) {
        set(level, o.offset(x, 1, z), Blocks.DARK_OAK_STAIRS);
        set(level, o.offset(x + 1, 1, z), Blocks.DARK_OAK_STAIRS);
        set(level, o.offset(x, 2, z), Blocks.SPRUCE_SLAB);
        set(level, o.offset(x + 1, 2, z), Blocks.SPRUCE_SLAB);
        set(level, o.offset(x, 2, z - 1), Blocks.DARK_OAK_PLANKS);
        set(level, o.offset(x + 1, 2, z - 1), Blocks.DARK_OAK_PLANKS);
        set(level, o.offset(x, 3, z - 1), Blocks.WHITE_CONCRETE);
        set(level, o.offset(x + 1, 3, z - 1), Blocks.WHITE_CONCRETE);
    }

    private static void drawMinamori(ServerLevel level, BlockPos base) {
        String[] glyphs = {"10001/11011/10101/10001/10001","11/01/01/01/11","1001/1101/1011/1001/1001","0110/1001/1111/1001/1001","10001/11011/10101/10001/10001","0110/1001/1001/1001/0110","1110/1001/1110/1010/1001","11/01/01/01/11"};
        int cursor = 0;
        for (String glyph : glyphs) {
            String[] rows = glyph.split("/");
            for (int row = 0; row < rows.length; row++) for (int col = 0; col < rows[row].length(); col++) if (rows[row].charAt(col) == '1') set(level, base.offset(cursor + col, row + 1, 0), Blocks.BLACK_CONCRETE);
            cursor += rows[0].length() + 1;
        }
    }

    private static void spawn(ServerLevel level, BlockPos pos, net.minecraft.world.entity.EntityType<? extends Entity> type) {
        Entity entity = type.create(level);
        if (entity == null) return;
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        level.addFreshEntity(entity);
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) { level.setBlock(pos, block.defaultBlockState(), 3); }

    private static long mix(long value) {
        value ^= value >>> 33; value *= 0xff51afd7ed558ccdL; value ^= value >>> 33; value *= 0xc4ceb9fe1a85ec53L; value ^= value >>> 33; return value;
    }
}
