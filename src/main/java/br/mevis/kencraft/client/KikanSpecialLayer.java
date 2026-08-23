package br.mevis.kencraft.client;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class KikanSpecialLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("kencraft", "textures/entity/kikan.png");
    private final KikanSpecialModel model;

    public KikanSpecialLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                             EntityModelSet modelSet) {
        super(parent);
        this.model = new KikanSpecialModel(modelSet.bakeLayer(KikanSpecialRendererRegistration.SPECIAL_LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.RINKA) return;

        boolean butterfly = "BUTTERFLY_TENTACLE".equalsIgnoreCase(data.kikanType());
        boolean lizard = "LIZARD_CROCODILE_TAIL".equalsIgnoreCase(data.kikanType());
        if (!butterfly && !lizard) return;

        boolean active = player.getData(ModAttachments.KIKAKOGOU_STATE).active();
        model.setType(data.kikanType(), active);
        model.animate(ageInTicks);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.72D, 0.24D);
        model.renderToBuffer(poseStack,
                bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
