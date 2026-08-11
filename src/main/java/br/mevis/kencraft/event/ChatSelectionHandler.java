package br.mevis.kencraft.event;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.Locale;

public final class ChatSelectionHandler {
    private ChatSelectionHandler() {}

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);

        if (data.hasRace()) {
            return;
        }

        event.setCanceled(true);
        String msg = event.getRawText().trim().toLowerCase(Locale.ROOT);

        if (msg.equals("rinka")) {
            player.setData(ModAttachments.PLAYER_DATA, PlayerData.forRinka());
            explainRinka(player);
            return;
        }

        if (msg.equals("humano")) {
            player.setData(ModAttachments.PLAYER_DATA, PlayerData.forHuman());
            explainHuman(player);
            return;
        }

        player.sendSystemMessage(Component.literal("Digite Rinka ou Humano para escolher sua raça."));
    }

    private static void explainRinka(ServerPlayer player) {
        send(player, "Você escolheu: RINKA");
        send(player, "O Rinka é um humano geneticamente modificado pelo Onoki.");
        send(player, "Possui a Jinsuikaku, que absorve nutrientes para produzir mais células.");
        send(player, "Isso garante regeneração e atributos físicos superiores aos de um humano comum.");
        send(player, "Pode liberar a Kikan: a Jinsuikaku expele células em modo de ataque.");
        send(player, "Rinkas não usam o sistema de Jio — a evolução deles é biológica.");
    }

    private static void explainHuman(ServerPlayer player) {
        send(player, "Você escolheu: HUMANO");
        send(player, "O Humano possui controle sobre o Jio, com estoque máximo de 100/100.");
        send(player, "O Jio pode ser usado para criar fenômenos sobrenaturais.");
        send(player, "Também pode ser usado para aumentar os próprios atributos.");
        send(player, "Humanos não têm as capacidades biológicas dos Rinka.");
        send(player, "A evolução deles é focada no domínio e desenvolvimento do Jio.");
    }

    private static void send(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }
}
