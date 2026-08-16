package br.mevis.kencraft.entity;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.event.KikakogouProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class AodaiEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> SNAKE_ACTIVE =
            SynchedEntityData.defineId(AodaiEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String DIALOGUE = "Ora, ora! Parece que um fracote veio me perguntar como desbloquear a Kikakogou. Bem, pelo que me disseram, a Kikakogou é liberada depois que o Rinka devorar 50 Jinsuikaku normais e 30 Jinsuikaku Rank C. Depois disso, o Rinka terá que derrotar alguém forte como eu... Hahaha! Mas acho que você não conseguiria me derrotar, não é? Hahaha! Patético.";

    public AodaiEntity(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); setPersistenceRequired(); }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(SNAKE_ACTIVE, false); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.85D));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide) {
            LivingEntity attacker = getLastHurtByMob();
            boolean active = attacker != null && attacker.isAlive() && !attacker.isRemoved();
            if (entityData.get(SNAKE_ACTIVE) != active) entityData.set(SNAKE_ACTIVE, active);
        }
    }
    public boolean isSnakeActive() { return entityData.get(SNAKE_ACTIVE); }

    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (player.getData(ModAttachments.PLAYER_DATA).race() != Race.RINKA) {
            player.sendSystemMessage(Component.literal("Aodai apenas ensina sobre a Kikakogou para um Rinka."));
            return InteractionResult.CONSUME;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (KikakogouProgress.isUnlocked(serverPlayer)) {
            player.sendSystemMessage(Component.literal("Você já desbloqueou a Kikakogou."));
        } else if (!KikakogouProgress.isMissionStarted(serverPlayer)) {
            KikakogouProgress.startMission(serverPlayer);
            player.sendSystemMessage(Component.literal(DIALOGUE));
        } else {
            player.sendSystemMessage(Component.literal("A missão da Kikakogou continua: 50 Jinsuikaku, 30 Jinsuikaku Rank C e derrotar Aodai."));
        }
        return InteractionResult.CONSUME;
    }

    @Override public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (!level().isClientSide) {
            LivingEntity killer = source.getEntity() instanceof LivingEntity living ? living : getLastHurtByMob();
            if (killer instanceof ServerPlayer player) KikakogouProgress.markAodaiDefeated(player);
        }
        super.die(source);
    }
}
