package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Double-tap W dash trigger. The server still validates Speed >= 5. */
@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class DashMovementTicker {
    private static boolean forwardHeld;
    private static int tapWindow;

    private DashMovementTicker() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            forwardHeld = false;
            tapWindow = 0;
            return;
        }

        if (tapWindow > 0) tapWindow--;
        boolean nowHeld = mc.options.keyUp.isDown();
        if (nowHeld && !forwardHeld) {
            if (tapWindow > 0 && mc.player.getData(ModAttachments.PLAYER_DATA).speed() >= 5) {
                mc.player.connection.sendCommand("kencraft dash");
                tapWindow = 0;
            } else {
                tapWindow = 10;
            }
        }
        forwardHeld = nowHeld;
    }
}
