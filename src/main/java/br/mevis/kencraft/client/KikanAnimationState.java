package br.mevis.kencraft.client;

/** Dependency-free Kikan animation state with distinct Z/C attack choreography. */
public final class KikanAnimationState {
    private static int ticks;
    private static int duration;
    private static String key = "z";

    private KikanAnimationState() {}

    public static void trigger(String attackKey) {
        key = attackKey == null ? "z" : attackKey.toLowerCase();
        duration = 24;
        ticks = duration;
    }

    public static void tick() {
        if (ticks > 0) ticks--;
    }

    public static boolean active() {
        return ticks > 0;
    }

    public static float progress() {
        return !active() || duration <= 0 ? 0.0F : 1.0F - (ticks / (float) duration);
    }

    /** Z is a fast strike; C is a heavier/longer crowd-control motion. */
    public static boolean isHeavy() {
        return "c".equals(key);
    }

    public static String key() {
        return key;
    }

    /** Peaks at the moment where the Kikan should visually meet the target. */
    public static float impactEnvelope() {
        float p = progress();
        if (!active()) return 0.0F;
        if (p < 0.52F) return p / 0.52F;
        return Math.max(0.0F, (1.0F - p) / 0.48F);
    }
}
