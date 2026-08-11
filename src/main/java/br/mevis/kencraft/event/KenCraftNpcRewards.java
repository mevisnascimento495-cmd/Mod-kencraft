package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.RinkaEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** XP is awarded only for killing the NPC, never for right-clicking it. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcRewards {
    private KenCraftNpcRewards() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (victim instanceof RinkaEntity) {
            player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp(), data.physicalXp() + 5));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Rinka derrotado: +5 XP Física."));
        } else if (victim instanceof ArfInvestigatorEntity) {
            player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp() + 5, data.physicalXp()));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Investigador da ARF derrotado: +5 XP Mental."));
        }
    }
}
