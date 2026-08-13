package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.data.ModAttachments;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = KenCraft.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public final class DashKeyHandler {
    public static final KeyMapping DASH = new KeyMapping("key.kencraft.dash", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.kencraft");
    private DashKeyHandler() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) { event.register(DASH); }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null && mc.player != null && DASH.consumeClick()) {
            if (mc.player.getData(ModAttachments.PLAYER_DATA).speed() >= 5) {
                mc.player.connection.sendCommand("kencraft dash");
            }
        }
    }
}
