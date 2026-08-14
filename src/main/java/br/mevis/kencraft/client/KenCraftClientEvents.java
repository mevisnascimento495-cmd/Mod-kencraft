package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class KenCraftClientEvents {
    private static int jioChargeTicker;
    private KenCraftClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        KikanAnimationState.tick();
        JioAnimationState.tick();
        if (minecraft.screen == null && KenCraftClient.OPEN_MENU.consumeClick()) {
            minecraft.setScreen(new KenCraftScreenV2());
            return;
        }
        if (minecraft.screen == null && minecraft.player != null) {
            PlayerData data = minecraft.player.getData(ModAttachments.PLAYER_DATA);
            String technique = PlayerData.normalizeTechnique(data.jioTechnique());

            if (KenCraftClient.KIKAN_Z.isDown() && data.race() == Race.HUMAN) {
                if (++jioChargeTicker >= 5) {
                    jioChargeTicker = 0;
                    minecraft.player.connection.sendCommand("kencraftjio charge");
                }
            } else {
                jioChargeTicker = 0;
            }

            if (KenCraftClient.KIKAN_Z.consumeClick() && data.race() == Race.RINKA) {
                KikanAnimationState.trigger("z");
                minecraft.player.connection.sendCommand("kencraft kikan attack z");
            }
            if (KenCraftClient.KIKAN_C.consumeClick() && data.race() == Race.RINKA) {
                KikanAnimationState.trigger("c");
                minecraft.player.connection.sendCommand("kencraft kikan attack c");
            }

            if (KenCraftClient.JIO_F.consumeClick() && data.race() == Race.HUMAN && !"NONE".equals(technique)) {
                JioAnimationState.trigger(technique, data.jioAbilitySlot());
                minecraft.player.connection.sendCommand("kencraftjio use");
            }

            if (KenCraftClient.JIO_G.consumeClick() && data.race() == Race.HUMAN && !"NONE".equals(technique)) {
                minecraft.player.connection.sendCommand("kencraftjio next");
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!(event.getRenderer().getModel() instanceof PlayerModel<?> model)) return;

        animateHumanJio((PlayerModel<?>) model);
    }

    private static void animateHumanJio(PlayerModel<?> model) {
        if (!JioAnimationState.active()) return;

        float p = JioAnimationState.progress();
        float t = JioAnimationState.impactEnvelope();
        int ability = JioAnimationState.ability();
        String technique = JioAnimationState.technique();

        // Keep natural player pose as the baseline; only apply combat deltas.
        if (technique.equalsIgnoreCase("Seishin dan")) {
            if (ability == 0) {
                // Basic spiritual shot: raise the arm, then drive it forward at contact.
                float raise = easeInOut(Math.min(1.0F, p / 0.45F));
                float recoil = p > 0.5F ? t : 0.0F;
                model.rightArm.xRot -= 1.15F * raise;
                model.rightArm.zRot += 0.10F * raise;
                model.body.yRot -= 0.10F * raise;
                model.rightArm.xRot -= 0.55F * recoil;
                model.rightArm.zRot += 0.08F * recoil;
            } else if (ability == 1) {
                // Both arms stay raised while the technique continuously fires for ~4 seconds.
                float hold = Math.min(1.0F, p / 0.20F);
                float pulse = (float) Math.sin(p * Math.PI * 18.0F) * 0.10F;
                model.rightArm.xRot -= 1.25F * hold + pulse;
                model.leftArm.xRot -= 1.25F * hold - pulse;
                model.body.yRot += pulse * 0.18F;
            } else {
                // Intangibility: neutral stance; aura is rendered by JioAuraLayer.
                float calm = Math.min(1.0F, p / 0.25F);
                model.rightArm.xRot -= 0.18F * calm;
                model.leftArm.xRot -= 0.18F * calm;
            }
            return;
        }

        if (technique.equalsIgnoreCase("Hakai satsu Totetsu: Seimei kui")) {
            if (ability == 0) {
                // Pull back, punch, impact, then recoil.
                float windup = 1.0F - easeIn(Math.min(1.0F, p / 0.42F));
                float punch = Math.min(1.0F, p / 0.68F);
                model.rightArm.xRot += 0.78F * windup;
                model.rightArm.zRot -= 0.18F * windup;
                model.body.yRot -= 0.14F * windup;
                model.rightArm.xRot -= 1.65F * punch;
                model.body.yRot += 0.24F * t;
            } else if (ability == 1) {
                // Seven-second barrage: alternate the arms continuously.
                float combo = (float) Math.sin(p * Math.PI * 14.0F);
                float accent = (float) Math.sin(p * Math.PI * 7.0F);
                model.rightArm.xRot -= 0.95F + combo * 0.78F;
                model.leftArm.xRot -= 0.95F - combo * 0.78F;
                model.rightArm.zRot += combo * 0.18F;
                model.leftArm.zRot -= combo * 0.18F;
                model.body.yRot += accent * 0.12F;
            } else {
                // Destruction total: one clean, committed punch.
                float punch = easeInOut(Math.min(1.0F, p / 0.62F));
                model.rightArm.xRot -= 1.90F * punch;
                model.body.yRot += 0.22F * t;
            }
            return;
        }

        if (technique.equalsIgnoreCase("Kata kyoka")) {
            if (ability == 0) {
                // No animation by design.
                return;
            }
            if (ability == 1) {
                // Grab: both arms snap forward and hold the victim in place.
                float grab = easeOut(Math.min(1.0F, p / 0.50F));
                float hold = p >= 0.50F ? 1.0F : grab;
                model.rightArm.xRot -= 1.45F * hold;
                model.leftArm.xRot -= 1.45F * hold;
                model.rightArm.yRot -= 0.18F * hold;
                model.leftArm.yRot += 0.18F * hold;
                model.body.yRot += 0.16F * t;
                return;
            }

            // Reinforcement combo: alternating punches with synchronized leg/knee movement.
            float combo = (float) Math.sin(p * Math.PI * 10.0F);
            float kick = (float) Math.sin(p * Math.PI * 5.0F);
            model.rightArm.xRot -= 1.05F + combo * 0.88F;
            model.leftArm.xRot -= 1.05F - combo * 0.88F;
            model.rightLeg.xRot += kick * 0.34F;
            model.leftLeg.xRot -= kick * 0.34F;
            model.body.yRot += combo * 0.12F;
        }
    }

    private static float easeIn(float x) { return x * x; }
    private static float easeOut(float x) { float y = 1.0F - x; return 1.0F - y * y; }
    private static float easeInOut(float x) { return x < 0.5F ? 2.0F * x * x : 1.0F - (float) Math.pow(-2.0F * x + 2.0F, 2.0F) / 2.0F; }
}
