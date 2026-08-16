package br.mevis.kencraft.client;

import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public final class HomareRenderer extends MobRenderer<KenCraftEntities.HomareEntity, HumanoidModel<KenCraftEntities.HomareEntity>> {
    private static final ResourceLocation DEFAULT_TEXTURE = DefaultPlayerSkin.getDefaultTexture();
    public HomareRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)), 0.5F);
    }
    @Override public ResourceLocation getTextureLocation(KenCraftEntities.HomareEntity entity) { return DEFAULT_TEXTURE; }
}
