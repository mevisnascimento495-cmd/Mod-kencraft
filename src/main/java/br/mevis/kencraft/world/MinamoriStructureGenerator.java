package br.mevis.kencraft.world;

import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber
public final class MinamoriStructureGenerator {
    private static final int CHANCE_DENOMINATOR = 96;
    private static final int WIDTH = 25;
    private static final int DEPTH = 15;
    private static final int HEIGHT = 8;

    private MinamoriStructureGenerator() {}

    @SubscribeEvent
    public static void onNewChunk(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ChunkAccess chunk = event.getChunk();
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        if (!isCandidateChunk(level, chunkX, chunkZ)) {
            return;
        }

        level.getServer().execute(() -> ensureAtChunk(level, chunkX, chunkZ));
    }

    public static boolean isCandidateChunk(ServerLevel level, int chunkX, int chunkZ) {
        long hash = mix(
            level.getSeed()
                ^ ((long) chunkX * 341873128712L)
                ^ ((long) chunkZ * 132897987541L)
        );
        return Math.floorMod(hash, CHANCE_DENOMINATOR) == 0;
    }

    /**
     * Places Minamori at the center of a candidate chunk if it does not already
     * exist. This is shared by natural generation and the locate command.
     */
    public static boolean ensureAtChunk(ServerLevel level, int chunkX, int chunkZ) {
        if (!isCandidateChunk(level, chunkX, chunkZ)) {
            return false;
        }

        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;

        if (hasMarker(level, centerX, centerZ)) {
            return true;
        }

        return placeMinamori(level, centerX, centerZ);
    }

    private static boolean hasMarker(ServerLevel level, int centerX, int centerZ) {
        int groundY = level.getHeight(
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ
        ) - 1;

        if (groundY < level.getMinBuildHeight()) {
            return false;
        }

        return level.getBlockState(new BlockPos(centerX, groundY, centerZ))
            .is(Blocks.LODESTONE);
    }

    private static boolean placeMinamori(ServerLevel level, int centerX, int centerZ) {
        int groundY = level.getHeight(
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ
        ) - 1;

        if (groundY < level.getMinBuildHeight() + 3
                || groundY > level.getMaxBuildHeight() - HEIGHT - 2) {
            return false;
        }

        if (!validGround(level, centerX, groundY, centerZ)) {
            return false;
        }

        for (int x = -12; x <= 12; x += 12) {
            for (int z = -7; z <= 7; z += 7) {
                int y = level.getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    centerX + x, centerZ + z
                ) - 1;

                if (Math.abs(y - groundY) > 2
                        || !validGround(level, centerX + x, y, centerZ + z)) {
                    return false;
                }
            }
        }

        BlockPos origin = new BlockPos(centerX - 12, groundY + 1, centerZ - 7);
        buildCafe(level, origin);

        spawn(level, origin.offset(12, 1, 6), KenCraftEntities.SHIN_HOMARE.get());
        spawn(level, origin.offset(10, 1, 4), KenCraftEntities.KAORI_HOMARE.get());
        return true;
    }

    private static boolean validGround(ServerLevel level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return !state.is(Blocks.WATER) && !state.is(Blocks.LAVA) && state.isSolid();
    }

    private static void buildCafe(ServerLevel level, BlockPos origin) {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                set(
                    level,
                    origin.offset(x, 0, z),
                    (x == 0 || x == WIDTH - 1 || z == 0 || z == DEPTH - 1)
                        ? Blocks.SMOOTH_STONE
                        : Blocks.SPRUCE_PLANKS
                );
            }
        }

        // Hidden locator marker below the floor at the exact structure center.
        set(level, origin.offset(12, -1, 6), Blocks.LODESTONE);

        for (int y = 1; y <= 5; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, origin.offset(x, y, 0), Blocks.WHITE_CONCRETE);
                set(level, origin.offset(x, y, DEPTH - 1), Blocks.WHITE_CONCRETE);
            }
            for (int z = 0; z < DEPTH; z++) {
                set(level, origin.offset(0, y, z), Blocks.WHITE_CONCRETE);
                set(level, origin.offset(WIDTH - 1, y, z), Blocks.WHITE_CONCRETE);
            }
        }

        for (int x = 3; x <= 21; x++) {
            for (int y = 2; y <= 4; y++) {
                set(level, origin.offset(x, y, 0), Blocks.GLASS);
            }
        }

        for (int x = 11; x <= 13; x++) {
            for (int y = 1; y <= 3; y++) {
                set(level, origin.offset(x, y, 0), Blocks.AIR);
            }
        }

        for (int x = -1; x <= WIDTH; x++) {
            for (int z = -1; z <= DEPTH; z++) {
                set(level, origin.offset(x, 6, z), Blocks.SPRUCE_SLAB);
            }
        }

        for (int x = 3; x <= 8; x++) {
            set(level, origin.offset(x, 1, 3), Blocks.SPRUCE_PLANKS);
        }

        set(level, origin.offset(5, 2, 3), Blocks.BREWING_STAND);
        set(level, origin.offset(6, 2, 3), Blocks.CAULDRON);

        for (int x : new int[]{5, 10, 15, 19}) {
            set(level, origin.offset(x, 1, 8), Blocks.SPRUCE_PLANKS);
            set(level, origin.offset(x, 1, 10), Blocks.SPRUCE_PLANKS);
        }

        set(level, origin.offset(5, 4, 7), Blocks.LANTERN);
        set(level, origin.offset(19, 4, 7), Blocks.LANTERN);
        drawMinamori(level, origin.offset(2, 1, -1));
    }

    private static void drawMinamori(ServerLevel level, BlockPos base) {
        String[] glyphs = {
            "10001/11011/10101/10001/10001",
            "11/01/01/01/11",
            "1001/1101/1011/1001/1001",
            "0110/1001/1111/1001/1001",
            "10001/11011/10101/10001/10001",
            "0110/1001/1001/1001/0110",
            "1110/1001/1110/1010/1001",
            "11/01/01/01/11"
        };

        int cursor = 0;
        for (String glyph : glyphs) {
            String[] rows = glyph.split("/");
            for (int row = 0; row < rows.length; row++) {
                for (int col = 0; col < rows[row].length(); col++) {
                    if (rows[row].charAt(col) == '1') {
                        set(
                            level,
                            base.offset(cursor + col, row + 1, 0),
                            Blocks.BLACK_CONCRETE
                        );
                    }
                }
            }
            cursor += rows[0].length() + 1;
        }
    }

    private static void spawn(
            ServerLevel level,
            BlockPos pos,
            net.minecraft.world.entity.EntityType<? extends Entity> type) {

        Entity entity = type.create(level);
        if (entity == null) {
            return;
        }

        entity.moveTo(
            pos.getX() + 0.5D,
            pos.getY(),
            pos.getZ() + 0.5D,
            0.0F,
            0.0F
        );
        level.addFreshEntity(entity);
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) {
        level.setBlock(pos, block.defaultBlockState(), 3);
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }
}
