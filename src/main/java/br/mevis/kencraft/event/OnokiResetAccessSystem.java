package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.KikakogouState;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.StoryProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.Locale;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OnokiResetAccessSystem {
    private OnokiResetAccessSystem() {}

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        if (!progress.onokiChatOpen() || !progress.onokiPath().equals("NONE")) return;

        String message = event.getRawText().trim().toLowerCase(Locale.ROOT);
        if (!message.equals("resetar kikan")
                && !message.equals("resetar técnica jio")
                && !message.equals("resetar tecnica jio")) return;

        event.setCanceled(true);
        if (message.equals("resetar kikan")) {
            resetKikan(player);
        } else {
            resetJioTechnique(player);
        }
    }

    private static void resetKikan(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.RINKA && data.race() != Race.HYBRID) {
            say(player, "Onoki: Você precisa ser Rinka ou Híbrido para trocar sua Kikan.");
            return;
        }
        if ("NONE".equals(data.kikanType())) {
            say(player, "Onoki: Você ainda não possui uma Kikan para resetar.");
            return;
        }
        if (data.physicalXp() < 10) {
            say(player, "Onoki: Você precisa de 10 XP físico.");
            return;
        }

        player.setData(ModAttachments.PLAYER_DATA,
                data.withXp(data.mentalXp(), data.physicalXp() - 10).withKikanType("NONE"));
        player.setData(ModAttachments.KIKAKOGOU_STATE, KikakogouState.DEFAULT);
        say(player, "Onoki: Pronto. Sua Kikan foi resetada por 10 XP físico. Agora você pode girar outra.");
    }

    private static void resetJioTechnique(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN && data.race() != Race.HYBRID && data.race() != Race.JASHIN) {
            say(player, "Onoki: Você não possui acesso ao Jio.");
            return;
        }
        if ("NONE".equals(PlayerData.normalizeTechnique(data.jioTechnique()))) {
            say(player, "Onoki: Você ainda não possui uma Técnica Jio para resetar.");
            return;
        }
        if (data.mentalXp() < 10) {
            say(player, "Onoki: Você precisa de 10 XP mental.");
            return;
        }

        player.setData(ModAttachments.PLAYER_DATA,
                data.withXp(data.mentalXp() - 10, data.physicalXp()).withJioTechnique("NONE"));
        say(player, "Onoki: Pronto. Sua Técnica Jio foi resetada por 10 XP mental. Agora você pode girar outra.");
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }
}
