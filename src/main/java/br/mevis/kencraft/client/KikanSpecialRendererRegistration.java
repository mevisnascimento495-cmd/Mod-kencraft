package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = KenCraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class KikanSpecialRendererRegistration {
    public static final ModelLayerLocation SPECIAL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "kikan_special"), "main");

    private KikanSpecialRendererRegistration() {}

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SPECIAL_LAYER, KikanSpecialModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new KikanSpecialLayer(playerRenderer, event.getEntityModels()));
            }
        }
    }
}
