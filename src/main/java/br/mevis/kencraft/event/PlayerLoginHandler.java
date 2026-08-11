package br.mevis.kencraft.event;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.network.chat.Component;
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
        if (data.hasRace()) {
            return;
        }

        player.sendSystemMessage(Component.literal("Olá jogador(a)! Você entrou no KenCraft."));
        player.sendSystemMessage(Component.literal("Para escolher sua raça, digite Rinka ou Humano no chat."));
        player.sendSystemMessage(Component.literal("Pressione R para abrir o menu do KenCraft."));
    }
}
