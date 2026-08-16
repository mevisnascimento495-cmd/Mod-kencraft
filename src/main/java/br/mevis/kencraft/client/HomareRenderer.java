package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.KenCraftEntities;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class HomareRenderer extends HumanoidMobRenderer<KenCraftEntities.HomareEntity, PlayerModel<KenCraftEntities.HomareEntity>> {
    private static final ResourceLocation SHIN_TEXTURE = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "textures/entity/shin_homare.png");
    private static final ResourceLocation KAORI_TEXTURE = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "textures/entity/kaori_homare.png");

    public HomareRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KenCraftEntities.HomareEntity entity) {
        return entity.getType() == KenCraftEntities.KAORI_HOMARE.get() ? KAORI_TEXTURE : SHIN_TEXTURE;
    }
}
