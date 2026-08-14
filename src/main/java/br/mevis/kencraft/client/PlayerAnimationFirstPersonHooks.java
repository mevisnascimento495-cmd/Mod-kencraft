package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/** First-person presentation of the same dependency-free combat animations. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class PlayerAnimationFirstPersonHooks {
    private PlayerAnimationFirstPersonHooks() {}

    @SubscribeEvent
    public static void renderFirstPersonHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        float side = event.getHand() == InteractionHand.MAIN_HAND ? 1.0F : -1.0F;
        var pose = event.getPoseStack();

        if (KikanAnimationState.active()) {
            float p = KikanAnimationState.progress();
            float hit = KikanAnimationState.impactEnvelope();
            boolean heavy = KikanAnimationState.isHeavy();
            pose.pushPose();
            pose.translate(0.0D, 0.03D * hit, -0.13D * hit);
            pose.mulPose(Axis.XP.rotationDegrees(-12.0F * hit));
            pose.mulPose(Axis.YP.rotationDegrees(side * (heavy ? 15.0F : 9.0F) * hit));
            pose.mulPose(Axis.ZP.rotationDegrees(side * 8.0F * hit));
            pose.popPose();
        }

        if (!JioAnimationState.active()) return;

        float p = JioAnimationState.progress();
        float hit = JioAnimationState.impactEnvelope();
        int ability = JioAnimationState.ability();
        String technique = JioAnimationState.technique();

        pose.pushPose();
        if (technique.equalsIgnoreCase("Seishin dan")) {
            if (ability == 0) {
                // Raise the arm, then drive it forward for the shot.
                float raise = Math.min(1.0F, p / 0.45F);
                pose.translate(0.0D, 0.02D * raise, -0.20D * raise - 0.08D * hit);
                pose.mulPose(Axis.XP.rotationDegrees(-18.0F * raise - 15.0F * hit));
                pose.mulPose(Axis.YP.rotationDegrees(side * 8.0F * raise));
            } else if (ability == 1) {
                // Both arms are held forward while the stream fires for ~4 seconds.
                float hold = Math.min(1.0F, p / 0.20F);
                float pulse = (float) Math.sin(p * Math.PI * 18.0F) * 2.5F;
                pose.translate(0.0D, 0.02D * hold, -0.19D * hold);
                pose.mulPose(Axis.XP.rotationDegrees(-20.0F * hold));
                pose.mulPose(Axis.ZP.rotationDegrees(side * pulse));
            } else {
                // Protection stance: no violent arm motion; the blue aura is visualized separately.
                pose.translate(0.0D, 0.01D, 0.02D * (1.0F - p));
                pose.mulPose(Axis.YP.rotationDegrees(side * 4.0F * (1.0F - p)));
            }
        } else if (technique.equalsIgnoreCase("Hakai satsu Totetsu: Seimei kui")) {
            if (ability == 0) {
                float windup = 1.0F - Math.min(1.0F, p / 0.42F);
                pose.translate(0.0D, 0.04D * hit, -0.18D * hit + 0.16D * windup);
                pose.mulPose(Axis.XP.rotationDegrees(-34.0F * hit + 28.0F * windup));
                pose.mulPose(Axis.YP.rotationDegrees(side * (12.0F * hit + 8.0F * windup)));
            } else if (ability == 1) {
                float combo = (float) Math.sin(p * Math.PI * 14.0F);
                pose.translate(0.0D, 0.02D, -0.18D);
                pose.mulPose(Axis.XP.rotationDegrees(-24.0F));
                pose.mulPose(Axis.ZP.rotationDegrees(side * 24.0F * combo));
            } else {
                pose.translate(0.0D, 0.04D * hit, -0.28D * hit);
                pose.mulPose(Axis.XP.rotationDegrees(-42.0F * hit));
                pose.mulPose(Axis.ZP.rotationDegrees(side * 22.0F * hit));
            }
        } else if (technique.equalsIgnoreCase("Kata kyoka")) {
            if (ability == 1) {
                float grab = Math.min(1.0F, p / 0.50F);
                pose.translate(0.0D, 0.04D * grab, -0.28D * grab);
                pose.mulPose(Axis.XP.rotationDegrees(-34.0F * grab));
                pose.mulPose(Axis.YP.rotationDegrees(side * 14.0F * grab));
            } else if (ability == 2) {
                float combo = (float) Math.sin(p * Math.PI * 10.0F);
                pose.translate(0.0D, 0.03D, -0.20D);
                pose.mulPose(Axis.XP.rotationDegrees(-26.0F));
                pose.mulPose(Axis.ZP.rotationDegrees(side * 26.0F * combo));
            }
        }
        pose.popPose();
    }
}
