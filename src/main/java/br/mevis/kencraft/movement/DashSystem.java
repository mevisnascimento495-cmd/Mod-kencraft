package br.mevis.kencraft.movement;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative dash. Requires Speed >= 5. */
public final class DashSystem {
    public static final int REQUIRED_SPEED = 5;
    private static final double DASH_STRENGTH = 1.65D;
    private static final int COOLDOWN_TICKS = 12;

    private DashSystem() {}

    public static boolean tryDash(ServerPlayer player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.speed() < REQUIRED_SPEED) return false;

        long now = player.level().getGameTime();
        long last = player.getPersistentData().getLong("kencraft_last_dash");
        if (now - last < COOLDOWN_TICKS) return false;

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-5D) return false;
        horizontal = horizontal.normalize();

        Vec3 current = player.getDeltaMovement();
        player.setDeltaMovement(horizontal.scale(DASH_STRENGTH).add(0.0D, Math.max(current.y, 0.05D), 0.0D));
        player.hurtMarked = true;
        player.getPersistentData().putLong("kencraft_last_dash", now);
        return true;
    }
}
