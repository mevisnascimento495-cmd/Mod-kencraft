package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.AodaiEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;

public final class AodaiSnakeLayer extends RenderLayer<AodaiEntity, net.minecraft.client.model.HumanoidModel<AodaiEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID, "textures/entity/aodai_snake.png");
    private final AodaiSnakeModel model;
    public AodaiSnakeLayer(RenderLayerParent<AodaiEntity, net.minecraft.client.model.HumanoidModel<AodaiEntity>> parent, EntityRendererProvider.Context ctx) {
        super(parent); model = new AodaiSnakeModel(ctx.bakeLayer(AodaiSnakeModel.LAYER));
    }
    @Override public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AodaiEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isSnakeActive()) return;
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.15D, 0.28D);
        poseStack.scale(0.9F, 0.9F, 0.9F);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vc, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 1,1,1,1);
        poseStack.popPose();
    }
}
