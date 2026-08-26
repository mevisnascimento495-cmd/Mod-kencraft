package br.mevis.kencraft.client;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Registers the player layer used by the spiritual-state system on both vanilla skin models. */
@EventBusSubscriber(modid = "kencraft", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SpiritualStateClient {
    private SpiritualStateClient() {}

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new JioAuraLayer(renderer, event.getEntityModels()));
            }
        }
    }
}
