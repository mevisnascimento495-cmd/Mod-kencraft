package br.mevis.kencraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public final class StructureDecoration {
    private StructureDecoration() {}

    public static void addEntrance(ServerLevel level, BlockPos origin, int width) {
        int center = width / 2;
        for (int x = 0; x < width; x++) {
            BlockPos path = origin.offset(x, 0, -2);
            if (level.getBlockState(path).isAir()) level.setBlock(path, Blocks.DIRT_PATH.defaultBlockState(), 3);
        }
        for (int dx : new int[]{-center + 3, center - 3}) {
            BlockPos post = origin.offset(center + dx, 1, -2);
            if (level.getBlockState(post).isAir()) level.setBlock(post, Blocks.LANTERN.defaultBlockState(), 3);
        }
        for (int x = 2; x < width - 2; x += 6) {
            BlockPos accent = origin.offset(x, 0, 1);
            if (level.getBlockState(accent).is(Blocks.STONE_BRICKS)) {
                level.setBlock(accent, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), 3);
            }
        }
    }
}
