package br.mevis.kencraft.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Mitso, the Rishin leader who confronts the investigator after the coven escape. */
public final class MitsoEntity extends RinkaEntity {
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            net.minecraft.network.chat.Component.literal("Mitso"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    public MitsoEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setCustomName(net.minecraft.network.chat.Component.literal("Mitso"));
        setCustomNameVisible(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !(level() instanceof ServerLevel server)) return;
        bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
        java.util.Set<ServerPlayer> nearby = new java.util.HashSet<>();
        for (ServerPlayer player : server.players()) {
            if (player.distanceToSqr(this) <= 64.0D * 64.0D) {
                nearby.add(player);
                bossEvent.addPlayer(player);
            }
        }
        for (ServerPlayer tracked : new java.util.ArrayList<>(bossEvent.getPlayers())) {
            if (!nearby.contains(tracked)) bossEvent.removePlayer(tracked);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        bossEvent.removeAllPlayers();
        super.remove(reason);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 140.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ARMOR, 9.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.40D);
    }
}
