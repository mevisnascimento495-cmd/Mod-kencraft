package br.mevis.kencraft.client;

/** Dependency-free Kikan animation state with attack-specific timing. */
public final class KikanAnimationState {
    private static int ticks;
    private static int duration;
    private static String key = "z";

    private KikanAnimationState() {}

    public static void trigger(String attackKey) {
        key = attackKey == null ? "z" : attackKey.toLowerCase(java.util.Locale.ROOT);
        duration = "c".equals(key) ? 32 : 20;
        ticks = duration;
    }

    public static void tick() { if (ticks > 0) ticks--; }
    public static boolean active() { return ticks > 0 && duration > 0; }
    public static float progress() { return !active() ? 0.0F : 1.0F - (ticks / (float)duration); }
    public static boolean isHeavy() { return "c".equals(key); }
    public static String key() { return key; }

    /** Peaks at the visible contact moment without changing the existing Kikan model choreography. */
    public static float impactEnvelope() {
        if (!active()) return 0.0F;
        float p = progress();
        float center = isHeavy() ? 0.54F : 0.50F;
        float halfWidth = isHeavy() ? 0.24F : 0.26F;
        return Math.max(0.0F, 1.0F - Math.abs(p - center) / halfWidth);
    }
}
