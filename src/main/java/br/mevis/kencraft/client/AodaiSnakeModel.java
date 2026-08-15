package br.mevis.kencraft.client;

import br.mevis.kencraft.entity.AodaiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class AodaiSnakeModel extends HierarchicalModel<AodaiEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("kencraft", "aodai_snake"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;

    public AodaiSnakeModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1F, -1F, -4F, 2F, 2F, 8F),
                PartPose.offset(0F, 0F, 0F));
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, -1.5F, -3F, 3F, 3F, 3F),
                PartPose.offset(0F, 0F, -4F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(AodaiEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        body.yRot = (float) Math.sin(ageInTicks * 0.35F) * 0.35F;
        head.yRot = body.yRot;
        head.xRot = (float) Math.sin(ageInTicks * 0.28F) * 0.12F;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}