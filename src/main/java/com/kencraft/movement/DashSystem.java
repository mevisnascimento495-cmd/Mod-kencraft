package com.kencraft.movement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

/** Simple server-authoritative dash. A player needs at least 5 Speed points. */
public final class DashSystem {
    private static final double REQUIRED_SPEED = 5.0D;
    private static final double DASH_STRENGTH = 1.65D;
    private static final int COOLDOWN_TICKS = 12;

    private DashSystem() {}

    public static boolean tryDash(ServerPlayer player, double speedStat) {
        if (speedStat < REQUIRED_SPEED) return false;
        if (player.getCooldowns().isOnCooldown(net.minecraft.world.item.Items.SUGAR)) return false;

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-5D) return false;
        horizontal = horizontal.normalize();

        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(horizontal.x * DASH_STRENGTH, Math.max(motion.y, 0.05D), horizontal.z * DASH_STRENGTH);
        player.hurtMarked = true;
        player.getCooldowns().addCooldown(net.minecraft.world.item.Items.SUGAR, COOLDOWN_TICKS);
        return true;
    }
}
