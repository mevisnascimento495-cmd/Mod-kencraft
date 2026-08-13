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
            minecraft.setScreen(new KenCraftScreen());
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

            if (KenCraftClient.JIO_F.consumeClick() && data.race() == Race.HUMAN) {
                if (!"NONE".equals(technique)) {
                    JioAnimationState.trigger(technique, data.jioAbilitySlot());
                    minecraft.player.connection.sendCommand("kencraftjio use");
                }
            }

            if (KenCraftClient.JIO_G.consumeClick() && data.race() == Race.HUMAN) {
                if (!"NONE".equals(technique)) {
                    minecraft.player.connection.sendCommand("kencraftjio next");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!(event.getRenderer().getModel() instanceof PlayerModel<?> model)) return;

        if (KikanAnimationState.active()) {
            float progress = KikanAnimationState.progress();
            float swing = (float) Math.sin(progress * Math.PI);
            float direction = KikanAnimationState.isHeavy() ? 1.0F : 0.72F;
            model.rightArm.xRot -= swing * 1.25F * direction;
            model.leftArm.xRot += swing * 0.35F;
            model.rightArm.zRot += swing * 0.22F;
            model.body.yRot += swing * 0.18F;
        }

        if (JioAnimationState.active()) {
            float progress = JioAnimationState.progress();
            float swing = (float) Math.sin(progress * Math.PI);
            int ability = JioAnimationState.ability();
            String technique = JioAnimationState.technique();

            if (technique.equalsIgnoreCase("Seishin dan")) {
                if (ability == 0) {
                    model.rightArm.xRot -= swing * 1.05F;
                    model.leftArm.xRot -= swing * 0.20F;
                    model.body.yRot -= swing * 0.12F;
                } else if (ability == 1) {
                    model.rightArm.xRot -= swing * 1.35F;
                    model.leftArm.xRot -= swing * 0.95F;
                    model.body.yRot += swing * 0.10F;
                } else {
                    model.rightArm.xRot = -0.55F * swing;
                    model.leftArm.xRot = 0.55F * swing;
                }
            } else if (technique.equalsIgnoreCase("Hakai satsu Totetsu: Seimei kui")) {
                if (ability == 0) {
                    model.rightArm.xRot -= swing * 1.45F;
                    model.body.yRot += swing * 0.20F;
                } else if (ability == 1) {
                    float rapid = (float) Math.sin(progress * Math.PI * 6.0F) * swing;
                    model.rightArm.xRot -= rapid * 0.80F;
                    model.leftArm.xRot += rapid * 0.80F;
                    model.body.yRot += rapid * 0.10F;
                } else {
                    model.rightArm.xRot -= swing * 1.65F;
                    model.leftArm.xRot -= swing * 0.95F;
                    model.body.yRot -= swing * 0.25F;
                }
            } else if (technique.equalsIgnoreCase("Kata kyoka")) {
                if (ability == 0) {
                    model.rightArm.xRot -= swing * 0.25F;
                    model.leftArm.xRot += swing * 0.25F;
                    model.body.yRot += swing * 0.08F;
                } else if (ability == 1) {
                    model.rightArm.xRot -= swing * 1.55F;
                    model.leftArm.xRot += swing * 0.25F;
                    model.body.yRot += swing * 0.22F;
                } else {
                    float combo = (float) Math.sin(progress * Math.PI * 5.0F) * swing;
                    model.rightArm.xRot -= combo * 0.95F;
                    model.leftArm.xRot += combo * 0.95F;
                    model.body.yRot += combo * 0.16F;
                }
            }
        }
    }
}
