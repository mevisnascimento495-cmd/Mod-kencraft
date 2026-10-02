package br.mevis.kencraft.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RinkaHungryEntity extends RinkaEntity {
    private static final String GRAB_TARGET = "kencraft_grab_target";
    private static final String GRAB_TICKS = "kencraft_grab_ticks";
    private int attackAnimationTicks;
    private String attackKey = "z";
    private boolean kikanActive;

    public RinkaHungryEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setCustomName(net.minecraft.network.chat.Component.literal("Rinka Hungry"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.12D, true));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isKikanActive() { return kikanActive; }
    public String getKikanType() { return "SCORPION_TAIL"; }
    public float getKikanAnimationProgress(float partialTick) {
        return Math.max(0.0F, Math.min(1.0F, (attackAnimationTicks - partialTick) / 12.0F));
    }
    public String getKikanAttackKey() { return attackKey; }

    @Override
    public void tick() {
        super.tick();
        if (attackAnimationTicks > 0) attackAnimationTicks--;
        if (!level().isClientSide) {
            updateGrab();
            if (getTarget() != null && getTarget().isAlive()) {
                kikanActive = true;
                if (tickCount % 30 == 0) useScorpionSting(getTarget());
                if (tickCount % 70 == 0) useScorpionHallucination(getTarget());
                if (tickCount % 100 == 0) useGrabPunch(getTarget());
            } else if (tickCount % 40 == 0) {
                kikanActive = false;
            }
        }
    }

    private void activateKikan(String key) {
        kikanActive = true;
        attackKey = key;
        attackAnimationTicks = 12;
        if (level() instanceof net.minecraft.server.level.ServerLevel server)
            server.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.0D, getZ(), 12, 0.45D, 0.7D, 0.45D, 0.06D);
    }

    private void useScorpionSting(net.minecraft.world.entity.LivingEntity target) {
        if (distanceToSqr(target) > 30.0D) return;
        activateKikan("z");
        target.hurt(damageSources().mobAttack(this), 8.0F);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
        target.setDeltaMovement(target.getDeltaMovement().x, 0.45D, target.getDeltaMovement().z);
        target.hurtMarked = true;
    }

    private void useScorpionHallucination(net.minecraft.world.entity.LivingEntity target) {
        if (distanceToSqr(target) > 100.0D) return;
        activateKikan("c");
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
        if (level() instanceof net.minecraft.server.level.ServerLevel server)
            server.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0D, target.getZ(), 16, 0.45D, 0.6D, 0.45D, 0.04D);
    }

    private void useGrabPunch(net.minecraft.world.entity.LivingEntity target) {
        if (distanceToSqr(target) > 14.0D) return;
        activateKikan("c");
        getPersistentData().putInt(GRAB_TARGET, target.getId());
        getPersistentData().putInt(GRAB_TICKS, 10);
        target.hurt(damageSources().mobAttack(this), 6.0F);
    }

    private void updateGrab() {
        int ticks = getPersistentData().getInt(GRAB_TICKS);
        if (ticks <= 0) return;
        int id = getPersistentData().getInt(GRAB_TARGET);
        net.minecraft.world.entity.Entity entity = level().getEntity(id);
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity target) || !target.isAlive() || distanceToSqr(target) > 25.0D) {
            getPersistentData().putInt(GRAB_TICKS, 0);
            return;
        }
        getPersistentData().putInt(GRAB_TICKS, ticks - 1);
        Vec3 hold = position().add(getLookAngle().scale(1.55D)).add(0.0D, 0.45D, 0.0D);
        target.teleportTo(hold.x, hold.y, hold.z);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        if (level() instanceof net.minecraft.server.level.ServerLevel server)
            server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 2, 0.12D, 0.18D, 0.12D, 0.02D);
        if (ticks == 1) {
            target.hurt(damageSources().mobAttack(this), 10.0F);
            target.setDeltaMovement(getLookAngle().scale(0.8D).add(0, 0.25D, 0));
            target.hurtMarked = true;
            getPersistentData().putInt(GRAB_TICKS, 0);
        }
    }
}
