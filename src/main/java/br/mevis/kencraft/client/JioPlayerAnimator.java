package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/** Reliable third-person Jio animator, applied only to actual client players. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class JioPlayerAnimator {
    private JioPlayerAnimator() {}

    @SubscribeEvent
    public static void onPlayerRender(RenderLivingEvent.Pre<?, ?> event) {
        if (!JioAnimationState.active()) return;
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;
        if (!(event.getRenderer() instanceof PlayerRenderer renderer)) return;
        if (!(renderer.getModel() instanceof PlayerModel<?> rawModel)) return;
        if (!player.isAlive()) return;

        @SuppressWarnings("unchecked")
        PlayerModel<AbstractClientPlayer> model = (PlayerModel<AbstractClientPlayer>) rawModel;
        apply(model, JioAnimationState.technique(), JioAnimationState.ability(),
                JioAnimationState.progress(), JioAnimationState.impactEnvelope());
    }

    private static void apply(PlayerModel<AbstractClientPlayer> model, String technique, int ability,
                              float p, float impact) {
        p = clamp(p);

        if ("Seishin dan".equalsIgnoreCase(technique)) {
            switch (ability) {
                case 0 -> {
                    float lift = easeOut(Math.min(1.0F, p / 0.34F));
                    float thrust = easeInOut(Math.min(1.0F, Math.max(0.0F, (p - 0.28F) / 0.38F)));
                    float recoil = Math.max(0.0F, (p - 0.72F) / 0.28F);
                    model.rightArm.xRot = -1.15F * lift - 1.35F * thrust + 0.55F * recoil;
                    model.rightArm.yRot = -0.18F * thrust;
                    model.rightArm.zRot = 0.08F * lift;
                    model.body.yRot = 0.12F * thrust;
                }
                case 1 -> {
                    float hold = Math.min(1.0F, p / 0.16F);
                    float pulse = (float) Math.sin(p * Math.PI * 18.0F) * 0.13F * hold;
                    model.rightArm.xRot = -1.30F * hold - pulse;
                    model.leftArm.xRot = -1.30F * hold + pulse;
                    model.rightArm.zRot = 0.12F * hold;
                    model.leftArm.zRot = -0.12F * hold;
                    model.body.yRot = pulse * 0.45F;
                }
                case 2 -> {
                    float calm = easeOut(Math.min(1.0F, p / 0.22F));
                    model.rightArm.xRot = -0.20F * calm;
                    model.leftArm.xRot = -0.20F * calm;
                    model.body.yRot = 0.04F * (float) Math.sin(p * Math.PI * 2.0F);
                }
                default -> {}
            }
            return;
        }

        if ("Hakai satsu Totetsu: Seimei kui".equalsIgnoreCase(technique)) {
            switch (ability) {
                case 0 -> {
                    float windup = 1.0F - easeIn(Math.min(1.0F, p / 0.34F));
                    float punch = easeInOut(Math.min(1.0F, Math.max(0.0F, (p - 0.25F) / 0.42F)));
                    float recoil = Math.max(0.0F, (p - 0.78F) / 0.22F);
                    model.rightArm.xRot = 0.82F * windup - 1.92F * punch + 0.65F * recoil;
                    model.rightArm.yRot = -0.12F * windup;
                    model.body.yRot = -0.18F * windup + 0.28F * impact;
                }
                case 1 -> {
                    float combo = (float) Math.sin(p * Math.PI * 14.0F);
                    model.rightArm.xRot = -0.90F - 0.95F * combo;
                    model.leftArm.xRot = -0.90F + 0.95F * combo;
                    model.rightArm.yRot = -0.26F * Math.max(0.0F, combo);
                    model.leftArm.yRot = 0.26F * Math.max(0.0F, -combo);
                    model.rightArm.zRot = 0.10F * combo;
                    model.leftArm.zRot = -0.10F * combo;
                    model.body.yRot = 0.15F * (float) Math.sin(p * Math.PI * 7.0F);
                }
                case 2 -> {
                    float punch = easeInOut(Math.min(1.0F, p / 0.62F));
                    model.rightArm.xRot = -2.02F * punch + 0.60F * Math.max(0.0F, (p - 0.82F) / 0.18F);
                    model.body.yRot = 0.26F * impact;
                    model.body.xRot = -0.10F * punch;
                }
                default -> {}
            }
            return;
        }

        if ("Kata kyoka".equalsIgnoreCase(technique)) {
            if (ability == 0) return;
            if (ability == 1) {
                float grab = easeOut(Math.min(1.0F, p / 0.45F));
                float hold = p < 0.45F ? grab : 1.0F;
                model.rightArm.xRot = -1.58F * hold;
                model.leftArm.xRot = -1.58F * hold;
                model.rightArm.yRot = -0.22F * hold;
                model.leftArm.yRot = 0.22F * hold;
                model.body.yRot = 0.18F * impact;
                return;
            }
            float combo = (float) Math.sin(p * Math.PI * 10.0F);
            float kick = (float) Math.sin(p * Math.PI * 5.0F);
            model.rightArm.xRot = -1.05F - 0.98F * combo;
            model.leftArm.xRot = -1.05F + 0.98F * combo;
            model.rightArm.yRot = -0.16F * Math.max(0.0F, combo);
            model.leftArm.yRot = 0.16F * Math.max(0.0F, -combo);
            model.rightLeg.xRot = 0.35F * kick;
            model.leftLeg.xRot = -0.35F * kick;
            model.body.yRot = 0.14F * combo;
        }
    }

    private static float clamp(float value) { return Math.max(0.0F, Math.min(1.0F, value)); }
    private static float easeIn(float x) { return x * x; }
    private static float easeOut(float x) { float y = 1.0F - x; return 1.0F - y * y; }
    private static float easeInOut(float x) {
        x = clamp(x);
        return x < 0.5F ? 2.0F * x * x : 1.0F - (float) Math.pow(-2.0F * x + 2.0F, 2.0F) / 2.0F;
    }
}
