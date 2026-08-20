package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class KenCraftClient {
    public static final KeyMapping OPEN_MENU = new KeyMapping("key.kencraft.menu", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.kencraft");
    public static final KeyMapping KIKAN_Z = new KeyMapping("key.kencraft.kikan_z", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.kencraft");
    public static final KeyMapping KIKAN_C = new KeyMapping("key.kencraft.kikan_c", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.kencraft");
    public static final KeyMapping JIO_F = new KeyMapping("key.kencraft.jio_f", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F, "key.categories.kencraft");
    public static final KeyMapping JIO_G = new KeyMapping("key.kencraft.jio_g", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.kencraft");
    private KenCraftClient() {}
    @SubscribeEvent public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU); event.register(KIKAN_Z); event.register(KIKAN_C); event.register(JIO_F); event.register(JIO_G);
    }
    @SubscribeEvent public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(KenCraftEntities.SHIN_HOMARE.get(), ShinHomareRenderer::new);
        event.registerEntityRenderer(KenCraftEntities.KAORI_HOMARE.get(), KaoriHomareRenderer::new);
        event.registerEntityRenderer(KenCraftEntities.ARF_GENERAL.get(), ArfGeneralRenderer::new);
        event.registerEntityRenderer(KenCraftEntities.AKIO_GINSHO.get(), AkioGinshoRenderer::new);
    }
}
