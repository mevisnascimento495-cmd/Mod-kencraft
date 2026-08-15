package br.mevis.kencraft.event;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.JioAnimationData;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class JioCombatTicker {
    private JioCombatTicker() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;
        JioAnimationData animation = player.getData(ModAttachments.JIO_ANIMATION);
        if (!animation.activeAt(player.level().getGameTime())) return;
        long elapsed = player.level().getGameTime() - animation.startTick();
        int technique = JioSystem.indexOf(PlayerData.normalizeTechnique(animation.technique()));
        int ability = animation.ability();
        if (technique == 0) seishin(player, ability, elapsed);
        else if (technique == 1) hakai(player, ability, elapsed);
        else if (technique == 2) kata(player, ability, elapsed);
    }

    private static void seishin(ServerPlayer player, int ability, long elapsed) {
        if (ability == 0 && elapsed == 7) {
            LivingEntity target = target(player, 24.0D);
            if (target != null) { target.hurt(player.damageSources().magic(), spiritualDamage(player, 5.0F, 1.5F)); beamImpact(player, target); }
        } else if (ability == 1 && elapsed >= 8 && elapsed <= 64 && elapsed % 8 == 0) {
            LivingEntity target = target(player, 24.0D);
            if (target != null) { target.hurt(player.damageSources().magic(), spiritualDamage(player, 3.0F, 0.65F)); beamImpact(player, target); }
        }
    }

    private static float spiritualDamage(ServerPlayer player, float base, float scale) {
        return base + player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment() * scale;
    }

    private static void hakai(ServerPlayer player, int ability, long elapsed) {
        if (ability == 0 && elapsed == 8) {
            LivingEntity target = target(player, 5.0D);
            if (target != null) { target.hurt(player.damageSources().playerAttack(player), 8.0F); target.setDeltaMovement(target.getDeltaMovement().add(player.getLookAngle().scale(1.4D))); impactExplosion(target); }
        } else if (ability == 1 && elapsed >= 10 && elapsed <= 136 && (elapsed - 10) % 14 == 0) {
            LivingEntity target = target(player, 5.0D);
            if (target != null) { target.hurt(player.damageSources().playerAttack(player), 3.5F + player.getData(ModAttachments.PLAYER_DATA).spiritualDevelopment() * 0.5F); target.setRemainingFireTicks(Math.max(100, target.getRemainingFireTicks())); punchImpact(target); }
        } else if (ability == 2 && elapsed == 7) {
            LivingEntity target = target(player, 5.0D);
            if (target != null) { target.hurt(player.damageSources().playerAttack(player), 70.0F); target.setDeltaMovement(target.getDeltaMovement().add(player.getLookAngle().scale(0.75D))); punchImpact(target); }
        }
    }

    private static void kata(ServerPlayer player, int ability, long elapsed) {
        if (ability == 1 && elapsed == 7) {
            LivingEntity target = target(player, 4.0D);
            if (target != null) { target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 255, false, false)); target.setDeltaMovement(Vec3.ZERO); pullIntoGrabPosition(player, target); punchImpact(target); }
        } else if (ability == 2 && elapsed >= 8 && elapsed <= 38 && (elapsed - 8) % 6 == 0) {
            LivingEntity target = target(player, 4.0D);
            if (target != null) { target.hurt(player.damageSources().playerAttack(player), 3.0F); target.setDeltaMovement(target.getDeltaMovement().multiply(0.55D, 1.0D, 0.55D)); punchImpact(target); }
        }
    }

    private static void beamImpact(ServerPlayer player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 start = player.getEyePosition(), end = target.getBoundingBox().getCenter(), step = end.subtract(start).scale(1.0D / 8.0D);
        for (int i = 0; i <= 8; i++) { Vec3 p = start.add(step.scale(i)); level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.04D, 0.04D, 0.04D, 0.0D); }
        level.sendParticles(ParticleTypes.END_ROD, end.x, end.y, end.z, 8, 0.25D, 0.25D, 0.25D, 0.02D);
    }

    private static void impactExplosion(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        Vec3 p = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        level.sendParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 8, 0.35D, 0.35D, 0.35D, 0.0D);
        level.sendParticles(ParticleTypes.POOF, p.x, p.y, p.z, 16, 0.4D, 0.4D, 0.4D, 0.04D);
    }

    private static void punchImpact(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level)) return;
        Vec3 p = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 6, 0.2D, 0.3D, 0.2D, 0.05D);
    }

    private static void pullIntoGrabPosition(ServerPlayer player, LivingEntity target) {
        Vec3 desired = player.getEyePosition().add(player.getLookAngle().scale(1.15D));
        target.teleportTo(desired.x, target.getY(), desired.z);
    }

    private static LivingEntity target(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition(), end = eye.add(player.getLookAngle().scale(range));
        var box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0D);
        LivingEntity best = null; double bestDistance = range * range;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
            var hit = entity.getBoundingBox().inflate(0.3D).clip(eye, end);
            if (hit.isPresent()) { double distance = eye.distanceToSqr(hit.get()); if (distance < bestDistance) { bestDistance = distance; best = entity; } }
        }
        return best;
    }
}
