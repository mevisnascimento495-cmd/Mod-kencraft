package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ArfMissionData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.TalentData;
import br.mevis.kencraft.entity.RinkaHungryEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ArfMissionSystem {
    private ArfMissionSystem() {}

    public static void acceptOrShowMission(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN || data.arfClass() == 0) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: As missões da ARF são exclusivas para investigadores humanos."));
            return;
        }

        ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
        if (mission.active()) {
            showCurrentTask(player, mission);
            return;
        }

        if (mission.allRegularMissionsCompleted()) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: Você concluiu as 30 missões regulares da ARF."));
            player.sendSystemMessage(Component.literal("Patente de missão: " + mission.rankName() + " | Reputação: " + mission.reputation() + "/200."));
            player.sendSystemMessage(Component.literal("A última etapa não possui outro Rank UP."));
            return;
        }

        if (mission.rankUpPending()) {
            player.setData(ModAttachments.ARF_MISSION, mission.startNextTask());
            ArfMissionData started = player.getData(ModAttachments.ARF_MISSION);
            player.sendSystemMessage(Component.literal("Akio Ginshō: Você concluiu 10 missões do estágio " + (started.stage()) + "."));
            player.sendSystemMessage(Component.literal("MISSÃO DE RANK UP — alcance o próximo estágio de treinamento."));
            player.sendSystemMessage(Component.literal("Esta missão de Rank UP usa o ID " + started.missionId() + "."));
            player.sendSystemMessage(Component.literal("Depois dela, novas missões do estágio " + (started.stage() + 1) + " serão liberadas."));
            return;
        }

        player.setData(ModAttachments.ARF_MISSION, mission.startNextTask());
        ArfMissionData started = player.getData(ModAttachments.ARF_MISSION);

        if (started.missionId() == 1) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: Tenho uma missão para você."));
            player.sendSystemMessage(Component.literal("Missão 1 — Distrito 7 → Distrito 1"));
            player.sendSystemMessage(Component.literal("Derrote o Rinka do distrito 7 que veio para o 1°. Ele está em um hospital abandonado."));
            player.sendSystemMessage(Component.literal("Alvo: Rinka Hungry. Recompensa: 100 de reputação."));
        } else {
            player.sendSystemMessage(Component.literal("ARF — Missão " + started.missionId() + " do estágio " + started.stage() + "."));
            player.sendSystemMessage(Component.literal("Objetivo desta missão ainda não foi definido no projeto."));
            player.sendSystemMessage(Component.literal("A estrutura de 10 missões por estágio já está preparada para receber os objetivos."));
        }
        player.sendSystemMessage(Component.literal("Patente de missão: " + started.rankName() + " | Reputação: " + started.reputation() + "/200."));
    }

    private static void showCurrentTask(ServerPlayer player, ArfMissionData mission) {
        if (mission.missionId() == 1) {
            player.sendSystemMessage(Component.literal("ARF — Missão 1 ativa."));
            player.sendSystemMessage(Component.literal("Derrote o Rinka do distrito 7 que veio para o 1°. Ele está em um hospital abandonado."));
        } else if (mission.missionId() == 11 || mission.missionId() == 22) {
            player.sendSystemMessage(Component.literal("ARF — MISSÃO DE RANK UP " + mission.missionId() + " ativa."));
            player.sendSystemMessage(Component.literal("Objetivo específico do Rank UP ainda não foi definido."));
        } else {
            player.sendSystemMessage(Component.literal("ARF — Missão " + mission.missionId() + " ativa."));
            player.sendSystemMessage(Component.literal("Objetivo específico ainda não foi definido."));
        }
        player.sendSystemMessage(Component.literal("Estágio: " + mission.stage() + " — " + mission.rankName() + " | Reputação: " + mission.reputation() + "/200."));
    }

    public static boolean canUseTalentRoulette(ServerPlayer player) {
        ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
        return mission.mission1Completed();
    }

    public static void completeTalentUnlockCheck(ServerPlayer player) {
        if (canUseTalentRoulette(player)) {
            player.sendSystemMessage(Component.literal("Akio Ginshō: A Roleta de Talentos está disponível para você."));
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof RinkaHungryEntity)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
        if (!mission.active() || mission.missionId() != 1) return;

        player.setData(ModAttachments.ARF_MISSION, mission.completeCurrentTask());
        ArfMissionData done = player.getData(ModAttachments.ARF_MISSION);

        player.sendSystemMessage(Component.literal("Missão 1 concluída! Você derrotou Rinka Hungry."));
        player.sendSystemMessage(Component.literal("Recompensa recebida: +100 de reputação."));
        player.sendSystemMessage(Component.literal("Reputação atual: " + done.reputation() + "/200."));
        player.sendSystemMessage(Component.literal("Patente de missão da ARF: " + done.rankName() + "."));
        completeTalentUnlockCheck(player);
    }
}
