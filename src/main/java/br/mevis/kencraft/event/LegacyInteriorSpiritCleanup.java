package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.RankCRinkaEntity;
import br.mevis.kencraft.entity.RinkaEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Removes Inner Spirit entities left behind by the old implementation that used Rinka/Rank C Rinka. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LegacyInteriorSpiritCleanup {
    private static final String SPIRIT_TAG = "kencraft_interior_spirit";

    private LegacyInteriorSpiritCleanup() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        Level level = player.level();
        if (!level.dimension().equals(SpiritualTrainingSystem.PARADISE_TRAINING)
                && !level.dimension().equals(SpiritualTrainingSystem.KING_TRAINING)) return;
        if (player.tickCount % 10 != 0) return;

        AABB area = new AABB(-32, 0, -32, 32, 128, 32);
        level.getEntities(player, area, LegacyInteriorSpiritCleanup::isLegacySpirit)
                .forEach(Entity::discard);
    }

    private static boolean isLegacySpirit(Entity entity) {
        if (!(entity instanceof RinkaEntity) && !(entity instanceof RankCRinkaEntity)) return false;
        return entity.getTags().contains(SPIRIT_TAG)
                || (entity.hasCustomName() && entity.getCustomName().getString().startsWith("Espírito Interior"));
    }
}
