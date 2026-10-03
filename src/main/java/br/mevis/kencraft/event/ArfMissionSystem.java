package br.mevis.kencraft.event;
import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ArfMissionData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.entity.RinkaHungryEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
@EventBusSubscriber(modid=KenCraft.MOD_ID,bus=EventBusSubscriber.Bus.GAME)
public final class ArfMissionSystem{
 private ArfMissionSystem(){}
 public static void acceptOrShowMission(ServerPlayer player){
  PlayerData data=player.getData(ModAttachments.PLAYER_DATA);
  if(data.race()!=Race.HUMAN||data.arfClass()==0){player.sendSystemMessage(Component.literal("Akio Ginshō: As missões da ARF são exclusivas para investigadores humanos."));return;}
  ArfMissionData mission=player.getData(ModAttachments.ARF_MISSION);
  if(mission.active()){player.sendSystemMessage(Component.literal("ARF — "+mission.rankName()+": missão atual."));player.sendSystemMessage(Component.literal("Derrote o Rinka do distrito 7 que veio para o 1°. Ele está em um hospital abandonado."));player.sendSystemMessage(Component.literal("Reputação: "+mission.reputation()+" | Patente de missão: "+mission.rankName()));return;}
  if(mission.mission1Completed()){player.sendSystemMessage(Component.literal("Akio Ginshō: Primeira missão concluída. Você recebeu exatamente 100 de reputação."));player.sendSystemMessage(Component.literal("ARF — Patente de missão: "+mission.rankName()+" | Reputação: "+mission.reputation()));player.sendSystemMessage(Component.literal("As próximas missões serão adicionadas em uma atualização futura."));return;}
  player.setData(ModAttachments.ARF_MISSION,mission.startMission1());
  player.sendSystemMessage(Component.literal("Akio Ginshō: Tenho uma missão para você."));
  player.sendSystemMessage(Component.literal("Missão 1 — Distrito 7 → Distrito 1"));
  player.sendSystemMessage(Component.literal("Derrote o Rinka do distrito 7 que veio para o 1°. Ele está em um hospital abandonado."));
  player.sendSystemMessage(Component.literal("Alvo: Rinka Hungry. Recompensa: 100 de reputação."));
  player.sendSystemMessage(Component.literal("ARF — Patente de missão: "+player.getData(ModAttachments.ARF_MISSION).rankName()));
 }
 @SubscribeEvent public static void onLivingDeath(LivingDeathEvent event){
  if(!(event.getEntity() instanceof RinkaHungryEntity))return;if(!(event.getSource().getEntity() instanceof ServerPlayer player))return;
  ArfMissionData mission=player.getData(ModAttachments.ARF_MISSION);if(!mission.active()||mission.missionId()!=1)return;
  player.setData(ModAttachments.ARF_MISSION,mission.completeMission1());ArfMissionData done=player.getData(ModAttachments.ARF_MISSION);
  player.sendSystemMessage(Component.literal("Missão 1 concluída! Você derrotou Rinka Hungry."));player.sendSystemMessage(Component.literal("Recompensa recebida: +100 de reputação."));player.sendSystemMessage(Component.literal("Sua patente de missão da ARF agora é: "+done.rankName()+"."));
 }
}