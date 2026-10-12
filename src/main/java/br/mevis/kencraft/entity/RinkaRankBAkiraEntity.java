package br.mevis.kencraft.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Rank B Rinka Akira: the final ARF mission opponent with a crocodile-tail Kikan. */
public class RinkaRankBAkiraEntity extends RinkaEntity {
    private static final EntityDataAccessor<Integer> KIKAN_ANIMATION_TICKS =
            SynchedEntityData.defineId(RinkaRankBAkiraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> KIKAN_ATTACK_KEY =
            SynchedEntityData.defineId(RinkaRankBAkiraEntity.class, EntityDataSerializers.STRING);

    private int zAttackCooldown;
    private int cAttackCooldown;
    private int specialAttackLock;
    private boolean nextAttackIsZ = true;

    public RinkaRankBAkiraEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setCustomName(net.minecraft.network.chat.Component.literal("Rinka Rank B — Akira"));
        setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 222.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.FOLLOW_RANGE, 34.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.47D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIKAN_ANIMATION_TICKS, 0);
        builder.define(KIKAN_ATTACK_KEY, "z");
    }

    public int getKikanAnimationTicks() {
        return entityData.get(KIKAN_ANIMATION_TICKS);
    }

    public float getKikanAnimationProgress(float partialTick) {
        return Math.max(0.0F, Math.min(1.0F, (getKikanAnimationTicks() - partialTick) / 12.0F));
    }

    public String getKikanAttackKey() {
        return entityData.get(KIKAN_ATTACK_KEY);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        int animationTicks = getKikanAnimationTicks();
        if (animationTicks > 0) entityData.set(KIKAN_ANIMATION_TICKS, animationTicks - 1);
        if (zAttackCooldown > 0) zAttackCooldown--;
        if (cAttackCooldown > 0) cAttackCooldown--;
        if (specialAttackLock > 0) specialAttackLock--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || target.isRemoved() || !hasLineOfSight(target)) return;
        if (specialAttackLock > 0 || distanceToSqr(target) > 20.25D) return;

        // Use both existing CROCODILE_TAIL techniques, alternating Z and C.
        if (nextAttackIsZ && zAttackCooldown <= 0) {
            useCrocodileTailAttack(target, "z");
            zAttackCooldown = 40;
            specialAttackLock = 15;
            nextAttackIsZ = false;
        } else if (!nextAttackIsZ && cAttackCooldown <= 0) {
            useCrocodileTailAttack(target, "c");
            cAttackCooldown = 55;
            specialAttackLock = 15;
            nextAttackIsZ = true;
        } else if (zAttackCooldown <= 0) {
            useCrocodileTailAttack(target, "z");
            zAttackCooldown = 40;
            specialAttackLock = 15;
            nextAttackIsZ = false;
        } else if (cAttackCooldown <= 0) {
            useCrocodileTailAttack(target, "c");
            cAttackCooldown = 55;
            specialAttackLock = 15;
            nextAttackIsZ = true;
        }
    }

    private void useCrocodileTailAttack(LivingEntity target, String key) {
        entityData.set(KIKAN_ATTACK_KEY, key);
        entityData.set(KIKAN_ANIMATION_TICKS, 12);
        target.hurt(damageSources().mobAttack(this), "c".equals(key) ? 9.0F : 6.0F);
        target.setDeltaMovement(target.getDeltaMovement().add(getLookAngle().scale(0.45D)));
        target.hurtMarked = true;

        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(),
                    "c".equals(key) ? 10 : 6, 0.25D, 0.4D, 0.25D, 0.03D);
            server.playSound(null, blockPosition(),
                    "c".equals(key) ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_WEAK,
                    SoundSource.HOSTILE, 1.0F, "c".equals(key) ? 0.8F : 1.0F);
        }
    }
}
