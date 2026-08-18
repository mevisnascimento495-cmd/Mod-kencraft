package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class KikakogouKeyHandler {
    public static final KeyMapping KIKAKOGOU = new KeyMapping("key.kencraft.kikakogou", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.kencraft");
    private KikakogouKeyHandler() {}
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) { event.register(KIKAKOGOU); }
}
