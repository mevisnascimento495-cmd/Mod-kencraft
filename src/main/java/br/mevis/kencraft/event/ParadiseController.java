package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ParadiseController {
    private static final int RADIUS = 30;
    private static final int SUFOCO_DURATION = 180;
    private static final int ARROW_DURATION = 200;
    private static final int ARROW_INTERVAL = 10;
    private static final float BASE_ARROW_DAMAGE = 7.0F;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private ParadiseController() {}
    public static boolean isActive(ServerPlayer player) { return SESSIONS.containsKey(player.getUUID()); }

    public static void start(ServerPlayer player) { startInternal(player,false); }
    public static void startSujo(ServerPlayer player) { startInternal(player,true); }
    private static void startInternal(ServerPlayer player, boolean sujo) {
        end(player);
        Session session = new Session(player.serverLevel().dimension(), new ArrayList<>(), sujo);
        AABB box = player.getBoundingBox().inflate(RADIUS);
        for (Entity entity : player.serverLevel().getEntities(player, box, entity -> entity.isAlive() && entity != player)) session.targets.add(new Target(entity.getUUID(), entity.position()));
        SESSIONS.put(player.getUUID(), session);
    }

    public static void useSecondary(ServerPlayer player, int slot) {
        useSecondaryInternal(player,slot,false);
    }
    public static void useSujoSecondary(ServerPlayer player, int slot) {
        useSecondaryInternal(player,slot,true);
    }
    private static void useSecondaryInternal(ServerPlayer player,int slot,boolean sujo) {
        Session session = SESSIONS.get(player.getUUID());
        if(session==null)return;
        ServerLevel level=player.serverLevel();
        if(!level.dimension().equals(session.dimension)){end(player);return;}
        if(sujo && !session.sujo)return;
        if(slot==1){
            if(sujo){
                for(Target target:session.targets){Entity entity=level.getEntity(target.uuid);if(entity instanceof LivingEntity living&&living.isAlive())living.addEffect(new MobEffectInstance(KenCraftEffects.SUFOCO,160,0,false,true,true));}
                return;
            }
            Target target=nearestTarget(player,session,level);if(target==null)return;Entity entity=level.getEntity(target.uuid);if(entity instanceof LivingEntity living&&living.isAlive())living.addEffect(new MobEffectInstance(KenCraftEffects.SUFOCO,SUFOCO_DURATION,0,false,true,true));return;
        }
        if(slot==2){
            if(sujo){session.sujoExplosionTicks=1;return;}
            session.arrowTicksRemaining=ARROW_DURATION;
        }
    }

    private static Target nearestTarget(ServerPlayer player,Session session,ServerLevel level){Target best=null;double bestDistance=Double.MAX_VALUE;for(Target target:session.targets){Entity entity=level.getEntity(target.uuid);if(!(entity instanceof LivingEntity living)||!living.isAlive())continue;double distance=player.distanceToSqr(living);if(distance<bestDistance){bestDistance=distance;best=target;}}return best;}
    public static void end(ServerPlayer player){SESSIONS.remove(player.getUUID());}
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer player)end(player);}

    @SubscribeEvent public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer controller))return;
        Session session=SESSIONS.get(controller.getUUID());if(session==null)return;
        if(controller.isDeadOrDying()||!controller.isAlive()){end(controller);return;}
        if(controller.serverLevel().dimension()!=session.dimension){end(controller);return;}
        ServerLevel level=controller.serverLevel();
        for(Target target:session.targets){Entity entity=level.getEntity(target.uuid);if(entity==null||!entity.isAlive())continue;entity.teleportTo(target.position.x,target.position.y,target.position.z);entity.setDeltaMovement(Vec3.ZERO);}
        if(session.sujo){
            // The Sujo state uses an isolated session in the current dimension rather than registering a permanent world dimension.
            // This keeps the effect local and prevents a global time-stop/weather bug.
            for(int i=0;i<10;i++){double x=controller.getX()-RADIUS+controller.getRandom().nextDouble()*RADIUS*2;double z=controller.getZ()-RADIUS+controller.getRandom().nextDouble()*RADIUS*2;level.sendParticles(ParticleTypes.RAIN,x,controller.getY()+10+controller.getRandom().nextDouble()*8,z,1,0,0,0,0);}
            if(session.sujoExplosionTicks>0){
                session.sujoExplosionTicks=0;
                for(Target target:session.targets){Entity entity=level.getEntity(target.uuid);if(entity instanceof LivingEntity living&&living.isAlive())living.hurt(controller.damageSources().playerAttack(controller),89.0F);}
            }
            return;
        }
        if(session.arrowTicksRemaining>0){if(session.arrowTicksRemaining%ARROW_INTERVAL==0)spawnArrowRain(controller,session,level);session.arrowTicksRemaining--;}
    }

    private static void spawnArrowRain(ServerPlayer player,Session session,ServerLevel level){
        boolean kirisai="KIRISAI".equals(player.getData(br.mevis.kencraft.data.ModAttachments.CLAN_DATA).clan());
        double damage=BASE_ARROW_DAMAGE*(kirisai?1.10D:1.0D);
        for(Target target:session.targets){Entity entity=level.getEntity(target.uuid);if(!(entity instanceof LivingEntity living)||!living.isAlive())continue;for(int i=0;i<3;i++){if(!(EntityType.ARROW.create(level) instanceof Arrow arrow))continue;arrow.setPos(living.getX()-1.0D+player.getRandom().nextDouble()*2.0D,living.getY()+8.0D+player.getRandom().nextDouble()*2.0D,living.getZ()-1.0D+player.getRandom().nextDouble()*2.0D);arrow.setBaseDamage(damage);arrow.setDeltaMovement(0.0D,-0.75D,0.0D);level.addFreshEntity(arrow);}}
    }
    private static final class Session {private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;private final List<Target> targets;private final boolean sujo;private int arrowTicksRemaining;private int sujoExplosionTicks;private Session(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,List<Target> targets,boolean sujo){this.dimension=dimension;this.targets=targets;this.sujo=sujo;}}
    private record Target(UUID uuid,Vec3 position){}
}
