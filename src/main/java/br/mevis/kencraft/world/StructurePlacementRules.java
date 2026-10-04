package br.mevis.kencraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public final class StructurePlacementRules {
    public static final int MIN_DISTANCE = 192;
    private StructurePlacementRules() {}

    public static boolean isFarEnough(ServerLevel level, int centerX, int centerZ) {
        int minChunkX = Math.floorDiv(centerX - MIN_DISTANCE, 16);
        int maxChunkX = Math.floorDiv(centerX + MIN_DISTANCE, 16);
        int minChunkZ = Math.floorDiv(centerZ - MIN_DISTANCE, 16);
        int maxChunkZ = Math.floorDiv(centerZ + MIN_DISTANCE, 16);
        double limit = (double) MIN_DISTANCE * MIN_DISTANCE;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                int minX = cx << 4, minZ = cz << 4;
                for (int x = minX; x < minX + 16; x += 4) {
                    for (int z = minZ; z < minZ + 16; z += 4) {
                        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                        for (int y = surface - 16; y <= surface + 2; y++) {
                            if (!level.getBlockState(new BlockPos(x, y, z)).is(Blocks.LODESTONE)) continue;
                            double dx = x - centerX, dz = z - centerZ;
                            if (dx * dx + dz * dz < limit) return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
