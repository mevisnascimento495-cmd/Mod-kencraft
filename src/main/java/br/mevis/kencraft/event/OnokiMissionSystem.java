package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.KikakogouState;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Locale;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class OnokiMissionSystem {
    private static final int EVOLUTION_TICKS = 400;
    private static final int EVOLUTION_STAT_BONUS = 10;
    private OnokiMissionSystem() {}

    public static void talkToOnoki(ServerPlayer player) {
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        Race race = player.getData(ModAttachments.PLAYER_DATA).race();
        if (race != Race.HUMAN && race != Race.RINKA && race != Race.HYBRID && race != Race.JASHIN) {
            say(player, "Onoki: Primeiro escolha uma raça. Depois volte aqui, não vou transformar um fantasma por acidente.");
            return;
        }
        if (progress.onokiEvolutionTicks() > 0) {
            say(player, "Onoki: Seu corpo ainda está se adaptando. Aguente firme por mais " + ((progress.onokiEvolutionTicks()+19)/20) + " segundos.");
            return;
        }
        if (progress.onokiEvolutionComplete()) {
            player.setData(ModAttachments.STORY_PROGRESS, progress.withShopOpen(true));
            say(player, "Onoki: Evolução concluída. Agora vendo duas coisas: reset de Kikan por 10 XP físico e reset de Técnica Jio por 10 XP mental.");
            say(player, "Onoki: Digite \"Resetar Kikan\" ou \"Resetar Técnica Jio\".");
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
        showProgress(player, progress);
    }

    public static void onRankCConsumed(ServerPlayer player) {
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        if (progress.onokiPath().equals("NONE") || progress.onokiMissionReady() || progress.onokiEvolutionComplete()) return;
        player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiJinsuikakuRankC(progress.onokiJinsuikakuRankC()+1));
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        String message = event.getRawText().trim().toLowerCase(Locale.ROOT);

        if (progress.onokiShopOpen()) {
            event.setCanceled(true);
            player.setData(ModAttachments.STORY_PROGRESS, progress.withShopOpen(false));
            if (message.equals("resetar kikan")) { resetKikan(player); return; }
            if (message.equals("resetar técnica jio") || message.equals("resetar tecnica jio")) { resetJioTechnique(player); return; }
            say(player, "Onoki: Eu só vendo \"Resetar Kikan\" e \"Resetar Técnica Jio\". Não complica.");
            return;
        }
        if (!progress.onokiChatOpen()) return;
        event.setCanceled(true);
        if (message.equals("quero me tornar um jashin") || message.equals("jashin")) {
            if (!canChoose(player)) return;
            player.setData(ModAttachments.STORY_PROGRESS, progress.startOnokiPath("JASHIN"));
            say(player, "Onoki: Bem... já que um Jashin foi o que você escolheu, você vai ter que sofrer um pouquinho.");
            say(player, "Onoki: Derrote 100 Rinkas Rank C e coma 20 Jinsuikaku Rank C para tornar seu corpo fácil de adaptar à mutação.");
            say(player, "Onoki: Logo depois, traga o coração de alguém com uma técnica, ou um Rinka com uma Kikan.");
            say(player, "Onoki: O Aodai Shou Aijou é um ótimo exemplo. O garoto de cabelo verde, você deve conhecer.");
            say(player, "Onoki: Mate-o e traga o coração dele. Depois volte aqui. Eu vou conferir tudo, porque confiar em você é pedir pra dar errado.");
            return;
        }
        if (message.equals("quero me tornar um híbrido") || message.equals("híbrido") || message.equals("hibrido")) {
            if (!canChoose(player)) return;
            player.setData(ModAttachments.STORY_PROGRESS, progress.startOnokiPath("HYBRID"));
            say(player, "Onoki: HAHAHAHA! Sua ambiciosidade é algo que me assusta... mas vou te ajudar.");
            say(player, "Onoki: Se você quer tanto se tornar o auge da sua espécie, mate Akio Ginshō, o general mais forte da história da ARF.");
            say(player, "Onoki: Em força bruta ele é inferior a Tatsuo Yakumori, mas ainda é poderoso o suficiente. Traga o coração dele.");
            say(player, "Onoki: Depois de matar Akio, mate 120 Rishins e Rinkas. Depois, mate 30 generais da ARF.");
            say(player, "Onoki: Por seguida, coma 50 Jinsuikaku Rank C. Sendo humano ou Rinka, tanto faz.");
            say(player, "Onoki: Depois de completar tudo, volte a mim e eu te tornarei o mais forte que eu conseguir.");
            return;
        }
        if (message.equals("não quero nada") || message.equals("nao quero nada")) {
            player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiChatOpen(false));
            say(player, "Onoki: Hm... prudente. Ou medroso. Ainda não decidi.");
            say(player, "Onoki: Não tem problema. Existem outras coisas que posso te oferecer.");
            return;
        }
        player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiChatOpen(true));
        say(player, "Onoki: Não entendi. Fala direito, jogador(a). Jashin, Híbrido ou nada?");
    }

    private static void resetKikan(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HYBRID && data.race() != Race.RINKA) {
            say(player, "Onoki: Você não tem uma Kikan que eu possa resetar."); return;
        }
        if (data.physicalXp() < 10) { say(player, "Onoki: Você precisa de 10 XP físico."); return; }
        player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp(), data.physicalXp()-10).withKikanType("NONE"));
        player.setData(ModAttachments.KIKAKOGOU_STATE, KikakogouState.DEFAULT);
        say(player, "Onoki: Pronto. Sua Kikan foi resetada. Agora pode girar outra.");
    }

    private static void resetJioTechnique(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN && data.race() != Race.HYBRID && data.race() != Race.JASHIN) {
            say(player, "Onoki: Você não tem acesso ao reset de Técnica Jio."); return;
        }
        if (data.mentalXp() < 10) { say(player, "Onoki: Você precisa de 10 XP mental."); return; }
        player.setData(ModAttachments.PLAYER_DATA, data.withXp(data.mentalXp()-10, data.physicalXp()).withJioTechnique("NONE"));
        say(player, "Onoki: Pronto. Sua Técnica Jio foi resetada. Agora pode girar outra.");
    }

    private static boolean canChoose(ServerPlayer player) {
        Race race = player.getData(ModAttachments.PLAYER_DATA).race();
        if (race == Race.HUMAN || race == Race.RINKA) return true;
        say(player, "Onoki: Você já passou por uma evolução. Não dá para repetir o ritual."); return false;
    }

    @SubscribeEvent
    public static void onLivingTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        if (progress.onokiEvolutionTicks() <= 0 || progress.onokiEvolutionComplete()) return;
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 255, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 255, false, true));
        StoryProgress next = progress.tickEvolution();
        if (next.onokiEvolutionTicks() == 0) {
            finishEvolution(player, next);
        } else {
            player.setData(ModAttachments.STORY_PROGRESS, next);
        }
    }

    private static void finishEvolution(ServerPlayer player, StoryProgress progress) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        Race target = progress.onokiPath().equals("HYBRID") ? Race.HYBRID : Race.JASHIN;
        PlayerData evolved = data.withRace(target).withJio(100,100);
        evolved = evolved.withStatus("strength", Math.min(PlayerData.MAX_STATUS, data.strength() + EVOLUTION_STAT_BONUS))
                .withStatus("defense", Math.min(PlayerData.MAX_STATUS, data.defense() + EVOLUTION_STAT_BONUS))
                .withStatus("spiritual", Math.min(PlayerData.MAX_STATUS, data.spiritualDevelopment() + EVOLUTION_STAT_BONUS))
                .withStatus("genetics", Math.min(PlayerData.MAX_STATUS, data.genetics() + EVOLUTION_STAT_BONUS))
                .withStatus("life", Math.min(PlayerData.MAX_STATUS, data.life() + EVOLUTION_STAT_BONUS));
        if (target == Race.JASHIN) {
            evolved = evolved.withKikanType("NONE").withJioTechnique(data.jioTechnique().equals("NONE") ? "NONE" : data.jioTechnique());
            player.setData(ModAttachments.KIKAKOGOU_STATE, KikakogouState.DEFAULT);
        }
        player.setData(ModAttachments.PLAYER_DATA, evolved);
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.setData(ModAttachments.STORY_PROGRESS, progress.finishEvolution());
        say(player, target == Race.HYBRID ? "Onoki: Pronto. Você agora é um Híbrido. Seu corpo alcançou o auge que eu consegui forçar." : "Onoki: Pronto. Você agora é um Jashin. O Jio ficou, sua Kikan e seu Kikakogou foram embora.");
        say(player, "Onoki: Depois volte aqui. Eu tenho dois resets para vender.");
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player)) return;
        LivingEntity dead = event.getEntity();
        StoryProgress progress = player.getData(ModAttachments.STORY_PROGRESS);
        String path = progress.onokiPath();
        if (path.equals("NONE") || progress.onokiEvolutionComplete()) return;
        if (dead instanceof RankCRinkaEntity && path.equals("JASHIN")) player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiRankCKills(progress.onokiRankCKills()+1));
        if ((dead instanceof RishinEntity || dead instanceof RinkaEntity) && path.equals("HYBRID")) player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiRishinRinkaKills(progress.onokiRishinRinkaKills()+1));
        if (dead instanceof ArfGeneralEntity && path.equals("HYBRID")) player.setData(ModAttachments.STORY_PROGRESS, progress.withOnokiArfGeneralKills(progress.onokiArfGeneralKills()+1));
        if (dead instanceof AodaiEntity && path.equals("JASHIN")) giveHeart(player, KenCraftItems.AODAI_HEART.get(), "Coração do Aodai Shou Aijou");
        if (dead instanceof AkioGinshoEntity && path.equals("HYBRID")) giveHeart(player, KenCraftItems.AKIO_GINSHO_HEART.get(), "Coração de Akio Ginshō");
    }

    public static void showProgress(ServerPlayer player, StoryProgress progress) {
        if (progress.onokiPath().equals("JASHIN")) {
            boolean heart=hasItem(player,KenCraftItems.AODAI_HEART.get());
            say(player,"Onoki: Jashin, né? Rinkas Rank C: "+progress.onokiRankCKills()+"/100.");
            say(player,"Onoki: Jinsuikaku Rank C: "+progress.onokiJinsuikakuRankC()+"/20.");
            say(player,"Onoki: Coração do Aodai: "+(heart?"ENTREGUE":"FALTA")+".");
            if(progress.onokiRankCKills()>=100&&progress.onokiJinsuikakuRankC()>=20&&heart){consumeOne(player,KenCraftItems.AODAI_HEART.get()); beginEvolution(player,progress);}
            return;
        }
        if(progress.onokiPath().equals("HYBRID")){
            boolean heart=hasItem(player,KenCraftItems.AKIO_GINSHO_HEART.get());
            say(player,"Onoki: Híbrido... coração do Akio: "+(heart?"ENTREGUE":"FALTA")+".");
            say(player,"Onoki: Rishins e Rinkas: "+progress.onokiRishinRinkaKills()+"/120.");
            say(player,"Onoki: Generais ARF: "+progress.onokiArfGeneralKills()+"/30.");
            say(player,"Onoki: Jinsuikaku Rank C: "+progress.onokiJinsuikakuRankC()+"/50.");
            if(progress.onokiRishinRinkaKills()>=120&&progress.onokiArfGeneralKills()>=30&&progress.onokiJinsuikakuRankC()>=50&&heart){consumeOne(player,KenCraftItems.AKIO_GINSHO_HEART.get());beginEvolution(player,progress);}
        }
    }

    private static void beginEvolution(ServerPlayer player, StoryProgress progress){
        player.setData(ModAttachments.PLAYER_DATA, player.getData(ModAttachments.PLAYER_DATA).withJio(0,player.getData(ModAttachments.PLAYER_DATA).maxJio()));
        player.setData(ModAttachments.STORY_PROGRESS, progress.startEvolution());
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,EVOLUTION_TICKS,255,false,true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,EVOLUTION_TICKS,255,false,true));
        say(player,"Onoki: Pare. Seu corpo entrou em colapso por 20 segundos. Depois disso, a evolução termina.");
    }

    private static void giveHeart(ServerPlayer player,Item item,String name){if(!hasItem(player,item)){player.getInventory().placeItemBackInInventory(new ItemStack(item));say(player,"Você recebeu: "+name+".");}}
    private static boolean hasItem(ServerPlayer player,Item item){for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(item))return true;return false;}
    private static void consumeOne(ServerPlayer player,Item item){for(int i=0;i<player.getInventory().getContainerSize();i++){ItemStack s=player.getInventory().getItem(i);if(s.is(item)){s.shrink(1);return;}}}
    private static void say(ServerPlayer player,String text){player.sendSystemMessage(Component.literal(text));}
}
