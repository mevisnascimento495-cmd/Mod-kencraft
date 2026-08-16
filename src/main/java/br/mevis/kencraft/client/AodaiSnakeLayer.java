package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.AodaiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public final class AodaiSnakeLayer extends RenderLayer<AodaiEntity, net.minecraft.client.model.HumanoidModel<AodaiEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            KenCraft.MOD_ID, "textures/entity/aodai_snake.png");
    private final AodaiSnakeModel model;

    public AodaiSnakeLayer(RenderLayerParent<AodaiEntity, net.minecraft.client.model.HumanoidModel<AodaiEntity>> parent,
                           EntityRendererProvider.Context context) {
        super(parent);
        this.model = new AodaiSnakeModel(context.bakeLayer(AodaiSnakeModel.LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AodaiEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!entity.isSnakeActive()) return;

        poseStack.pushPose();
        // Start at Aodai's back and make the serpent clearly visible over the shoulders.
        poseStack.translate(0.0D, 0.05D, 0.32D);
        poseStack.scale(1.15F, 1.15F, 1.15F);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1);
        poseStack.popPose();
    }
}