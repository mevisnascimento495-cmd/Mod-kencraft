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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class RinkaEntity extends Monster {
    public RinkaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Rinkas naturally hunt both players and ARF investigators.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, ArfInvestigatorEntity.class, true));
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

        // Nearby Rinkas join an active hunt instead of behaving as isolated mobs.
        if (tickCount % 10 == 0) {
            for (RinkaEntity ally : level().getEntitiesOfClass(
                    RinkaEntity.class, getBoundingBox().inflate(12.0D),
                    entity -> entity != this && entity.isAlive())) {
                ally.setTarget(target);
            }
        }
    }
}
