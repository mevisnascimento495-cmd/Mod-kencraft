package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.KikakogouState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OnokiPointShopChat {
    private OnokiPointShopChat() {}

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText().trim().toLowerCase(java.util.Locale.ROOT);
        if (!message.startsWith("kencraft loja pontos ")) return;
        event.setCanceled(true);

        String choice = message.substring("kencraft loja pontos ".length()).trim();
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        Race race = data.race();

        if (!choice.equals("humano") && !choice.equals("rinka")) {
            say(player, "Loja de Pontos: escolha Humano ou Rinka.");
            return;
        }

        if (race == Race.HUMAN && !choice.equals("humano")) {
            say(player, "Loja de Pontos: como Humano, escolha Humano.");
            return;
        }
        if (race == Race.RINKA && !choice.equals("rinka")) {
            say(player, "Loja de Pontos: como Rinka, escolha Rinka.");
            return;
        }
        if (race != Race.HUMAN && race != Race.RINKA && race != Race.HYBRID && race != Race.JASHIN) {
            say(player, "Loja de Pontos: sua raça não pode usar esta loja.");
            return;
        }

        if (choice.equals("humano")) {
            if (data.mentalXp() < 10) {
                say(player, "Loja de Pontos: Reset de Técnica Jio custa 10 XP mental. Você tem " + data.mentalXp() + ".");
                return;
            }
            player.setData(ModAttachments.PLAYER_DATA,
                    data.withXp(data.mentalXp() - 10, data.physicalXp()).withJioTechnique("NONE"));
            say(player, "Loja de Pontos: Técnica Jio resetada. Agora você pode girar uma nova técnica.");
            return;
        }

        if (data.physicalXp() < 10) {
            say(player, "Loja de Pontos: Reset de Kikan custa 10 XP físico. Você tem " + data.physicalXp() + ".");
            return;
        }
        player.setData(ModAttachments.PLAYER_DATA,
                data.withXp(data.mentalXp(), data.physicalXp() - 10).withKikanType("NONE"));
        player.setData(ModAttachments.KIKAKOGOU_STATE, KikakogouState.DEFAULT);
        say(player, "Loja de Pontos: Kikan resetada. Agora você pode girar uma nova Kikan.");
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }
}
