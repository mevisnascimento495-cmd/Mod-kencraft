package br.mevis.kencraft.client;

/** Lightweight client-only Jio animation controller. No Player Animator dependency. */
public final class JioAnimationState {
    private static int ticks;
    private static String technique = "NONE";
    private static int ability;

    private JioAnimationState() {}

    public static void trigger(String currentTechnique, int currentAbility) {
        technique = currentTechnique == null ? "NONE" : currentTechnique;
        ability = Math.max(0, Math.min(2, currentAbility));
        ticks = durationFor(technique, ability);
    }

    public static void tick() {
        if (ticks > 0) ticks--;
    }

    public static boolean active() { return ticks > 0; }

    public static float progress() {
        int duration = durationFor(technique, ability);
        return ticks <= 0 ? 0.0F : 1.0F - (ticks / (float) duration);
    }

    public static String technique() { return technique; }
    public static int ability() { return ability; }

    private static int durationFor(String technique, int ability) {
        if (technique == null) return 12;
        if (technique.equalsIgnoreCase("Seishin dan") && ability == 1) return 18;
        if (technique.equalsIgnoreCase("Seishin dan") && ability == 2) return 20;
        if (technique.equalsIgnoreCase("Hakai satsu Totetsu: Seimei kui") && ability == 1) return 18;
        if (technique.equalsIgnoreCase("Hakai satsu Totetsu: Seimei kui") && ability == 2) return 28;
        if (technique.equalsIgnoreCase("Kata kyoka") && ability == 2) return 22;
        return 14;
    }
}
