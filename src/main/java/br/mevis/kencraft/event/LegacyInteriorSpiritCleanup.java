package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.InteriorSpiritEntity;
import br.mevis.kencraft.entity.RankCRinkaEntity;
import br.mevis.kencraft.entity.RinkaEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Isolates the spiritual training dimensions from all other mobs and legacy spirits. */
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
        if (entity instanceof LivingEntity) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isTrainingDimension(player.level())) return;
        if (event.getSource().getEntity() instanceof InteriorSpiritEntity) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Level level = player.level();
        if (!isTrainingDimension(level)) return;

        // No technique effect is allowed to remain on the player while training.
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.POISON);

        AABB area = new AABB(-32, 0, -32, 32, 128, 32);
        level.getEntitiesOfClass(LivingEntity.class, area,
                        entity -> !(entity instanceof ServerPlayer) && !(entity instanceof InteriorSpiritEntity))
                .forEach(Entity::discard);
        var spirits = level.getEntitiesOfClass(InteriorSpiritEntity.class, area, Entity::isAlive);
        if (!spirits.isEmpty()) {
            InteriorSpiritEntity keeper = spirits.get(0);
            keeper.addTag(SPIRIT_TAG);
            for (int i = 1; i < spirits.size(); i++) spirits.get(i).discard();
        }
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
