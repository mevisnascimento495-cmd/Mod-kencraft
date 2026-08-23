package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.StoryProgress;
import br.mevis.kencraft.entity.AkioGinshoEntity;
import br.mevis.kencraft.entity.AodaiEntity;
import br.mevis.kencraft.entity.ArfGeneralEntity;
import br.mevis.kencraft.entity.RankCRinkaEntity;
import br.mevis.kencraft.entity.RinkaEntity;
import br.mevis.kencraft.entity.RishinEntity;
import br.mevis.kencraft.item.KenCraftItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Locale;

/** Mission/progression logic for Onoki's Jashin and Hybrid paths. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OnokiMissionSystem {
    private OnokiMissionSystem() {}

    public static void talkToOnoki(ServerPlayer player) {
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        Race race = data.race();

        if (race != Race.HUMAN && race != Race.RINKA) {
            player.sendSystemMessage(Component.literal("Onoki: Primeiro escolha uma raça. Depois volte aqui, não quero transformar um fantasma em Jashin por acidente."));
            return;
        }

        if (progress.onokiPath().equals("NONE")) {
            player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiChatOpen(true));
            say(player, "Onoki: Olá jogador(a), você chegou até aqui... está bem forte, né?");
            say(player, "Onoki: Posso te dar um pequeno presente. Mas não será de graça... na verdade, será uma graça divina.");
            say(player, "Onoki: Você deve estar se perguntando: o que vou te dar? Simples. Te darei uma evolução.");
            say(player, "Onoki: Você sendo Rinka ou humano pode se tornar um Híbrido. Agora, um humano ou Rinka pode se tornar Jashin.");
            say(player, "Onoki: Jashin mantém o Jio, mas perde a Kikan e, posteriormente, o Kikakogou.");
            say(player, "Onoki: Então... o que você gostaria de se tornar? Um Jashin ou um Híbrido?");
            say(player, "Onoki: Os dois caminhos serão muito difíceis. Você tem três opções: sair daqui sendo uma dessas duas raças, ou sair daqui do jeito que nasceu e está.");
            say(player, "Onoki: Se quiser algum deles, diga no chat: \"Quero me tornar um Jashin\" ou \"Quero me tornar um Híbrido\".");
            say(player, "Onoki: Se não quiser nada, diga \"Não quero nada\". Devo ter outras coisas que te interessem.");
            return;
        }

        if (progress.onokiMissionReady()) {
            say(player, "Onoki: HAHAHA... você realmente conseguiu. Eu sabia que ia dar trabalho, mas não pensei que fosse tanto.");
            say(player, "Onoki: Sua missão está concluída. Ainda não vou realizar a evolução aqui, mas você já provou que merece chegar até a próxima etapa.");
            return;
        }

        showProgress(player, progress);
    }

    public static void onRankCConsumed(ServerPlayer player) {
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        if (progress.onokiPath().equals("NONE") || progress.onokiMissionReady()) return;
        int next = progress.onokiJinsuikakuRankC() + 1;
        player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiJinsuikakuRankC(next));
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        if (!progress.onokiChatOpen()) return;

        event.setCanceled(true);
        String message = event.getRawText().trim().toLowerCase(Locale.ROOT);
        StoryProgress next = progress.withOnokiChatOpen(false);

        if (message.equals("quero me tornar um jashin") || message.equals("jashin")) {
            if (!canChoose(player)) return;
            next = next.startOnokiPath("JASHIN");
            player.setData(ModAttachments.STORY_PROGRESS, next);
            say(player, "Onoki: Bem... já que um Jashin foi o que você escolheu, você vai ter que sofrer um pouquinho.");
            say(player, "Onoki: Derrote 100 Rinkas Rank C e coma 20 Jinsuikaku Rank C para tornar seu corpo fácil de adaptar à mutação.");
            say(player, "Onoki: Logo depois, você terá que trazer o coração de alguém com uma técnica, ou um Rinka com uma Kikan.");
            say(player, "Onoki: O Aodai Shou Aijou é um ótimo exemplo. O garoto de cabelo verde, você deve conhecer.");
            say(player, "Onoki: Mate-o e traga o coração dele. Depois volte aqui. Eu vou conferir tudo, porque confiar em você é pedir pra dar errado.");
            return;
        }

        if (message.equals("quero me tornar um híbrido") || message.equals("híbrido") || message.equals("hibrido")) {
            if (!canChoose(player)) return;
            next = next.startOnokiPath("HYBRID");
            player.setData(ModAttachments.STORY_PROGRESS, next);
            say(player, "Onoki: HAHAHAHA! Sua ambiciosidade é algo que me assusta... mas vou te ajudar.");
            say(player, "Onoki: Se você quer tanto se tornar o auge da sua espécie, mate Akio Ginshō, o general mais forte da história da ARF.");
            say(player, "Onoki: Em força bruta ele é inferior a Tatsuo Yakumori, mas ainda é poderoso o suficiente para arrancar seu orgulho... e sua cabeça.");
            say(player, "Onoki: Traga o coração dele.");
            say(player, "Onoki: Depois de matar Akio, mate 120 Rishins e Rinkas. Depois, mate 30 generais da ARF.");
            say(player, "Onoki: Por seguida, coma 50 Jinsuikaku Rank C. Sendo humano ou Rinka, tanto faz.");
            say(player, "Onoki: Depois de completar tudo, volte a mim e eu te tornarei o mais forte que eu conseguir.");
            return;
        }

        if (message.equals("não quero nada") || message.equals("nao quero nada")) {
            player.setData(ModAttachments.STORY_PROGRESS, next);
            say(player, "Onoki: Hm... prudente. Ou medroso. Ainda não decidi.");
            say(player, "Onoki: Não tem problema. Existem outras coisas que posso te oferecer, mas não precisa decidir tudo hoje.");
            return;
        }

        player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiChatOpen(true));
        say(player, "Onoki: Não entendi. Fala direito, jogador(a). Você quer Jashin, Híbrido ou não quer nada?");
    }

    private static boolean canChoose(ServerPlayer player) {
        Race race = player.getData(ModAttachments.PLAYER_DATA).race();
        if (race == Race.HUMAN || race == Race.RINKA) return true;
        say(player, "Onoki: Você nem escolheu uma raça e já quer uma evolução? Calma aí, criatura.");
        return false;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player)) return;

        LivingEntity dead = event.getEntity();
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        String path = progress.onokiPath();
        if (path.equals("NONE") || progress.onokiMissionReady()) return;

        if (dead instanceof RankCRinkaEntity && path.equals("JASHIN")) {
            int next = progress.onokiRankCKills() + 1;
            player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiRankCKills(next));
        }

        if ((dead instanceof RishinEntity || dead instanceof RinkaEntity) && path.equals("HYBRID")) {
            int next = progress.onokiRishinRinkaKills() + 1;
            player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiRishinRinkaKills(next));
        }

        if (dead instanceof ArfGeneralEntity && path.equals("HYBRID")) {
            int next = progress.onokiArfGeneralKills() + 1;
            player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiArfGeneralKills(next));
        }

        if (dead instanceof AodaiEntity && path.equals("JASHIN")) {
            giveHeart(player, KenCraftItems.AODAI_HEART.get(), "Coração do Aodai Shou Aijou");
            StoryProgress current = player.getData(ModAttachments.STORY_PROGRESS);
            player.setData(ModAttachments.STORY_PROGRESS, current.withOnokiHearts(true, current.onokiAkioHeartReady()));
        }

        if (dead instanceof AkioGinshoEntity && path.equals("HYBRID")) {
            giveHeart(player, KenCraftItems.AKIO_GINSHO_HEART.get(), "Coração de Akio Ginshō");
            StoryProgress current = player.getData(ModAttachments.STORY_PROGRESS);
            player.setData(ModAttachments.STORY_PROGRESS, current.withOnokiHearts(current.onokiAodaiHeartReady(), true));
        }
    }

    public static void showProgress(ServerPlayer player, StoryProgress progress) {
        if (progress.onokiPath().equals("JASHIN")) {
            boolean heart = hasItem(player, KenCraftItems.AODAI_HEART.get());
            say(player, "Onoki: Jashin, né? Vamos ver se você fez o que eu mandei.");
            say(player, "Onoki: Rinkas Rank C: " + progress.onokiRankCKills() + "/100.");
            say(player, "Onoki: Jinsuikaku Rank C: " + progress.onokiJinsuikakuRankC() + "/20.");
            say(player, "Onoki: Coração do Aodai: " + (heart ? "ENTREGUE" : "FALTA") + ".");
            if (progress.onokiRankCKills() >= 100 && progress.onokiJinsuikakuRankC() >= 20 && heart) {
                consumeOne(player, KenCraftItems.AODAI_HEART.get());
                player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiHearts(true, progress.onokiAkioHeartReady()).withOnokiMissionReady(true));
                say(player, "Onoki: Tudo certo... você realmente fez isso. Volte a falar comigo para seguir para a próxima etapa.");
            }
            return;
        }

        if (progress.onokiPath().equals("HYBRID")) {
            boolean heart = hasItem(player, KenCraftItems.AKIO_GINSHO_HEART.get());
            say(player, "Onoki: Híbrido... agora vamos conferir se você é ambicioso ou só maluco.");
            say(player, "Onoki: Coração de Akio Ginshō: " + (heart ? "ENTREGUE" : "FALTA") + ".");
            say(player, "Onoki: Rishins e Rinkas: " + progress.onokiRishinRinkaKills() + "/120.");
            say(player, "Onoki: Generais da ARF: " + progress.onokiArfGeneralKills() + "/30.");
            say(player, "Onoki: Jinsuikaku Rank C: " + progress.onokiJinsuikakuRankC() + "/50.");
            if (progress.onokiRishinRinkaKills() >= 120 && progress.onokiArfGeneralKills() >= 30 && progress.onokiJinsuikakuRankC() >= 50 && heart) {
                consumeOne(player, KenCraftItems.AKIO_GINSHO_HEART.get());
                player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiHearts(progress.onokiAodaiHeartReady(), true).withOnokiMissionReady(true));
                say(player, "Onoki: Tudo certo... você realmente chegou até o fim. Volte a falar comigo para seguir para a próxima etapa.");
            }
        }
    }

    private static void giveHeart(ServerPlayer player, Item item, String name) {
        if (!hasItem(player, item)) {
            player.getInventory().placeItemBackInInventory(new ItemStack(item));
            say(player, "§aVocê recebeu: " + name + ".");
        }
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) return true;
        }
        return false;
    }

    private static void consumeOne(ServerPlayer player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                return;
            }
        }
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }
}
