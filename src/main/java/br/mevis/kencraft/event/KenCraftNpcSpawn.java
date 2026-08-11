package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcSpawn {
    private static final int RINKAS_PER_CHUNK = 4;
    private static final int ARF_PER_CHUNK = 2;
    private static final Random RANDOM = new Random();

    private KenCraftNpcSpawn() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ChunkPos chunkPos = chunk.getPos();
        boolean night = isNight(level);
        String targetName = night ? KenCraftNpcCommand.RINKA_NAME : KenCraftNpcCommand.ARF_NAME;
        int targetCount = night ? RINKAS_PER_CHUNK : ARF_PER_CHUNK;

        List<Villager> existing = level.getEntitiesOfClass(Villager.class,
                chunk.getBoundingBox(),
                villager -> villager.hasCustomName()
                        && targetName.equals(villager.getCustomName().getString()));

        int missing = Math.max(0, targetCount - existing.size());
        for (int i = 0; i < missing; i++) {
            spawnNpcInChunk(level, chunkPos, targetName, night);
        }
    }

    private static boolean isNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time >= 13000L && time < 23000L;
    }

    private static void spawnNpcInChunk(ServerLevel level, ChunkPos chunkPos, String name, boolean rinka) {
        int x = chunkPos.getMinBlockX() + RANDOM.nextInt(16);
        int z = chunkPos.getMinBlockZ() + RANDOM.nextInt(16);
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos pos = new BlockPos(x, y, z);

        if (!level.getBlockState(pos.below()).isSolid()) return;

        Villager npc = EntityType.VILLAGER.spawn(level, pos, MobSpawnType.EVENT);
        if (npc == null) return;

        npc.setCustomName(net.minecraft.network.chat.Component.literal(name));
        npc.setCustomNameVisible(true);
        npc.setNoAi(true);
        npc.setInvulnerable(true);
        npc.setSilent(true);
        npc.setPersistenceRequired();

        npc.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        npc.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        npc.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
    }
}
