package br.mevis.kencraft.client;

import br.mevis.kencraft.entity.AkioGinshoEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AkioGinshoRenderer extends HumanoidMobRenderer<AkioGinshoEntity, HumanoidModel<AkioGinshoEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("kencraft", "textures/entity/akio_ginsho.png");

    public AkioGinshoRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        // The supplied Akio texture does not contain the separate outer hat layer.
        // Hide the HumanoidModel hat part so it cannot sample the wrong/black UV area.
        this.model.hat.visible = false;
    }

    @Override
    public ResourceLocation getTextureLocation(AkioGinshoEntity entity) {
        return TEXTURE;
    }
}
