package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import br.mevis.kencraft.entity.OnokiEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OnokiNaturalSpawnSystem {
    private static final double SPAWN_CHANCE = 0.002D; // 0.2%
    private OnokiNaturalSpawnSystem() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().dimension() != Level.OVERWORLD) return;
        if (player.tickCount % 200 != 0) return;
        ServerLevel level = player.serverLevel();
        if (!level.getEntitiesOfClass(OnokiEntity.class, player.getBoundingBox().inflate(256.0D)).isEmpty()) return;
        if (player.getRandom().nextDouble() >= SPAWN_CHANCE) return;

        double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
        int distance = 24 + player.getRandom().nextInt(25);
        int x = BlockPos.containing(player.getX() + Math.cos(angle) * distance, 0, player.getZ() + Math.sin(angle) * distance).getX();
        int z = BlockPos.containing(player.getX() + Math.cos(angle) * distance, 0, player.getZ() + Math.sin(angle) * distance).getZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.getBlockState(pos.below()).isSolid() || !level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) return;

        OnokiEntity onoki = KenCraftEntities.ONOKI.get().create(level);
        if (onoki == null) return;
        onoki.moveTo(x + 0.5D, y, z + 0.5D, player.getRandom().nextFloat() * 360.0F, 0.0F);
        onoki.setPersistenceRequired();
        level.addFreshEntity(onoki);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§dUma presença estranha surgiu nas proximidades..."));
    }
}
