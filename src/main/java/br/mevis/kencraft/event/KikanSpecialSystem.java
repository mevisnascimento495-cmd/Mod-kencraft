package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KikanSpecialSystem {
    private static final String HALLUCINATION_TICKS = "kencraft_butterfly_hallucination_ticks";
    private static final String HALLUCINATION_TARGET = "kencraft_butterfly_hallucination_target";
    private static final String FLIGHT_GRANTED = "kencraft_butterfly_flight_granted";
    private static final String FLIGHT_PREVIOUS_MAYFLY = "kencraft_butterfly_previous_mayfly";
    private static final String FLIGHT_PREVIOUS_FLYING = "kencraft_butterfly_previous_flying";

    private KikanSpecialSystem() {}

    public static void startButterflyHallucination(ServerPlayer player, LivingEntity target) {
        player.getPersistentData().putInt(HALLUCINATION_TICKS, 16 * 20);
        player.getPersistentData().putInt(HALLUCINATION_TARGET, target.getId());
        player.sendSystemMessage(Component.literal("Borboleta: feromônios liberados. Alucinação aplicada por 16s."));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        boolean butterfly = data.race() == Race.RINKA && "BUTTERFLY_TENTACLE".equals(data.kikanType());

        var persistent = player.getPersistentData();
        if (butterfly && !player.isCreative() && !player.isSpectator()) {
            if (!persistent.getBoolean(FLIGHT_GRANTED)) {
                persistent.putBoolean(FLIGHT_PREVIOUS_MAYFLY, player.getAbilities().mayfly);
                persistent.putBoolean(FLIGHT_PREVIOUS_FLYING, player.getAbilities().flying);
                persistent.putBoolean(FLIGHT_GRANTED, true);
            }
            // Do not trust the marker alone: death, respawn and other ability resets can clear mayfly.
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        } else if (!butterfly && persistent.getBoolean(FLIGHT_GRANTED)) {
            // Creative/spectator flight belongs to Minecraft, so never revoke it here.
            if (!player.isCreative() && !player.isSpectator()) {
                boolean previousMayfly = persistent.getBoolean(FLIGHT_PREVIOUS_MAYFLY);
                boolean previousFlying = persistent.getBoolean(FLIGHT_PREVIOUS_FLYING);
                player.getAbilities().mayfly = previousMayfly;
                player.getAbilities().flying = previousMayfly && previousFlying;
                player.onUpdateAbilities();
            }
            persistent.remove(FLIGHT_GRANTED);
            persistent.remove(FLIGHT_PREVIOUS_MAYFLY);
            persistent.remove(FLIGHT_PREVIOUS_FLYING);
        }

        int ticks = player.getPersistentData().getInt(HALLUCINATION_TICKS);
        if (ticks <= 0) return;

        int targetId = player.getPersistentData().getInt(HALLUCINATION_TARGET);
        LivingEntity target = findLivingEntity(player, targetId);
        if (target == null || !target.isAlive()) {
            clearHallucination(player);
            return;
        }

        player.getPersistentData().putInt(HALLUCINATION_TICKS, ticks - 1);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        if (player.tickCount % 10 == 0) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 25, 0, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, false, true, true));
        }
        if (ticks <= 1) clearHallucination(player);
    }

    private static LivingEntity findLivingEntity(ServerPlayer player, int entityId) {
        if (entityId < 0) return null;
        var entity = player.serverLevel().getEntity(entityId);
        return entity instanceof LivingEntity living ? living : null;
    }

    private static void clearHallucination(ServerPlayer player) {
        player.getPersistentData().putInt(HALLUCINATION_TICKS, 0);
        player.getPersistentData().putInt(HALLUCINATION_TARGET, -1);
    }
}
