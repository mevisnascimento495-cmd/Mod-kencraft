package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.RankCRinkaEntity;
import br.mevis.kencraft.entity.RinkaEntity;
import br.mevis.kencraft.entity.RishinEntity;
import br.mevis.kencraft.item.KenCraftItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcRewards {
    private KenCraftNpcRewards() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);

        if (victim instanceof RankCRinkaEntity) {
            // Rank C Rinkas always drop their special organ. This makes the item
            // available to Rinka players regardless of the final damage source.
            player.spawnAtLocation(KenCraftItems.JINSUIKAKU_RANK_C.get());
            player.sendSystemMessage(Component.literal("O Rinka Rank C derrotado deixou cair uma Jinsuikaku Rank C."));

            if (data.race() == Race.HUMAN && data.arfClass() == 4 && data.arfMissionKills() >= 0) {
                int kills = data.arfMissionKills() + 1;
                player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp(), data.physicalXp() + 5).withArfMissionKills(kills));
                player.sendSystemMessage(Component.literal("Rinka Rank C derrotado: +5 XP Física. Missão ARF: " + kills + "/3."));
                if (kills >= 3) player.sendSystemMessage(Component.literal("Missão ARF concluída: volte ao General da ARF para sua promoção."));
            }
            return;
        }

        if (victim instanceof RishinEntity) return;

        if (victim instanceof RinkaEntity) {
            int nextKills = data.arfMissionKills();
            if (data.race() == Race.HUMAN && data.arfClass() == 0 && data.arfMissionKills() >= 0 && data.arfMissionKills() < 5) nextKills++;
            player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp(), data.physicalXp() + 5).withArfMissionKills(nextKills));
            player.sendSystemMessage(Component.literal("Rinka derrotado: +5 XP Física."));
            if (data.race() == Race.HUMAN && data.arfClass() == 0 && nextKills < 5)
                player.sendSystemMessage(Component.literal("Missão ARF: Rinkas derrotados " + nextKills + "/5."));
            else if (data.race() == Race.HUMAN && data.arfClass() == 0 && nextKills == 5)
                player.sendSystemMessage(Component.literal("Missão ARF concluída: volte ao Investigador ARF General."));
            if (data.race() == Race.RINKA) {
                player.spawnAtLocation(KenCraftItems.JINSUIKAKU.get());
                player.sendSystemMessage(Component.literal("O Rinka derrotado deixou cair uma Jinsuikaku."));
            }
        } else if (victim instanceof ArfInvestigatorEntity) {
            player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp() + 5, data.physicalXp()));
            player.sendSystemMessage(Component.literal("Investigador da ARF derrotado: +5 XP Mental."));
        }
    }
}
