package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.entity.OnokiEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Fixed Onoki death reward: 100 physical XP and 100 mental XP. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OnokiRewardSystem {
    private OnokiRewardSystem() {}

    @SubscribeEvent
    public static void onOnokiDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof OnokiEntity)) return;
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player)) return;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp() + 100, data.physicalXp() + 100));
        player.sendSystemMessage(Component.literal("§dVocê derrotou Onoki e recebeu 100 XP físico e 100 XP mental."));
    }
}
