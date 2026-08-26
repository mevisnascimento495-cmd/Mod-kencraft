package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.KikakogouState;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.StoryProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.Locale;

/** Applies the current 100-XP price to the existing Onoki point-shop flow. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class PointShopResetOverride {
    private static final int RESET_COST = 100;

    private PointShopResetOverride() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        if (!progress.onokiShopOpen()) return;

        String message = event.getRawText().trim().toLowerCase(Locale.ROOT);
        if (!message.equals("resetar kikan")
                && !message.equals("resetar técnica jio")
                && !message.equals("resetar tecnica jio")) return;

        event.setCanceled(true);
        player.setData(ModAttachments.STORY_PROGRESS, progress.withShopOpen(false));

        if (message.equals("resetar kikan")) {
            resetKikan(player);
        } else {
            resetJio(player);
        }
    }

    private static void resetKikan(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HYBRID && data.race() != Race.RINKA) {
            say(player, "Onoki: Você não tem uma Kikan que eu possa resetar.");
            return;
        }
        if (data.physicalXp() < RESET_COST) {
            say(player, "Onoki: Você precisa de 100 XP físico.");
            return;
        }
        player.setData(ModAttachments.PLAYER_DATA,
                data.withXp(data.mentalXp(), data.physicalXp() - RESET_COST).withKikanType("NONE"));
        player.setData(ModAttachments.KIKAKOGOU_STATE, KikakogouState.DEFAULT);
        say(player, "Onoki: Pronto. Sua Kikan foi resetada. Você gastou 100 XP físico. Agora pode girar outra.");
    }

    private static void resetJio(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN && data.race() != Race.HYBRID && data.race() != Race.JASHIN) {
            say(player, "Onoki: Você não tem acesso ao reset de Técnica Jio.");
            return;
        }
        if (data.mentalXp() < RESET_COST) {
            say(player, "Onoki: Você precisa de 100 XP mental.");
            return;
        }
        player.setData(ModAttachments.PLAYER_DATA,
                data.withXp(data.mentalXp() - RESET_COST, data.physicalXp()).withJioTechnique("NONE"));
        say(player, "Onoki: Pronto. Sua Técnica Jio foi resetada. Você gastou 100 XP mental. Agora pode girar outra.");
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }
}
