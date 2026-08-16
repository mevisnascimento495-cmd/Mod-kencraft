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
    private final ModelPart segment0;
    private final ModelPart segment1;
    private final ModelPart segment2;
    private final ModelPart segment3;
    private final ModelPart segment4;
    private final ModelPart head;

    public AodaiSnakeModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.segment0 = root.getChild("segment0");
        this.segment1 = root.getChild("segment1");
        this.segment2 = root.getChild("segment2");
        this.segment3 = root.getChild("segment3");
        this.segment4 = root.getChild("segment4");
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("segment0",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 5F),
                PartPose.offset(0F, 0F, 0F));
        root.addOrReplaceChild("segment1",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.6F, -1.6F, 0F, 3.2F, 3.2F, 5F),
                PartPose.offset(0F, 0F, 4.5F));
        root.addOrReplaceChild("segment2",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.7F, -1.7F, 0F, 3.4F, 3.4F, 5F),
                PartPose.offset(0F, 0F, 9F));
        root.addOrReplaceChild("segment3",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.7F, -1.7F, 0F, 3.4F, 3.4F, 5F),
                PartPose.offset(0F, 0F, 13.5F));
        root.addOrReplaceChild("segment4",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 4.5F),
                PartPose.offset(0F, 0F, 18F));

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -2F, 0F, 5F, 4F, 6F),
                PartPose.offset(0F, 0F, 21.5F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(AodaiEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float wave = ageInTicks * 0.22F;
        segment0.yRot = (float) Math.sin(wave) * 0.20F;
        segment1.yRot = (float) Math.sin(wave + 0.65F) * 0.24F;
        segment2.yRot = (float) Math.sin(wave + 1.30F) * 0.28F;
        segment3.yRot = (float) Math.sin(wave + 1.95F) * 0.30F;
        segment4.yRot = (float) Math.sin(wave + 2.60F) * 0.32F;
        head.yRot = segment4.yRot;
        head.xRot = (float) Math.sin(ageInTicks * 0.16F) * 0.10F;
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