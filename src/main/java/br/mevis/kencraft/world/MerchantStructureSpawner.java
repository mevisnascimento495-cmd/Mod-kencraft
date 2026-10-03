package br.mevis.kencraft.world;

import br.mevis.kencraft.entity.ArtifactShopEntity;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.concurrent.ThreadLocalRandom;

public final class MerchantStructureSpawner {
    private static final double SPAWN_CHANCE = 0.74D;
    private static final int SIDE_OFFSET = 2;
    private static final int FRONT_OFFSET = 2;

    private MerchantStructureSpawner() {}

    public static void trySpawn(ServerLevel level, int centerX, int centerZ, int groundY, int width, int depth) {
        if (ThreadLocalRandom.current().nextDouble() >= SPAWN_CHANCE) return;

        BlockPos center = new BlockPos(centerX, groundY, centerZ);
        if (!level.getEntitiesOfClass(ArtifactShopEntity.class,
                new net.minecraft.world.phys.AABB(center).inflate(16.0D, 8.0D, 16.0D),
                Entity::isAlive).isEmpty()) return;

        int originX = centerX - width / 2;
        int originZ = centerZ - depth / 2;

        for (int attempt = 0; attempt < 12; attempt++) {
            int side = ThreadLocalRandom.current().nextInt(3);
            int x;
            int z;

            if (side == 0) {
                x = ThreadLocalRandom.current().nextInt(originX + 2, originX + width - 2);
                z = originZ - FRONT_OFFSET;
            } else if (side == 1) {
                x = originX - SIDE_OFFSET;
                z = ThreadLocalRandom.current().nextInt(originZ + 2, originZ + depth - 2);
            } else {
                x = originX + width + SIDE_OFFSET - 1;
                z = ThreadLocalRandom.current().nextInt(originZ + 2, originZ + depth - 2);
            }

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos.below()).isSolid() || !level.getBlockState(pos).isAir()) continue;

            ArtifactShopEntity merchant = KenCraftEntities.ARTIFACT_SHOP.get().create(level);
            if (merchant == null) return;
            merchant.moveTo(x + 0.5D, y, z + 0.5D, ThreadLocalRandom.current().nextFloat() * 360.0F, 0.0F);
            merchant.restrictTo(pos, 7);
            level.addFreshEntity(merchant);
            return;
        }
    }
}
