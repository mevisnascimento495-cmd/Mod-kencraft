package br.mevis.kencraft.client;

import br.mevis.kencraft.entity.ArfInvestigatorEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renderer for ARF investigators. */
public class ArfInvestigatorRenderer extends HumanoidMobRenderer<ArfInvestigatorEntity, HumanoidModel<ArfInvestigatorEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("kencraft", "textures/entity/arf.png");

    public ArfInvestigatorRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ArfInvestigatorEntity entity) {
        return TEXTURE;
    }
}
