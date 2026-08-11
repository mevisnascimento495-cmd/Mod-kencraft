package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftStatusEffects {
    private KenCraftStatusEffects() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return;

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() == Race.RINKA) {
            applyRinkaRegeneration(player, data);
        } else if (data.race() == Race.HUMAN) {
            restoreHumanJio(player, data);
        }
    }

    private static void applyRinkaRegeneration(ServerPlayer player, PlayerData data) {
        if (player.getHealth() >= player.getMaxHealth()) return;
        float amount = 0.10F + (data.genetics() * 0.05F);
        player.heal(amount);
    }

    private static void restoreHumanJio(ServerPlayer player, PlayerData data) {
        int max = data.calculatedHumanMaxJio();
        int current = Math.min(data.jio(), max);
        if (current >= max) return;

        int regeneration = 1 + Math.max(0, data.spiritualDevelopment() - 1) / 3;
        int next = Math.min(max, current + regeneration);
        player.setData(ModAttachments.PLAYER_DATA, data.withJio(next, max));
    }
}
