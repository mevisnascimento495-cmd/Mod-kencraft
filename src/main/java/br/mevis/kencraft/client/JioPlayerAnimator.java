package br.mevis.kencraft.client;

import br.mevis.kencraft.data.JioAnimationData;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.player.Player;

/**
 * Local keyframe-style evaluator for KenCraft Jio animations.
 * Server state is replicated per player; this evaluator is applied after
 * vanilla PlayerModel.setupAnim so vanilla pose code cannot overwrite it.
 */
public final class JioPlayerAnimator {
    private JioPlayerAnimator() {}

    public static void apply(PlayerModel<?> model, Player player, JioAnimationData state, long gameTime) {
        if (model == null || player == null || state == null || !state.activeAt(gameTime)) return;

        float p = state.progressAt(gameTime);
        float hit = state.impactEnvelopeAt(gameTime);
        String technique = state.technique();
        int ability = Math.max(0, Math.min(2, state.ability()));

        if ("Seishin dan".equalsIgnoreCase(technique)) {
            applySeishin(model, ability, p, hit);
        } else if ("Hakai satsu Totetsu: Seimei kui".equalsIgnoreCase(technique)) {
            applyHakai(model, ability, p, hit);
        } else if ("Kata kyoka".equalsIgnoreCase(technique)) {
            applyKata(model, ability, p, hit);
        }
    }

    private static void applySeishin(PlayerModel<?> model, int ability, float p, float hit) {
        switch (ability) {
            case 0 -> {
                float raise = easeOut(segment(p, 0.00F, 0.30F));
                float aim = easeInOut(segment(p, 0.25F, 0.62F));
                float recoil = easeOut(segment(p, 0.72F, 1.00F));
                model.rightArm.xRot += -1.20F * raise - 1.05F * aim + 0.38F * recoil;
                model.rightArm.yRot += -0.14F * aim;
                model.rightArm.zRot += 0.06F * raise;
                model.body.yRot += 0.10F * aim;
            }
            case 1 -> {
                float raise = easeOut(segment(p, 0.00F, 0.14F));
                float pulse = (float) Math.sin(p * Math.PI * 18.0F) * 0.10F * raise;
                model.rightArm.xRot += -1.26F * raise - pulse;
                model.leftArm.xRot += -1.26F * raise + pulse;
                model.rightArm.zRot += 0.10F * raise;
                model.leftArm.zRot -= 0.10F * raise;
                model.body.yRot += 0.06F * pulse + 0.08F * hit;
            }
            case 2 -> {
                float settle = easeOut(segment(p, 0.00F, 0.18F));
                float breathe = (float) Math.sin(p * Math.PI * 2.0F);
                model.rightArm.xRot += -0.14F * settle;
                model.leftArm.xRot += -0.14F * settle;
                model.rightArm.zRot += 0.03F * breathe * settle;
                model.leftArm.zRot -= 0.03F * breathe * settle;
                model.body.yRot += 0.03F * breathe;
            }
            default -> { }
        }
    }

    private static void applyHakai(PlayerModel<?> model, int ability, float p, float hit) {
        switch (ability) {
            case 0 -> {
                float windup = 1.0F - easeIn(segment(p, 0.00F, 0.30F));
                float strike = easeInOut(segment(p, 0.25F, 0.66F));
                float recoil = easeOut(segment(p, 0.72F, 1.00F));
                model.rightArm.xRot += 0.74F * windup - 1.92F * strike + 0.58F * recoil;
                model.rightArm.yRot += -0.12F * windup;
                model.body.yRot += -0.16F * windup + 0.24F * hit;
                model.body.xRot += -0.08F * strike;
            }
            case 1 -> {
                float envelope = easeOut(segment(p, 0.00F, 0.08F))
                        * (1.0F - easeIn(segment(p, 0.94F, 1.00F)));
                float combo = (float) Math.sin(p * Math.PI * 14.0F) * envelope;
                model.rightArm.xRot += -0.78F - 0.92F * combo;
                model.leftArm.xRot += -0.78F + 0.92F * combo;
                model.rightArm.yRot += -0.20F * Math.max(0.0F, combo);
                model.leftArm.yRot += 0.20F * Math.max(0.0F, -combo);
                model.rightArm.zRot += 0.08F * combo;
                model.leftArm.zRot -= 0.08F * combo;
                model.body.yRot += 0.12F * (float) Math.sin(p * Math.PI * 7.0F);
            }
            case 2 -> {
                float windup = 1.0F - easeIn(segment(p, 0.00F, 0.28F));
                float strike = easeInOut(segment(p, 0.22F, 0.74F));
                float recoil = easeOut(segment(p, 0.82F, 1.00F));
                model.rightArm.xRot += 0.82F * windup - 2.04F * strike + 0.54F * recoil;
                model.body.xRot += -0.10F * strike;
                model.body.yRot += 0.20F * hit;
            }
            default -> { }
        }
    }

    private static void applyKata(PlayerModel<?> model, int ability, float p, float hit) {
        switch (ability) {
            case 0 -> { }
            case 1 -> {
                float grab = easeOut(segment(p, 0.00F, 0.35F));
                float hold = p >= 0.35F ? 1.0F : grab;
                model.rightArm.xRot += -1.52F * hold;
                model.leftArm.xRot += -1.52F * hold;
                model.rightArm.yRot += -0.20F * hold;
                model.leftArm.yRot += 0.20F * hold;
                model.body.yRot += 0.14F * hit;
            }
            case 2 -> {
                float combo = (float) Math.sin(p * Math.PI * 10.0F);
                float kick = (float) Math.sin(p * Math.PI * 5.0F);
                model.rightArm.xRot += -1.00F - 0.92F * combo;
                model.leftArm.xRot += -1.00F + 0.92F * combo;
                model.rightArm.yRot += -0.15F * Math.max(0.0F, combo);
                model.leftArm.yRot += 0.15F * Math.max(0.0F, -combo);
                model.rightLeg.xRot += 0.30F * kick;
                model.leftLeg.xRot -= 0.30F * kick;
                model.body.yRot += 0.12F * combo;
            }
            default -> { }
        }
    }

    private static float segment(float value, float start, float end) {
        if (value <= start) return 0.0F;
        if (value >= end) return 1.0F;
        return (value - start) / (end - start);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float easeIn(float x) {
        x = clamp(x);
        return x * x;
    }

    private static float easeOut(float x) {
        x = clamp(x);
        float y = 1.0F - x;
        return 1.0F - y * y;
    }

    private static float easeInOut(float x) {
        x = clamp(x);
        return x < 0.5F ? 2.0F * x * x
                : 1.0F - (float) Math.pow(-2.0F * x + 2.0F, 2.0F) / 2.0F;
    }
}
