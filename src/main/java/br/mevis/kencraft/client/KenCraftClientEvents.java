package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class KenCraftClientEvents {
    private KenCraftClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        KikanAnimationState.tick();

        if (minecraft.screen == null && KenCraftClient.OPEN_MENU.consumeClick()) {
            minecraft.setScreen(new KenCraftScreen());
            return;
        }

        if (minecraft.screen == null && minecraft.player != null) {
            if (KenCraftClient.KIKAN_Z.consumeClick()) {
                KikanAnimationState.trigger("z");
                minecraft.player.connection.sendCommand("kencraft kikan attack z");
            }
            if (KenCraftClient.KIKAN_C.consumeClick()) {
                KikanAnimationState.trigger("c");
                minecraft.player.connection.sendCommand("kencraft kikan attack c");
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!KikanAnimationState.active()) return;
        if (!(event.getRenderer().getModel() instanceof PlayerModel<?> model)) return;

        float progress = KikanAnimationState.progress();
        float swing = (float) Math.sin(progress * Math.PI);
        float direction = KikanAnimationState.isHeavy() ? 1.0F : 0.72F;

        model.rightArm.xRot -= swing * 1.25F * direction;
        model.leftArm.xRot += swing * 0.35F;
        model.rightArm.zRot += swing * 0.22F;
        model.body.yRot += swing * 0.18F;
    }
}
