package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/** Second-generation first-person animation hook. Vanilla restores the surrounding pose stack. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class PlayerAnimationFirstPersonHooksV2 {
    private PlayerAnimationFirstPersonHooksV2() {}

    @SubscribeEvent
    public static void renderFirstPersonHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getHand() != InteractionHand.MAIN_HAND) return;

        var pose = event.getPoseStack();
        float progress;
        float swing;

        if (KikanAnimationState.active()) {
            progress = KikanAnimationState.progress();
            swing = (float) Math.sin(progress * Math.PI);
            boolean heavy = KikanAnimationState.isHeavy();
            pose.translate(0.0D, 0.045D * swing, -0.20D * swing);
            pose.mulPose(Axis.XP.rotationDegrees(-22.0F * swing));
            pose.mulPose(Axis.YP.rotationDegrees(9.0F * swing));
            pose.mulPose(Axis.ZP.rotationDegrees((heavy ? 30.0F : 20.0F) * swing));
            return;
        }

        if (!JioAnimationState.active()) return;
        progress = JioAnimationState.progress();
        swing = (float) Math.sin(progress * Math.PI);
        int ability = JioAnimationState.ability();
        String technique = JioAnimationState.technique();

        if (technique.equalsIgnoreCase("Seishin dan")) {
            if (ability == 0) {
                pose.translate(0.0D, 0.025D * swing, -0.25D * swing);
                pose.mulPose(Axis.XP.rotationDegrees(-24.0F * swing));
            } else if (ability == 1) {
                float rapid = (float) Math.sin(progress * Math.PI * 8.0F) * swing;
                pose.translate(0.0D, 0.025D * swing, -0.20D * swing);
                pose.mulPose(Axis.XP.rotationDegrees(-23.0F * swing));
                pose.mulPose(Axis.ZP.rotationDegrees(28.0F * rapid));
            } else {
                pose.translate(0.0D, 0.015D * swing, 0.02D * swing);
                pose.mulPose(Axis.YP.rotationDegrees(8.0F * swing));
                pose.mulPose(Axis.ZP.rotationDegrees(34.0F * swing));
            }
        } else if (technique.equalsIgnoreCase("Hakai satsu Totetsu: Seimei kui")) {
            if (ability == 0) {
                pose.translate(0.0D, 0.05D * swing, -0.30D * swing);
                pose.mulPose(Axis.XP.rotationDegrees(-38.0F * swing));
                pose.mulPose(Axis.YP.rotationDegrees(12.0F * swing));
            } else if (ability == 1) {
                float rapid = (float) Math.sin(progress * Math.PI * 10.0F) * swing;
                pose.translate(0.0D, 0.035D * swing, -0.22D * swing);
                pose.mulPose(Axis.XP.rotationDegrees(-28.0F * swing));
                pose.mulPose(Axis.ZP.rotationDegrees(30.0F * rapid));
            } else {
                pose.translate(0.0D, 0.055D * swing, -0.38D * swing);
                pose.mulPose(Axis.XP.rotationDegrees(-48.0F * swing));
                pose.mulPose(Axis.ZP.rotationDegrees(40.0F * swing));
            }
        } else if (technique.equalsIgnoreCase("Kata kyoka")) {
            if (ability == 0) {
                pose.translate(0.0D, 0.015D * swing, -0.08D * swing);
                pose.mulPose(Axis.XP.rotationDegrees(-10.0F * swing));
            } else if (ability == 1) {
                pose.translate(0.0D, 0.07D * swing, -0.34D * swing);
                pose.mulPose(Axis.XP.rotationDegrees(-42.0F * swing));
                pose.mulPose(Axis.YP.rotationDegrees(18.0F * swing));
            } else {
                float combo = (float) Math.sin(progress * Math.PI * 8.0F) * swing;
                pose.translate(0.0D, 0.045D * swing, -0.26D * swing);
                pose.mulPose(Axis.XP.rotationDegrees(-32.0F * swing));
                pose.mulPose(Axis.ZP.rotationDegrees(30.0F * combo));
            }
        }
    }
}
