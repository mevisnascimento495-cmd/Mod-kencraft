package br.mevis.kencraft.client;

import br.mevis.kencraft.entity.ArfGeneralEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ArfGeneralRenderer extends HumanoidMobRenderer<ArfGeneralEntity, HumanoidModel<ArfGeneralEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("kencraft", "textures/entity/akio_ginsho.png");

    public ArfGeneralRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ArfGeneralEntity entity) {
        return TEXTURE;
    }
}
