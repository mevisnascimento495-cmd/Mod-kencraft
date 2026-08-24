package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

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
        if (minecraft.player == null) return;
        boolean active = minecraft.player.getData(ModAttachments.KIKAKOGOU_STATE).active();
        if (active && minecraft.screen != null) minecraft.setScreen(null);
        if (minecraft.screen == null) {
            PlayerData data = minecraft.player.getData(ModAttachments.PLAYER_DATA);
            String technique = PlayerData.normalizeTechnique(data.jioTechnique());
            boolean canJio = data.race() == Race.HUMAN || data.race() == Race.HYBRID || data.race() == Race.JASHIN;
            boolean canKikan = data.race() == Race.RINKA || data.race() == Race.HYBRID;

            if (minecraft.player.isAlive() && KikakogouKeyHandler.KIKAKOGOU.consumeClick()) {
                minecraft.player.connection.sendCommand("kencraft kikakogou toggle");
                return;
            }
            if (KenCraftClient.KIKAN_Z.isDown() && canJio) {
                if (++jioChargeTicker >= 5) {
                    jioChargeTicker = 0;
                    minecraft.player.connection.sendCommand("kencraftjio charge");
                }
            } else {
                jioChargeTicker = 0;
            }
            if (KenCraftClient.KIKAN_Z.consumeClick() && canKikan) {
                KikanAnimationState.trigger("z");
                if (active) minecraft.player.connection.sendCommand("kencraft kikakogou ability z");
                else minecraft.player.connection.sendCommand("kencraft kikan attack z");
            }
            if (KenCraftClient.KIKAN_C.consumeClick() && canKikan) {
                KikanAnimationState.trigger("c");
                if (active) minecraft.player.connection.sendCommand("kencraft kikakogou ability c");
                else minecraft.player.connection.sendCommand("kencraft kikan attack c");
            }
            if (KenCraftClient.JIO_F.consumeClick() && canJio && !"NONE".equals(technique)) {
                JioAnimationState.trigger(technique, data.jioAbilitySlot());
                minecraft.player.connection.sendCommand("kencraftjio use");
            }
            if (KenCraftClient.JIO_G.consumeClick() && canJio && !"NONE".equals(technique)) {
                minecraft.player.connection.sendCommand("kencraftjio next");
            }
        }
    }
}
