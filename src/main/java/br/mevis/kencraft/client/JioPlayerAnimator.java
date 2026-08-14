package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * Third-person human Jio animator.
 *
 * The authoritative attack clock lives on a synchronized player attachment.
 * That means the local player and every tracking client render the same attack
 * instead of depending on one client's static animation state.
 */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class JioPlayerAnimator {
    private JioPlayerAnimator() {}

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player == null || !player.isAlive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        JioAnimationData animation = player.getData(ModAttachments.JIO_ANIMATION);
        long tick = mc.level.getGameTime();
        if (!animation.active(tick)) return;

        PlayerModel<?> model = event.getRenderer().getModel();
        if (model == null) return;

        apply(model, animation.technique(), animation.ability(),
                animation.progress(tick), animation.impactEnvelope(tick));
    }

    private static void apply(PlayerModel<?> model, String technique,
                              int ability, float progress, float impact) {
        float p = clamp(progress);
        float hit = clamp(impact);
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
                float windup = easeOut(segment(p, 0.00F, 0.32F));
                float strike = easeInOut(segment(p, 0.24F, 0.72F));
                float recoil = easeOut(segment(p, 0.72F, 1.00F));
                model.rightArm.xRot = lerp(0.0F, -1.20F, windup)
                        + lerp(0.0F, -1.15F, strike) + lerp(0.0F, 0.40F, recoil);
                model.rightArm.yRot = -0.16F * strike;
                model.rightArm.zRot = 0.06F * windup;
                model.body.yRot = 0.10F * strike;
            }
            case 1 -> {
                float raise = easeOut(segment(p, 0.00F, 0.14F));
                float pulse = (float) Math.sin(p * Math.PI * 18.0F) * 0.10F;
                model.rightArm.xRot = -1.28F * raise - pulse * raise;
                model.leftArm.xRot = -1.28F * raise + pulse * raise;
                model.rightArm.zRot = 0.10F * raise;
                model.leftArm.zRot = -0.10F * raise;
                model.body.yRot = 0.04F * pulse + 0.10F * hit;
            }
            case 2 -> {
                float aura = easeOut(segment(p, 0.00F, 0.20F));
                float breathe = (float) Math.sin(p * Math.PI * 2.0F);
                model.rightArm.xRot = -0.16F * aura;
                model.leftArm.xRot = -0.16F * aura;
                model.rightArm.zRot = 0.04F * breathe * aura;
                model.leftArm.zRot = -0.04F * breathe * aura;
                model.body.yRot = 0.03F * breathe;
            }
            default -> {}
        }
    }

    private static void applyHakai(PlayerModel<?> model, int ability, float p, float hit) {
        switch (ability) {
            case 0 -> {
                float windup = 1.0F - easeIn(segment(p, 0.00F, 0.30F));
                float strike = easeInOut(segment(p, 0.24F, 0.66F));
                float recoil = easeOut(segment(p, 0.70F, 1.00F));
                model.rightArm.xRot = 0.72F * windup - 1.90F * strike + 0.58F * recoil;
                model.rightArm.yRot = -0.12F * windup;
                model.body.yRot = -0.16F * windup + 0.22F * hit;
                model.body.xRot = -0.08F * strike;
            }
            case 1 -> {
                float combo = (float) Math.sin(p * Math.PI * 14.0F);
                float envelope = easeOut(segment(p, 0.00F, 0.10F))
                        * (1.0F - easeIn(segment(p, 0.94F, 1.00F)));
                combo *= envelope;
                model.rightArm.xRot = -0.82F - 0.88F * combo;
                model.leftArm.xRot = -0.82F + 0.88F * combo;
                model.rightArm.yRot = -0.22F * Math.max(0.0F, combo);
                model.leftArm.yRot = 0.22F * Math.max(0.0F, -combo);
                model.rightArm.zRot = 0.08F * combo;
                model.leftArm.zRot = -0.08F * combo;
                model.body.yRot = 0.12F * (float) Math.sin(p * Math.PI * 7.0F);
            }
            case 2 -> {
                float windup = 1.0F - easeIn(segment(p, 0.00F, 0.30F));
                float strike = easeInOut(segment(p, 0.22F, 0.76F));
                float recoil = easeOut(segment(p, 0.82F, 1.00F));
                model.rightArm.xRot = 0.82F * windup - 2.05F * strike + 0.55F * recoil;
                model.body.yRot = 0.20F * hit;
                model.body.xRot = -0.10F * strike;
            }
            default -> {}
        }
    }

    private static void applyKata(PlayerModel<?> model, int ability, float p, float hit) {
        switch (ability) {
            case 0 -> { }
            case 1 -> {
                float grab = easeOut(segment(p, 0.00F, 0.35F));
                float hold = p >= 0.35F ? 1.0F : grab;
                model.rightArm.xRot = -1.52F * hold;
                model.leftArm.xRot = -1.52F * hold;
                model.rightArm.yRot = -0.20F * hold;
                model.leftArm.yRot = 0.20F * hold;
                model.body.yRot = 0.14F * hit;
            }
            case 2 -> {
                float combo = (float) Math.sin(p * Math.PI * 10.0F);
                float kick = (float) Math.sin(p * Math.PI * 5.0F);
                model.rightArm.xRot = -1.00F - 0.92F * combo;
                model.leftArm.xRot = -1.00F + 0.92F * combo;
                model.rightArm.yRot = -0.15F * Math.max(0.0F, combo);
                model.leftArm.yRot = 0.15F * Math.max(0.0F, -combo);
                model.rightLeg.xRot = 0.30F * kick;
                model.leftLeg.xRot = -0.30F * kick;
                model.body.yRot = 0.12F * combo;
            }
            default -> { }
        }
    }

    private static float segment(float value, float start, float end) {
        if (value <= start) return 0.0F;
        if (value >= end) return 1.0F;
        return (value - start) / (end - start);
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
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
