package br.mevis.kencraft.client;

/** Dependency-free, deterministic client combat animation state for human Jio techniques. */
public final class JioAnimationState {
    private static int ticks;
    private static int duration;
    private static String technique = "NONE";
    private static int ability;

    private JioAnimationState() {}

    public static void trigger(String currentTechnique, int currentAbility) {
        technique = currentTechnique == null ? "NONE" : currentTechnique;
        ability = Math.max(0, Math.min(2, currentAbility));
        duration = durationFor(technique, ability);
        ticks = duration;
    }

    public static void tick() {
        if (ticks > 0) ticks--;
    }

    public static boolean active() { return ticks > 0 && duration > 0; }

    /** 0..1 forward progress. */
    public static float progress() {
        if (!active()) return 0.0F;
        return 1.0F - (ticks / (float) duration);
    }

    /** 0..1 attack envelope, peaking around the impact/contact moment. */
    public static float impactEnvelope() {
        float p = progress();
        if (!active()) return 0.0F;
        if (p < 0.5F) return p * 2.0F;
        return (1.0F - p) * 2.0F;
    }

    public static String technique() { return technique; }
    public static int ability() { return ability; }
    public static int duration() { return duration; }

    private static int durationFor(String technique, int ability) {
        if (technique == null) return 0;
        if (technique.equalsIgnoreCase("Seishin dan")) {
            return switch (ability) {
                case 0 -> 14;   // basic shot
                case 1 -> 80;   // ~4 seconds channel
                case 2 -> 100;  // ~5 seconds protection
                default -> 0;
            };
        }
        if (technique.equalsIgnoreCase("Hakai satsu Totetsu: Seimei kui")) {
            return switch (ability) {
                case 0 -> 16;   // explosive punch
                case 1 -> 140;  // ~7 seconds barrage
                case 2 -> 14;   // single finishing punch
                default -> 0;
            };
        }
        if (technique.equalsIgnoreCase("Kata kyoka")) {
            return switch (ability) {
                case 0 -> 0;    // reinforcement itself has no attack animation
                case 1 -> 28;   // grab / hold
                case 2 -> 42;   // multi-hit combo
                default -> 0;
            };
        }
        return 0;
    }
}
