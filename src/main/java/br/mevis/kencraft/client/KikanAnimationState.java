package br.mevis.kencraft.client;

/** Lightweight client-only animation state. It does not depend on Player Animator. */
public final class KikanAnimationState {
    private static int ticks;
    private static String key = "z";

    private KikanAnimationState() {}

    public static void trigger(String attackKey) {
        key = attackKey;
        ticks = 12;
    }

    public static void tick() {
        if (ticks > 0) ticks--;
    }

    public static boolean active() {
        return ticks > 0;
    }

    public static float progress() {
        return ticks <= 0 ? 0.0F : 1.0F - (ticks / 12.0F);
    }

    public static boolean isHeavy() {
        return "c".equals(key);
    }
}
