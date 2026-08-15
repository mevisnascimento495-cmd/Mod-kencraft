package br.mevis.kencraft.client;

import br.mevis.kencraft.data.JioAnimationData;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.player.Player;

/** Keyframe-style evaluator for the server-replicated human Jio animations. */
public final class JioPlayerAnimator {
    private JioPlayerAnimator() {}

    public static void apply(PlayerModel<?> model, Player player, JioAnimationData state, long gameTime) {
        if (model == null || player == null || state == null || !state.activeAt(gameTime)) return;
        float p = state.progressAt(gameTime);
        float hit = state.impactEnvelopeAt(gameTime);
        int ability = Math.max(0, Math.min(2, state.ability()));
        String technique = state.technique();

        if ("Seishin dan".equalsIgnoreCase(technique)) applySeishin(model, ability, p, hit);
        else if ("Hakai satsu Totetsu: Seimei kui".equalsIgnoreCase(technique)) applyHakai(model, ability, p, hit);
        else if ("Kata kyoka".equalsIgnoreCase(technique)) applyKata(model, ability, p, hit);
    }

    private static void applySeishin(PlayerModel<?> m, int a, float p, float hit) {
        switch (a) {
            case 0 -> {
                // Raise -> aim -> fire -> recoil. The impact pulse is deliberately brief.
                float raise = easeOut(segment(p, 0.00F, 0.30F));
                float aim = easeInOut(segment(p, 0.24F, 0.58F));
                float fire = easeOut(segment(p, 0.52F, 0.70F));
                float recoil = easeOut(segment(p, 0.70F, 1.00F));
                m.rightArm.xRot += -1.15F * raise - 1.05F * aim + 0.34F * recoil - 0.18F * fire;
                m.rightArm.yRot += -0.16F * aim;
                m.rightArm.zRot += 0.07F * raise;
                m.body.yRot += 0.11F * aim + 0.05F * hit;
            }
            case 1 -> {
                // Both arms stay raised for the channel; only the wrists/torso pulse between shots.
                float raise = easeOut(segment(p, 0.00F, 0.14F));
                float hold = 1.0F - easeIn(segment(p, 0.92F, 1.00F));
                float pulse = (float)Math.sin(p * Math.PI * 16.0F) * 0.10F * raise * hold;
                m.rightArm.xRot += -1.30F * raise - pulse;
                m.leftArm.xRot += -1.30F * raise + pulse;
                m.rightArm.zRot += 0.10F * raise;
                m.leftArm.zRot -= 0.10F * raise;
                m.body.yRot += 0.055F * pulse + 0.045F * hit;
            }
            case 2 -> {
                // Spiritual protection: restrained stance instead of an attack pose.
                float settle = easeOut(segment(p, 0.00F, 0.18F));
                float breathe = (float)Math.sin(p * Math.PI * 2.0F);
                m.rightArm.xRot += -0.16F * settle;
                m.leftArm.xRot += -0.16F * settle;
                m.rightArm.zRot += 0.035F * breathe * settle;
                m.leftArm.zRot -= 0.035F * breathe * settle;
                m.body.yRot += 0.025F * breathe;
            }
            default -> { }
        }
    }

    private static void applyHakai(PlayerModel<?> m, int a, float p, float hit) {
        switch (a) {
            case 0 -> {
                // Wind-up -> committed punch -> contact -> recovery.
                float windup = 1.0F - easeIn(segment(p, 0.00F, 0.28F));
                float strike = easeInOut(segment(p, 0.24F, 0.58F));
                float recoil = easeOut(segment(p, 0.66F, 1.00F));
                m.rightArm.xRot += 0.82F * windup - 2.00F * strike + 0.62F * recoil;
                m.rightArm.yRot += -0.14F * windup + 0.04F * hit;
                m.body.yRot += -0.18F * windup + 0.26F * hit;
                m.body.xRot += -0.10F * strike + 0.05F * recoil;
            }
            case 1 -> {
                // Alternating left/right punches with a readable shoulder/body follow-through.
                float envelope = easeOut(segment(p, 0.00F, 0.08F)) * (1.0F - easeIn(segment(p, 0.94F, 1.00F)));
                float combo = (float)Math.sin(p * Math.PI * 14.0F) * envelope;
                float side = (float)Math.sin(p * Math.PI * 7.0F);
                m.rightArm.xRot += -0.74F - 0.98F * combo;
                m.leftArm.xRot += -0.74F + 0.98F * combo;
                m.rightArm.yRot += -0.22F * Math.max(0.0F, combo);
                m.leftArm.yRot += 0.22F * Math.max(0.0F, -combo);
                m.rightArm.zRot += 0.10F * combo;
                m.leftArm.zRot -= 0.10F * combo;
                m.body.yRot += 0.14F * side + 0.05F * hit;
                m.body.xRot += -0.035F * Math.abs(combo);
            }
            case 2 -> {
                // Heavy finisher: the whole upper body commits to one simple punch.
                float windup = 1.0F - easeIn(segment(p, 0.00F, 0.30F));
                float strike = easeInOut(segment(p, 0.24F, 0.68F));
                float recoil = easeOut(segment(p, 0.78F, 1.00F));
                m.rightArm.xRot += 0.88F * windup - 2.12F * strike + 0.58F * recoil;
                m.rightArm.yRot += -0.10F * windup;
                m.body.xRot += -0.12F * strike + 0.04F * recoil;
                m.body.yRot += 0.24F * hit;
            }
            default -> { }
        }
    }

    private static void applyKata(PlayerModel<?> m, int a, float p, float hit) {
        switch (a) {
            case 0 -> { }
            case 1 -> {
                // Reach -> close both hands -> hold. The hold remains stable until recovery.
                float reach = easeOut(segment(p, 0.00F, 0.32F));
                float hold = p >= 0.32F && p < 0.78F ? 1.0F : reach;
                float release = easeOut(segment(p, 0.78F, 1.00F));
                float pose = hold * (1.0F - release);
                m.rightArm.xRot += -1.54F * pose;
                m.leftArm.xRot += -1.54F * pose;
                m.rightArm.yRot += -0.24F * pose;
                m.leftArm.yRot += 0.24F * pose;
                m.body.yRot += 0.16F * hit;
            }
            case 2 -> {
                // Combat combo: punches alternate, then the legs add visible kicks instead of only arm vibration.
                float arm = (float)Math.sin(p * Math.PI * 10.0F);
                float leg = (float)Math.sin(p * Math.PI * 5.0F);
                float torso = (float)Math.sin(p * Math.PI * 5.0F + 0.65F);
                float envelope = easeOut(segment(p, 0.02F, 0.10F)) * (1.0F - easeIn(segment(p, 0.90F, 1.00F)));
                m.rightArm.xRot += -0.72F - 1.05F * arm * envelope;
                m.leftArm.xRot += -0.72F + 1.05F * arm * envelope;
                m.rightArm.yRot += -0.18F * Math.max(0.0F, arm) * envelope;
                m.leftArm.yRot += 0.18F * Math.max(0.0F, -arm) * envelope;
                m.rightLeg.xRot += 0.48F * leg * envelope;
                m.leftLeg.xRot -= 0.48F * leg * envelope;
                m.body.yRot += 0.16F * torso * envelope + 0.04F * hit;
                m.body.xRot += -0.035F * Math.abs(arm) * envelope;
            }
            default -> { }
        }
    }

    private static float segment(float value, float start, float end) {
        if (value <= start) return 0.0F;
        if (value >= end) return 1.0F;
        return (value - start) / (end - start);
    }

    private static float clamp(float value) { return Math.max(0.0F, Math.min(1.0F, value)); }
    private static float easeIn(float x) { x = clamp(x); return x * x; }
    private static float easeOut(float x) { x = clamp(x); float y = 1.0F - x; return 1.0F - y * y; }
    private static float easeInOut(float x) {
        x = clamp(x);
        return x < 0.5F ? 2.0F * x * x : 1.0F - (float)Math.pow(-2.0F * x + 2.0F, 2.0F) / 2.0F;
    }
}
