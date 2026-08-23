package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.JioAnimationData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class JioSystem {
    public static final String NONE = "NONE";
    public static final String SEISHIN = "Seishin dan";
    public static final String HAKAI = "Hakai satsu Totetsu: Seimei kui";
    public static final String KATA = "Kata kyoka";
    public static final String PARADISE = "The Paradise";
    public static final String KING_OF_LIES = "The King of Lies";
    private static final String[] TECHNIQUES = {SEISHIN, HAKAI, KATA, PARADISE, KING_OF_LIES};
    private JioSystem() {}

    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d=event.getDispatcher();
        d.register(Commands.literal("kencraftjio").then(Commands.literal("roll").executes(c->roll(c.getSource()))).then(Commands.literal("next").executes(c->nextAbility(c.getSource()))).then(Commands.literal("use").executes(c->use(c.getSource()))).then(Commands.literal("charge").executes(c->charge(c.getSource()))));
    }

    private static boolean eligible(PlayerData data) {
        return (data.race()==Race.HUMAN && data.arfClass()>=4) || data.race()==Race.HYBRID || data.race()==Race.JASHIN;
    }
    private static int roll(CommandSourceStack source){
        if(!(source.getEntity() instanceof ServerPlayer player))return 0;
        PlayerData data=player.getData(ModAttachments.PLAYER_DATA);
        if(!eligible(data)){player.sendSystemMessage(Component.literal("Você precisa ser Investigador de Quarta Classe ou superior, Híbrido ou Jashin para usar Jio."));return 0;}
        String current=PlayerData.normalizeTechnique(data.jioTechnique());
        if(!NONE.equals(current)){player.sendSystemMessage(Component.literal("Você já girou sua técnica Jio e ganhou: "+current));return 0;}
        String chosen=TECHNIQUES[player.getRandom().nextInt(TECHNIQUES.length)];
        player.setData(ModAttachments.PLAYER_DATA,data.withJioTechnique(chosen));
        player.sendSystemMessage(Component.literal("Você girou sua técnica Jio e ganhou: "+chosen));
        player.sendSystemMessage(Component.literal("Use G para trocar de habilidade e F para usar a habilidade selecionada."));return 1;
    }
    private static int nextAbility(CommandSourceStack source){
        if(!(source.getEntity() instanceof ServerPlayer player))return 0;
        PlayerData data=player.getData(ModAttachments.PLAYER_DATA);if(!eligible(data))return 0;
        String technique=PlayerData.normalizeTechnique(data.jioTechnique());if(NONE.equals(technique)){player.sendSystemMessage(Component.literal("Primeiro gire sua técnica Jio no menu R."));return 0;}
        int next=(data.jioAbilitySlot()+1)%3;player.setData(ModAttachments.PLAYER_DATA,data.withJioAbilitySlot(next));player.sendSystemMessage(Component.literal(technique+" — Habilidade "+(next+1)+"/3 selecionada."));return 1;
    }
    private static int use(CommandSourceStack source){
        if(!(source.getEntity() instanceof ServerPlayer player))return 0;PlayerData data=player.getData(ModAttachments.PLAYER_DATA);if(!eligible(data))return 0;
        String technique=PlayerData.normalizeTechnique(data.jioTechnique());if(NONE.equals(technique)){player.sendSystemMessage(Component.literal("Você ainda não possui uma técnica Jio. Gire uma no menu R."));return 0;}
        int techniqueIndex=indexOf(technique),slot=data.jioAbilitySlot();
        if(PARADISE.equals(technique)){
            if(slot==0&&ParadiseController.isActive(player)){ParadiseController.end(player);player.sendSystemMessage(Component.literal("The Paradise: a paralisação terminou."));return 1;}
            if(slot==0){if(data.jio()<100){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo: 100 Jio."));return 0;}int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-100,max));ParadiseController.start(player);player.sendSystemMessage(Component.literal("The Paradise: o tempo foi interrompido em um raio de 30 blocos."));return 1;}
            if(!ParadiseController.isActive(player)){player.sendSystemMessage(Component.literal("Ative a Habilidade 1 do The Paradise primeiro."));return 0;}
            int cost=30;if(data.jio()<cost){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo desta habilidade: "+cost+" Jio."));return 0;}int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-cost,max));ParadiseController.useSecondary(player,slot);return 1;
        }
        if(KING_OF_LIES.equals(technique)){
            int cost=slot==2?150:30;if(data.jio()<cost){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo desta habilidade: "+cost+" Jio."));return 0;}int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-cost,max));useKingOfLies(player,slot);return 1;
        }
        int cost=cost(techniqueIndex,slot);if(data.jio()<cost){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo desta habilidade: "+cost+" Jio."));return 0;}int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-cost,max));int duration=animationDuration(techniqueIndex,slot);if(duration>0)player.setData(ModAttachments.JIO_ANIMATION,new JioAnimationData(technique,slot,player.level().getGameTime(),duration));
        if(techniqueIndex==0&&slot==2)player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,100,4,false,true));
        else if(techniqueIndex==2&&slot==0){player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,240,2,false,true));player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST,240,1,false,true));player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,240,1,false,true));}
        return 1;
    }
    private static void useKingOfLies(ServerPlayer player,int slot){switch(slot){case 0->{player.sendSystemMessage(Component.literal("Você é forte demais"));player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST,120,9,false,true));}case 1->{player.sendSystemMessage(Component.literal("Seus órgãos podem explodir"));LivingEntity target=findNearestLivingTarget(player,30.0D);if(target!=null){ServerLevel level=player.serverLevel();level.explode(player,target.getX(),target.getY(),target.getZ(),2.0F,false,net.minecraft.world.level.Level.ExplosionInteraction.NONE);target.kill();}}case 2->{player.sendSystemMessage(Component.literal("Você morreu"));LivingEntity target=findAimedLivingTarget(player,64.0D);if(target!=null)target.kill();}default->{}}}
    private static LivingEntity findNearestLivingTarget(ServerPlayer player,double range){AABB box=player.getBoundingBox().inflate(range);return player.level().getEntitiesOfClass(LivingEntity.class,box,e->e.isAlive()&&e!=player).stream().min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElse(null);}
    private static LivingEntity findAimedLivingTarget(ServerPlayer player,double range){Vec3 start=player.getEyePosition(),look=player.getLookAngle().normalize(),end=start.add(look.scale(range));BlockHitResult blockHit=player.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));double maxDistance=start.distanceToSqr(blockHit.getLocation());AABB search=player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0D);LivingEntity best=null;double bestDistance=Double.MAX_VALUE;for(LivingEntity entity:player.level().getEntitiesOfClass(LivingEntity.class,search,target->target.isAlive()&&target!=player)){java.util.Optional<Vec3> hit=entity.getBoundingBox().inflate(0.15D).clip(start,end);if(hit.isEmpty())continue;double distance=start.distanceToSqr(hit.get());if(distance>maxDistance||distance>=bestDistance)continue;best=entity;bestDistance=distance;}return best;}
    private static int charge(CommandSourceStack source){if(!(source.getEntity() instanceof ServerPlayer player))return 0;PlayerData data=player.getData(ModAttachments.PLAYER_DATA);if(!eligible(data))return 0;int max=ClanSystem.maxJio(player,data);if(data.jio()<max)player.setData(ModAttachments.PLAYER_DATA,data.withJio(Math.min(max,data.jio()+2),max));return 1;}
    static int indexOf(String t){for(int i=0;i<TECHNIQUES.length;i++)if(TECHNIQUES[i].equals(t))return i;return -1;}
    static int cost(int t,int s){if(t==0&&s==2)return 50;if(t==1&&s==2)return 100;return 30;}
    static int animationDuration(int t,int s){if(t==0)return switch(s){case 0->14;case 1->80;case 2->100;default->0;};if(t==1)return switch(s){case 0->16;case 1->140;case 2->14;default->0;};if(t==2)return switch(s){case 1->28;case 2->42;default->0;};if(t==3)return switch(s){case 1->12;case 2->200;default->0;};if(t==4)return switch(s){case 0->120;case 1->20;case 2->10;default->0;};return 0;}
}
