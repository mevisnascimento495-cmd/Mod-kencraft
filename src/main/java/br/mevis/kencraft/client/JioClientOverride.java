package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class JioClientOverride {
    private JioClientOverride() {}

    @SubscribeEvent
    public static void init(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof KenCraftScreen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        PlayerData data = mc.player.getData(ModAttachments.PLAYER_DATA);
        if (data.race().name().equals("HUMAN") && data.arfClass() >= 4 && "NONE".equals(PlayerData.normalizeTechnique(data.jioTechnique()))) {
            int left = (screen.width - 420) / 2;
            int top = (screen.height - 320) / 2;
            event.addListener(Button.builder(net.minecraft.network.chat.Component.literal("GIRAR TÉCNICA JIO"), b -> mc.player.connection.sendCommand("kencraftjio roll"))
                    .bounds(left + 118, top + 228, 184, 26).build());
        }
    }
}
