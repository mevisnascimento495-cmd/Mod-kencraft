package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.JioAnimationData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.SpiritualState;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class JioSystem {
    public static final String NONE = "NONE";
    public static final String SEISHIN = "Seishin dan";
    public static final String HAKAI = "Hakai satsu Totetsu: Seimei kui";
    public static final String KATA = "Kata kyoka";
    public static final String PARADISE = "The Paradise";
    public static final String KING_OF_LIES = "The King of Lies";
    private static final String[] TECHNIQUES = {SEISHIN, HAKAI, KATA, PARADISE, KING_OF_LIES};
    private static final String SUJO_ATTACK = "kencraft_sujo_attack";
    private static final String SUJO_AUX = "kencraft_sujo_aux";
    private static final String SUJO_AUX2 = "kencraft_sujo_aux2";
    private static final String INTANGIBLE_OLD_NO_PHYSICS = "kencraft_sujo_old_nophysics";
    private static final String INTANGIBLE_OLD_INVULNERABLE = "kencraft_sujo_old_invulnerable";
    private JioSystem() {}

    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d=event.getDispatcher();
        d.register(Commands.literal("kencraftjio")
                .then(Commands.literal("roll").executes(c->roll(c.getSource())))
                .then(Commands.literal("next").executes(c->nextAbility(c.getSource())))
                .then(Commands.literal("use").executes(c->use(c.getSource())))
                .then(Commands.literal("charge").executes(c->charge(c.getSource())))
                .then(Commands.literal("sujo").executes(c->toggleSujo(c.getSource()))));
    }

    private static boolean eligible(PlayerData data) {
        return (data.race()==Race.HUMAN && data.arfClass()>=4) || data.race()==Race.HYBRID || data.race()==Race.JASHIN;
    }

    private static boolean sujo(PlayerData data, ServerPlayer player) {
        return player.getData(ModAttachments.SPIRITUAL_STATE).isSujo();
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

    private static int toggleSujo(CommandSourceStack source){
        if(!(source.getEntity() instanceof ServerPlayer player))return 0;
        PlayerData data=player.getData(ModAttachments.PLAYER_DATA);
        if(!eligible(data)){player.sendSystemMessage(Component.literal("Você não pode liberar o estado espiritual sem acesso ao Jio."));return 0;}
        String technique=PlayerData.normalizeTechnique(data.jioTechnique());
        if(NONE.equals(technique)){player.sendSystemMessage(Component.literal("Primeiro obtenha uma Técnica de Jio."));return 0;}
        if(!SpiritualStateAccessGuard.canActivate(data)){
            player.sendSystemMessage(SpiritualStateAccessGuard.requirementMessage());
            return 0;
        }
        SpiritualState current=player.getData(ModAttachments.SPIRITUAL_STATE);
        if(current.isSujo()){
            player.setData(ModAttachments.SPIRITUAL_STATE, SpiritualState.DEFAULT);
            player.getPersistentData().putInt(SUJO_ATTACK, 0);
            player.getPersistentData().putInt(SUJO_AUX, 0);
            player.getPersistentData().putInt(SUJO_AUX2, 0);
            if(PARADISE.equals(technique)) ParadiseController.end(player);
            restoreIntangible(player);
            player.sendSystemMessage(Component.literal("Estado Sujo desativado. Seu poder voltou ao estado normal."));
        }else{
            player.setData(ModAttachments.SPIRITUAL_STATE, new SpiritualState(SpiritualState.SUJO));
            player.sendSystemMessage(Component.literal(technique+" — Estado Sujo liberado."));
            player.sendSystemMessage(Component.literal("Seu Espírito Interior liberou uma parte do poder total da técnica."));
        }
        return 1;
    }

    private static int nextAbility(CommandSourceStack source){
        if(!(source.getEntity() instanceof ServerPlayer player))return 0;
        PlayerData data=player.getData(ModAttachments.PLAYER_DATA);if(!eligible(data))return 0;
        String technique=PlayerData.normalizeTechnique(data.jioTechnique());if(NONE.equals(technique)){player.sendSystemMessage(Component.literal("Primeiro gire sua técnica Jio no menu R."));return 0;}
        int next=(data.jioAbilitySlot()+1)%3;player.setData(ModAttachments.PLAYER_DATA,data.withJioAbilitySlot(next));player.sendSystemMessage(Component.literal(technique+" — Habilidade "+(next+1)+"/3 selecionada."));return 1;
    }

    private static int use(CommandSourceStack source){
        if(!(source.getEntity() instanceof ServerPlayer player))return 0;
        PlayerData data=player.getData(ModAttachments.PLAYER_DATA);if(!eligible(data))return 0;
        String technique=PlayerData.normalizeTechnique(data.jioTechnique());if(NONE.equals(technique)){player.sendSystemMessage(Component.literal("Você ainda não possui uma técnica Jio. Gire uma no menu R."));return 0;}
        int techniqueIndex=indexOf(technique),slot=data.jioAbilitySlot();
        if(PARADISE.equals(technique)){
            if(sujo(data, player)) return useParadiseSujo(player, slot);
            if(slot==0&&ParadiseController.isActive(player)){ParadiseController.end(player);player.sendSystemMessage(Component.literal("The Paradise: a paralisação terminou."));return 1;}
            if(slot==0){if(data.jio()<100){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo: 100 Jio."));return 0;}int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-100,max));ParadiseController.start(player);player.sendSystemMessage(Component.literal("The Paradise: o tempo foi interrompido em um raio de 30 blocos."));return 1;}
            if(!ParadiseController.isActive(player)){player.sendSystemMessage(Component.literal("Ative a Habilidade 1 do The Paradise primeiro."));return 0;}
            int cost=30;if(data.jio()<cost){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo desta habilidade: "+cost+" Jio."));return 0;}int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-cost,max));ParadiseController.useSecondary(player,slot);return 1;
        }
        if(sujo(data, player)) return useSujo(player, techniqueIndex, slot);
        if(KING_OF_LIES.equals(technique)){
            int cost=slot==2?150:30;if(data.jio()<cost){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo: "+cost+" Jio."));return 0;}int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-cost,max));useKingOfLies(player,slot);return 1;
        }
        int cost=cost(techniqueIndex,slot);if(data.jio()<cost){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo: "+cost+" Jio."));return 0;}int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-cost,max));int duration=animationDuration(techniqueIndex,slot);if(duration>0)player.setData(ModAttachments.JIO_ANIMATION,new JioAnimationData(technique,slot,player.level().getGameTime(),duration));if(techniqueIndex==0&&slot==2)player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,4,false,true));else if(techniqueIndex==2&&slot==0){player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,240,2,false,true));player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,240,1,false,true));player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,240,1,false,true));}return 1;
    }

    private static int useSujo(ServerPlayer player,int techniqueIndex,int slot){
        PlayerData data=player.getData(ModAttachments.PLAYER_DATA);
        int cost=30;
        if(data.jio()<cost){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo: "+cost+" Jio."));return 0;}
        int max=ClanSystem.maxJio(player,data);
        player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-cost,max));
        player.getPersistentData().putInt(SUJO_ATTACK,0);
        player.getPersistentData().putInt(SUJO_AUX,0);
        player.getPersistentData().putInt(SUJO_AUX2,0);
        switch(techniqueIndex){
            case 0 -> {
                if(slot==0) { player.getPersistentData().putInt(SUJO_ATTACK,200); player.sendSystemMessage(Component.literal("Seishin dan — Metralhadora de Jio por 10s.")); }
                else if(slot==1) { player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,200,255,false,true)); player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,200,255,false,true)); player.sendSystemMessage(Component.literal("Seishin dan — Imortalidade por 10s.")); }
                else { player.getPersistentData().putInt(SUJO_ATTACK,100); player.getPersistentData().putBoolean(INTANGIBLE_OLD_NO_PHYSICS,player.noPhysics); player.getPersistentData().putBoolean(INTANGIBLE_OLD_INVULNERABLE,player.isInvulnerable()); player.noPhysics=true; player.setInvulnerable(true); player.getAbilities().mayfly=true; player.getAbilities().flying=true; player.onUpdateAbilities(); player.sendSystemMessage(Component.literal("Seishin dan — intangibilidade e voo por 5s.")); }
            }
            case 1 -> {
                if(slot==0) { player.getPersistentData().putInt(SUJO_ATTACK,60); player.sendSystemMessage(Component.literal("Hakai satsu Totetsu: Seimei kui — barragem explosiva.")); }
                else if(slot==1) { player.getPersistentData().putInt(SUJO_AUX,1); player.setDeltaMovement(player.getDeltaMovement().x,1.15D,player.getDeltaMovement().z); player.hurtMarked=true; player.sendSystemMessage(Component.literal("Hakai satsu Totetsu: Seimei kui — salto explosivo preparado.")); }
                else { player.getPersistentData().putInt(SUJO_ATTACK,160); player.sendSystemMessage(Component.literal("Hakai satsu Totetsu: Seimei kui — barragem de 8s, 70 de dano por golpe.")); }
            }
            case 2 -> {
                int duration=animationDuration(2,slot); if(duration>0) player.setData(ModAttachments.JIO_ANIMATION,new JioAnimationData(KATA,slot,player.level().getGameTime(),duration+Math.max(1,duration/10))); player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,Math.max(40,duration+20),1,false,true)); player.sendSystemMessage(Component.literal("Kata kyoka — Estado Sujo: duração e dano aumentados."));
            }
            case 4 -> useKingOfLiesSujo(player,slot);
            default -> { player.getPersistentData().putInt(SUJO_ATTACK,20); }
        }
        return 1;
    }

    private static int useParadiseSujo(ServerPlayer player,int slot){
        PlayerData data=player.getData(ModAttachments.PLAYER_DATA);
        if(slot==0){ if(ParadiseController.isActive(player)){ParadiseController.end(player);return 1;} if(data.jio()<100){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo: 100 Jio."));return 0;} int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-100,max));ParadiseController.startSujo(player);player.sendSystemMessage(Component.literal("The Paradise — Estado Sujo: dimensão espiritual aberta. O mundo parou e a chuva começou."));return 1; }
        if(!ParadiseController.isActive(player)){player.sendSystemMessage(Component.literal("Primeiro abra a dimensão do The Paradise."));return 0;}
        if(data.jio()<30){player.sendSystemMessage(Component.literal("Jio insuficiente. Custo: 30 Jio."));return 0;}
        int max=ClanSystem.maxJio(player,data);player.setData(ModAttachments.PLAYER_DATA,data.withJio(data.jio()-30,max));ParadiseController.useSujoSecondary(player,slot);return 1;
    }

    private static void useKingOfLiesSujo(ServerPlayer player,int slot){
        if(slot==0){player.sendSystemMessage(Component.literal("Você é fraco, patético!"));LivingEntity target=findNearestLivingTarget(player,30.0D);if(target!=null){target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,260,4));target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,260,4));}}
        else if(slot==1){player.sendSystemMessage(Component.literal("Você tem uma chance de ter comido um veneno letal"));LivingEntity target=findAimedLivingTarget(player,64.0D);if(target!=null){target.addEffect(new MobEffectInstance(MobEffects.POISON,100,4));target.addEffect(new MobEffectInstance(KenCraftEffects.ALUCINACAO,100,0));}}
        else {player.sendSystemMessage(Component.literal("Massacre letal"));player.getPersistentData().putInt(SUJO_ATTACK,200);player.getPersistentData().putInt(SUJO_AUX,120);}
    }

    private static void onSujoTick(ServerPlayer player){
        PlayerData data=player.getData(ModAttachments.PLAYER_DATA);if(!data.jioTechnique().equals(PlayerData.normalizeTechnique(data.jioTechnique())))return;
        String technique=PlayerData.normalizeTechnique(data.jioTechnique());int attack=player.getPersistentData().getInt(SUJO_ATTACK);int aux=player.getPersistentData().getInt(SUJO_AUX);
        if(attack>0){
            if(KING_OF_LIES.equals(technique)&&aux>0){ if(player.tickCount%20==0) kingMassacreTick(player); aux--; player.getPersistentData().putInt(SUJO_AUX,aux); }
            else if(SEISHIN.equals(technique)&&data.jioAbilitySlot()==0){ if(player.tickCount%4==0) jioMachineGunTick(player); attack--; player.getPersistentData().putInt(SUJO_ATTACK,attack); }
            else if(SEISHIN.equals(technique)&&data.jioAbilitySlot()==2){ attack--; if(attack<=0) restoreIntangible(player); player.getPersistentData().putInt(SUJO_ATTACK,attack); }
            else if(HAKAI.equals(technique)&&data.jioAbilitySlot()==0){ if(player.tickCount%5==0) explosiveBarrageTick(player); attack--; player.getPersistentData().putInt(SUJO_ATTACK,attack); }
            else if(HAKAI.equals(technique)&&data.jioAbilitySlot()==2){ if(player.tickCount%2==0) hakaiBarrage70(player); attack--; player.getPersistentData().putInt(SUJO_ATTACK,attack); }
            else { attack--; player.getPersistentData().putInt(SUJO_ATTACK,attack); }
        }
        if(player.getPersistentData().getInt(SUJO_AUX2)>0){int t=player.getPersistentData().getInt(SUJO_AUX2)-1;player.getPersistentData().putInt(SUJO_AUX2,t);if(t<=0)restoreIntangible(player);}
        if(HAKAI.equals(technique)&&data.jioAbilitySlot()==1&&player.getPersistentData().getInt(SUJO_AUX)==1&&player.onGround()&&player.getDeltaMovement().y<=0.05D){player.getPersistentData().putInt(SUJO_AUX,0);player.serverLevel().explode(player,player.getX(),player.getY(),player.getZ(),4.0F,true,net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);for(LivingEntity target:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(5),e->e!=player&&e.isAlive())){target.hurt(player.damageSources().playerAttack(player),70.0F);target.setRemainingFireTicks(120);target.setDeltaMovement(target.getDeltaMovement().x,1.0D,target.getDeltaMovement().z);target.hurtMarked=true;}}
    }

    private static void explosiveBarrageTick(ServerPlayer player){for(LivingEntity target:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(4),e->e!=player&&e.isAlive())){player.serverLevel().explode(player,target.getX(),target.getY()+0.7,target.getZ(),1.4F,false,net.minecraft.world.level.Level.ExplosionInteraction.NONE);target.hurt(player.damageSources().playerAttack(player),14.0F);}}
    private static void hakaiBarrage70(ServerPlayer player){LivingEntity target=findNearestLivingTarget(player,6);if(target!=null){target.hurt(player.damageSources().playerAttack(player),70.0F);Vec3 away=target.position().subtract(player.position());if(away.lengthSqr()>0.001)away=away.normalize().scale(1.0);target.setDeltaMovement(away.x,0.45,away.z);target.hurtMarked=true;}}
    private static void jioMachineGunTick(ServerPlayer player){AABB area=player.getBoundingBox().inflate(20);List<LivingEntity> targets=player.level().getEntitiesOfClass(LivingEntity.class,area,e->e!=player&&e.isAlive());if(!targets.isEmpty()){LivingEntity target=targets.get(player.getRandom().nextInt(targets.size()));target.hurt(player.damageSources().playerAttack(player),10.0F);player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,player.getX(),player.getY()+1.4,player.getZ(),3,0.2,0.2,0.2,0.03);}}
    private static void kingMassacreTick(ServerPlayer player){for(LivingEntity target:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(4),e->e!=player&&e.isAlive()))target.hurt(player.damageSources().playerAttack(player),62.0F);player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,25,4,false,true));}
    private static void restoreIntangible(ServerPlayer player){boolean oldNo=player.getPersistentData().getBoolean(INTANGIBLE_OLD_NO_PHYSICS);boolean oldInv=player.getPersistentData().getBoolean(INTANGIBLE_OLD_INVULNERABLE);player.noPhysics=oldNo;player.setInvulnerable(oldInv);player.getAbilities().mayfly=false;player.getAbilities().flying=false;player.onUpdateAbilities();player.getPersistentData().remove(INTANGIBLE_OLD_NO_PHYSICS);player.getPersistentData().remove(INTANGIBLE_OLD_INVULNERABLE);}

    private static void useKingOfLies(ServerPlayer player,int slot){switch(slot){case 0->{player.sendSystemMessage(Component.literal("Você é forte demais"));player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,120,9,false,true));}case 1->{player.sendSystemMessage(Component.literal("Seus órgãos podem explodir"));LivingEntity target=findNearestLivingTarget(player,30.0D);if(target!=null){ServerLevel level=player.serverLevel();level.explode(player,target.getX(),target.getY(),target.getZ(),2.0F,false,net.minecraft.world.level.Level.ExplosionInteraction.NONE);target.kill();}}case 2->{player.sendSystemMessage(Component.literal("Você morreu"));LivingEntity target=findAimedLivingTarget(player,64.0D);if(target!=null)target.kill();}default->{}}}
    private static LivingEntity findNearestLivingTarget(ServerPlayer player,double range){AABB box=player.getBoundingBox().inflate(range);return player.level().getEntitiesOfClass(LivingEntity.class,box,e->e.isAlive()&&e!=player).stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);}
    private static LivingEntity findAimedLivingTarget(ServerPlayer player,double range){Vec3 start=player.getEyePosition(),look=player.getLookAngle().normalize(),end=start.add(look.scale(range));BlockHitResult blockHit=player.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));double maxDistance=start.distanceToSqr(blockHit.getLocation());AABB search=player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0D);LivingEntity best=null;double bestDistance=Double.MAX_VALUE;for(LivingEntity entity:player.level().getEntitiesOfClass(LivingEntity.class,search,target->target.isAlive()&&target!=player)){java.util.Optional<Vec3> hit=entity.getBoundingBox().inflate(0.15D).clip(start,end);if(hit.isEmpty())continue;double distance=start.distanceToSqr(hit.get());if(distance>maxDistance||distance>=bestDistance)continue;best=entity;bestDistance=distance;}return best;}
    private static int charge(CommandSourceStack source){if(!(source.getEntity() instanceof ServerPlayer player))return 0;PlayerData data=player.getData(ModAttachments.PLAYER_DATA);if(!eligible(data))return 0;int max=ClanSystem.maxJio(player,data);if(data.jio()<max)player.setData(ModAttachments.PLAYER_DATA,data.withJio(Math.min(max,data.jio()+2),max));return 1;}
    static int indexOf(String t){for(int i=0;i<TECHNIQUES.length;i++)if(TECHNIQUES[i].equals(t))return i;return -1;}
    static int cost(int t,int s){if(t==0&&s==2)return 50;if(t==1&&s==2)return 100;return 30;}
    static int animationDuration(int t,int s){if(t==0)return switch(s){case 0->14;case 1->80;case 2->100;default->0;};if(t==1)return switch(s){case 0->16;case 1->140;case 2->14;default->0;};if(t==2)return switch(s){case 1->28;case 2->42;default->0;};if(t==3)return switch(s){case 1->12;case 2->200;default->0;};if(t==4)return switch(s){case 0->120;case 1->20;case 2->10;default->0;};return 0;}

    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        SpiritualState state=player.getData(ModAttachments.SPIRITUAL_STATE);
        if(!state.isSujo())return;
        onSujoTick(player);
    }
}
