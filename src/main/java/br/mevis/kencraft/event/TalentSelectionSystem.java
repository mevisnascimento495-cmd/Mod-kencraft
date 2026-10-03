package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ArfMissionData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.TalentData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class TalentSelectionSystem {
    private TalentSelectionSystem() {}

    public static void showTalentOptions(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        ArfMissionData mission = player.getData(ModAttachments.ARF_MISSION);
        if (data.race() != Race.HUMAN || !mission.mission1Completed()) {
            player.sendSystemMessage(Component.literal("§cComplete a primeira missão da ARF para desbloquear os talentos."));
            return;
        }

        TalentData current = player.getData(ModAttachments.TALENT_DATA);
        player.sendSystemMessage(Component.literal("§eAKIO — SISTEMA DE TALENTOS"));
        player.sendSystemMessage(Component.literal("Talento atual: " + display(current.talent())));
        player.sendSystemMessage(Component.literal("Opções: Força • Velocidade • Defesa • Regeneração."));
        player.sendSystemMessage(Component.literal("A seleção detalhada dos talentos ficará em um menu próprio na próxima etapa."));
        player.sendSystemMessage(Component.literal("Reputação disponível: " + mission.reputation() + "/200."));
    }

    private static String display(String talent) {
        return switch (talent) {
            case "FORCA" -> "Força";
            case "VELOCIDADE" -> "Velocidade";
            case "DEFESA" -> "Defesa";
            case "REGENERACAO" -> "Regeneração";
            default -> "Nenhum";
        };
    }
}
