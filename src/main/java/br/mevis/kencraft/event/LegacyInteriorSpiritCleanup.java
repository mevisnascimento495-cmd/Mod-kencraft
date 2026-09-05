package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.InteriorSpiritEntity;
import br.mevis.kencraft.entity.RankCRinkaEntity;
import br.mevis.kencraft.entity.RinkaEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Isolates the spiritual training dimensions from all other mobs and cleans up
 * entities left by the old Rinka-based Inner Spirit implementation.
 */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LegacyInteriorSpiritCleanup {
    private static final String SPIRIT_TAG = "kencraft_interior_spirit";

    private LegacyInteriorSpiritCleanup() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!isTrainingDimension(level)) return;

        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer || entity instanceof InteriorSpiritEntity) return;

        // The training arenas contain only the player and their single Interior Spirit.
        // This blocks vanilla and KenCraft mobs regardless of which system requested the spawn.
        if (entity instanceof LivingEntity) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Level level = player.level();
        if (!isTrainingDimension(level)) return;
        if (player.tickCount % 5 != 0) return;

        AABB area = new AABB(-32, 0, -32, 32, 128, 32);

        // Remove anything that was already present before the spawn guard ran.
        level.getEntitiesOfClass(LivingEntity.class, area,
                        entity -> !(entity instanceof ServerPlayer) && !(entity instanceof InteriorSpiritEntity))
                .forEach(Entity::discard);

        // There is exactly one Interior Spirit in a training dimension. If an older
        // version left several behind, keep the first and discard every duplicate.
        var spirits = level.getEntitiesOfClass(InteriorSpiritEntity.class, area, Entity::isAlive);
        if (!spirits.isEmpty()) {
            InteriorSpiritEntity keeper = spirits.get(0);
            keeper.addTag(SPIRIT_TAG);
            for (int i = 1; i < spirits.size(); i++) spirits.get(i).discard();
        }

        // Remove Inner Spirit entities left behind by the old Rinka/Rank C Rinka implementation.
        level.getEntities(player, area, LegacyInteriorSpiritCleanup::isLegacySpirit)
                .forEach(Entity::discard);
    }

    private static boolean isTrainingDimension(Level level) {
        return level.dimension().equals(SpiritualTrainingSystem.PARADISE_TRAINING)
                || level.dimension().equals(SpiritualTrainingSystem.KING_TRAINING);
    }

    private static boolean isLegacySpirit(Entity entity) {
        if (!(entity instanceof RinkaEntity) && !(entity instanceof RankCRinkaEntity)) return false;
        return entity.getTags().contains(SPIRIT_TAG)
                || (entity.hasCustomName() && entity.getCustomName().getString().startsWith("Espírito Interior"));
    }
}
