package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import br.mevis.kencraft.entity.RinkaEntity;
import br.mevis.kencraft.entity.RishinEntity;
import br.mevis.kencraft.item.KenCraftItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** XP and progression rewards happen only when a player kills the NPC. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class KenCraftNpcRewards {
    private KenCraftNpcRewards() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);

        if (victim instanceof RishinEntity) {
            int kills = data.arfMissionKills();
            if (data.race() == Race.HUMAN && data.arfClass() >= 2 && data.arfClass() <= 4 && kills >= 0) {
                kills++;
                player.setData(ModAttachments.PLAYER_DATA,
                        data.withXp(data.mentalXp() + 5, data.physicalXp()).withArfMissionKills(kills));
                player.sendSystemMessage(Component.literal("Rishin derrotado: +5 XP Mental. Missão ARF: " + kills + "/" + requiredRishins(data.arfClass()) + "."));
            }
            return;
        }

        if (victim instanceof RinkaEntity) {
            int nextKills = data.arfMissionKills();
            if (data.race() == Race.HUMAN && data.arfClass() == 0 && data.arfMissionKills() >= 0 && data.arfMissionKills() < 5) {
                nextKills = data.arfMissionKills() + 1;
            }

            player.setData(ModAttachments.PLAYER_DATA,
                    data.withXp(data.mentalXp(), data.physicalXp() + 5).withArfMissionKills(nextKills));
            player.sendSystemMessage(Component.literal("Rinka derrotado: +5 XP Física."));
            if (data.race() == Race.HUMAN && data.arfClass() == 0 && nextKills < 5) {
                player.sendSystemMessage(Component.literal("Missão ARF: Rinkas derrotados " + nextKills + "/5."));
            } else if (data.race() == Race.HUMAN && data.arfClass() == 0 && nextKills == 5) {
                player.sendSystemMessage(Component.literal("Missão ARF concluída: volte ao Investigador ARF General."));
            }

            if (data.race() == Race.RINKA) {
                player.spawnAtLocation(KenCraftItems.JINSUIKAKU.get());
                player.sendSystemMessage(Component.literal("O Rinka derrotado deixou cair uma Jinsuikaku."));
            }
        } else if (victim instanceof ArfInvestigatorEntity) {
            player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp() + 5, data.physicalXp()));
            player.sendSystemMessage(Component.literal("Investigador da ARF derrotado: +5 XP Mental."));
        }
    }

    private static int requiredRishins(int arfClass) {
        return switch (arfClass) {
            case 4 -> 5;
            case 3 -> 10;
            case 2 -> 20;
            default -> 0;
        };
    }
}
