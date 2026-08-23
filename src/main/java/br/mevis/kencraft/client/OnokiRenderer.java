package br.mevis.kencraft.client;

import br.mevis.kencraft.entity.OnokiEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class OnokiRenderer extends HumanoidMobRenderer<OnokiEntity, HumanoidModel<OnokiEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("kencraft", "textures/entity/onoki.png");

    public OnokiRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.model.hat.visible = false;
    }

    @Override
    public ResourceLocation getTextureLocation(OnokiEntity entity) {
        return TEXTURE;
    }
}
