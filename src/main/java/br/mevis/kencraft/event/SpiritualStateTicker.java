package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.SpiritualState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Small server-side helpers for the first Sujo presentation/effects. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpiritualStateTicker {
    private SpiritualStateTicker() {}

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SpiritualState state = player.getData(ModAttachments.SPIRITUAL_STATE);
        if (!state.isSujo()) return;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        String technique = PlayerData.normalizeTechnique(data.jioTechnique());

        if ("Seishin dan".equals(technique) && data.jioAbilitySlot() == 1 && player.tickCount % 10 == 0) {
            player.addEffect(new MobEffectInstance(KenCraftEffects.IMORTALIDADE, 12, 0, false, true, true));
        }

        if ("The Paradise".equals(technique) && player.tickCount % 2 == 0) {
            // Blue spiritual wings: two particle arcs behind the player.
            double y = player.getY() + 1.25D;
            for (int side : new int[]{-1, 1}) {
                for (int i = 0; i < 4; i++) {
                    double spread = 0.35D + i * 0.32D;
                    double x = player.getX() + side * spread;
                    double z = player.getZ() - 0.35D - i * 0.10D;
                    player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + i * 0.12D, z, 1, 0, 0, 0, 0);
                }
            }
            // Blue bracelets around wrists and ankles.
            for (int side : new int[]{-1, 1}) {
                player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX() + side * 0.42D, player.getY() + 1.05D, player.getZ(), 2, 0.08D, 0.03D, 0.08D, 0);
                player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX() + side * 0.22D, player.getY() + 0.18D, player.getZ(), 2, 0.08D, 0.03D, 0.08D, 0);
            }
        }
    }
}
