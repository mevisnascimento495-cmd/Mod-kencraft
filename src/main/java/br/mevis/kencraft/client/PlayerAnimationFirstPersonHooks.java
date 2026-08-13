package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * First-person half of KenCraft's custom animation system.
 * RenderPlayerEvent only affects the player model in third person; Minecraft uses RenderHandEvent
 * for the local first-person arm, so this hook makes the same animation visible there as well.
 */
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
            float progress = KikanAnimationState.progress();
            float swing = (float) Math.sin(progress * Math.PI);
            boolean heavy = KikanAnimationState.isHeavy();

            pose.pushPose();
            pose.translate(0.0D, 0.04D * swing, -0.17D * swing);
            pose.mulPose(Axis.XP.rotationDegrees(-18.0F * swing));
            pose.mulPose(Axis.YP.rotationDegrees(side * 10.0F * swing));
            pose.mulPose(Axis.ZP.rotationDegrees(side * (18.0F + (heavy ? 12.0F : 0.0F)) * swing));
            pose.popPose();
        }

        if (JioAnimationState.active()) {
            float progress = JioAnimationState.progress();
            float swing = (float) Math.sin(progress * Math.PI);
            int ability = JioAnimationState.ability();
            String technique = JioAnimationState.technique();

            pose.pushPose();
            if (technique.equalsIgnoreCase("Seishin dan")) {
                if (ability == 0) {
                    pose.translate(0.0D, 0.03D * swing, -0.22D * swing);
                    pose.mulPose(Axis.XP.rotationDegrees(-20.0F * swing));
                    pose.mulPose(Axis.YP.rotationDegrees(side * 8.0F * swing));
                } else if (ability == 1) {
                    float rapid = (float) Math.sin(progress * Math.PI * 7.0F) * swing;
                    pose.translate(0.0D, 0.02D * swing, -0.18D * swing);
                    pose.mulPose(Axis.XP.rotationDegrees(-22.0F * swing));
                    pose.mulPose(Axis.ZP.rotationDegrees(side * 22.0F * rapid));
                } else {
                    pose.translate(0.0D, 0.02D * swing, 0.03D * swing);
                    pose.mulPose(Axis.YP.rotationDegrees(side * 6.0F * swing));
                    pose.mulPose(Axis.ZP.rotationDegrees(side * 30.0F * swing));
                }
            } else if (technique.equalsIgnoreCase("Hakai satsu Totetsu: Seimei kui")) {
                if (ability == 0) {
                    pose.translate(0.0D, 0.04D * swing, -0.28D * swing);
                    pose.mulPose(Axis.XP.rotationDegrees(-34.0F * swing));
                    pose.mulPose(Axis.YP.rotationDegrees(side * 16.0F * swing));
                } else if (ability == 1) {
                    float rapid = (float) Math.sin(progress * Math.PI * 9.0F) * swing;
                    pose.translate(0.0D, 0.03D * swing, -0.20D * swing);
                    pose.mulPose(Axis.XP.rotationDegrees(-26.0F * swing));
                    pose.mulPose(Axis.ZP.rotationDegrees(side * 26.0F * rapid));
                } else {
                    pose.translate(0.0D, 0.05D * swing, -0.34D * swing);
                    pose.mulPose(Axis.XP.rotationDegrees(-42.0F * swing));
                    pose.mulPose(Axis.ZP.rotationDegrees(side * 36.0F * swing));
                }
            } else if (technique.equalsIgnoreCase("Kata kyoka")) {
                if (ability == 0) {
                    pose.translate(0.0D, 0.02D * swing, -0.08D * swing);
                    pose.mulPose(Axis.XP.rotationDegrees(-9.0F * swing));
                } else if (ability == 1) {
                    pose.translate(0.0D, 0.06D * swing, -0.32D * swing);
                    pose.mulPose(Axis.XP.rotationDegrees(-38.0F * swing));
                    pose.mulPose(Axis.YP.rotationDegrees(side * 20.0F * swing));
                } else {
                    float combo = (float) Math.sin(progress * Math.PI * 8.0F) * swing;
                    pose.translate(0.0D, 0.04D * swing, -0.24D * swing);
                    pose.mulPose(Axis.XP.rotationDegrees(-30.0F * swing));
                    pose.mulPose(Axis.ZP.rotationDegrees(side * 28.0F * combo));
                }
            }
            pose.popPose();
        }
    }
}
