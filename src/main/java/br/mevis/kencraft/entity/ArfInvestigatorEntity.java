package br.mevis.kencraft.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class ArfInvestigatorEntity extends PathfinderMob {
    public ArfInvestigatorEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 3.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // ARF hunts Rinkas but remains neutral toward players until provoked.
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, RinkaEntity.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        LivingEntity target = getTarget();
        if (target == null) return;

        if (!target.isAlive() || target.isRemoved()) {
            setTarget(null);
            return;
        }

        double followRange = getAttributeValue(Attributes.FOLLOW_RANGE);
        if (distanceToSqr(target) > followRange * followRange * 2.25D) {
            setTarget(null);
            return;
        }

        // Investigators alert nearby ARF members when they find a Rinka.
        if (target instanceof RinkaEntity && tickCount % 10 == 0) {
            for (ArfInvestigatorEntity ally : level().getEntitiesOfClass(
                    ArfInvestigatorEntity.class, getBoundingBox().inflate(12.0D),
                    entity -> entity != this && entity.isAlive())) {
                ally.setTarget(target);
            }
            for (ArfGeneralEntity general : level().getEntitiesOfClass(
                    ArfGeneralEntity.class, getBoundingBox().inflate(16.0D),
                    entity -> entity != this && entity.isAlive())) {
                general.setTarget(target);
            }
        }
    }
}
