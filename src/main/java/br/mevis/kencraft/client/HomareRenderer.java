package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class HomareRenderer extends MobRenderer<KenCraftEntities.HomareEntity, HumanoidModel<KenCraftEntities.HomareEntity>> {
    private static final ResourceLocation SHIN_TEXTURE = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "textures/entity/shin_homare.png");
    private static final ResourceLocation KAORI_TEXTURE = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "textures/entity/kaori_homare.png");

    public HomareRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KenCraftEntities.HomareEntity entity) {
        return entity.getType() == KenCraftEntities.KAORI_HOMARE.get() ? KAORI_TEXTURE : SHIN_TEXTURE;
    }
}
