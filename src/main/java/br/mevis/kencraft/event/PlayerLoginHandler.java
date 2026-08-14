package br.mevis.kencraft.event;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class PlayerLoginHandler {
    private PlayerLoginHandler() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        String normalizedTechnique = PlayerData.normalizeTechnique(data.jioTechnique());
        if (!normalizedTechnique.equals(data.jioTechnique())) {
            data = data.withJioTechnique("NONE");
            player.setData(ModAttachments.PLAYER_DATA, data);
            if (data.race().name().equals("HUMAN")) {
                send(player, "Sua técnica Jio antiga foi removida. Abra o menu R e gire uma nova técnica.");
            }
        }

        if (data.hasRace()) {
            return;
        }

        send(player, "Olá jogador(a)! Você entrou no KenCraft.");
        send(player, "Para escolher sua raça, digite Rinka ou Humano no chat.");
        send(player, "Pressione R para abrir o menu do KenCraft.");
    }

    /**
     * Cria texto sem chamar Component.literal(), evitando o IncompatibleClassChangeError
     * que apareceu no build 0.2.3 quando o bytecode foi executado com Component como interface.
     */
    private static void send(ServerPlayer player, String text) {
        MutableComponent message = MutableComponent.create(ComponentContents.EMPTY).append(text);
        player.sendSystemMessage(message);
    }
}
