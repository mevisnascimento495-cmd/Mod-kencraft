package br.mevis.kencraft.client;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;

public final class KikanSpecialModel extends HierarchicalModel<AbstractClientPlayer> {
    private final ModelPart root;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingTip;
    private final ModelPart lizardTail1;
    private final ModelPart lizardTail2;
    private final ModelPart lizardTail3;

    public KikanSpecialModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        leftWing = root.getChild("leftWing");
        rightWing = root.getChild("rightWing");
        leftWingTip = leftWing.getChild("leftWingTip");
        rightWingTip = rightWing.getChild("rightWingTip");
        lizardTail1 = root.getChild("lizardTail1");
        lizardTail2 = lizardTail1.getChild("lizardTail2");
        lizardTail3 = lizardTail2.getChild("lizardTail3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition left = root.addOrReplaceChild("leftWing",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.35F, -5.0F, -0.5F, 7.0F, 10.0F, 0.6F),
                PartPose.offsetAndRotation(-1.6F, -2.0F, 1.8F, 0.0F, 0.0F, 0.22F));
        left.addOrReplaceChild("leftWingTip",
                CubeListBuilder.create().texOffs(0, 11).addBox(-0.25F, -4.0F, -0.3F, 5.0F, 8.0F, 0.5F),
                PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.18F));

        PartDefinition right = root.addOrReplaceChild("rightWing",
                CubeListBuilder.create().texOffs(16, 0).addBox(-6.65F, -5.0F, -0.5F, 7.0F, 10.0F, 0.6F),
                PartPose.offsetAndRotation(1.6F, -2.0F, 1.8F, 0.0F, 0.0F, -0.22F));
        right.addOrReplaceChild("rightWingTip",
                CubeListBuilder.create().texOffs(16, 11).addBox(-4.75F, -4.0F, -0.3F, 5.0F, 8.0F, 0.5F),
                PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.18F));

        PartDefinition tail1 = root.addOrReplaceChild("lizardTail1",
                CubeListBuilder.create().texOffs(0, 20).addBox(-1.8F, -1.8F, 0.0F, 3.6F, 3.6F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 1.4F));
        PartDefinition tail2 = tail1.addOrReplaceChild("lizardTail2",
                CubeListBuilder.create().texOffs(0, 29).addBox(-1.45F, -1.45F, 0.0F, 2.9F, 2.9F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 5.0F));
        tail2.addOrReplaceChild("lizardTail3",
                CubeListBuilder.create().texOffs(16, 29).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 5.0F));

        return LayerDefinition.create(mesh, 32, 38);
    }

    public void setType(String type, boolean active) {
        boolean butterfly = "BUTTERFLY_TENTACLE".equalsIgnoreCase(type);
        boolean lizard = "LIZARD_CROCODILE_TAIL".equalsIgnoreCase(type);
        leftWing.visible = rightWing.visible = leftWingTip.visible = rightWingTip.visible = butterfly;
        lizardTail1.visible = lizardTail2.visible = lizardTail3.visible = lizard;
        if (active) {
            leftWingTip.yRot = rightWingTip.yRot = 0.08F;
        } else {
            leftWingTip.yRot = rightWingTip.yRot = 0.0F;
        }
    }

    public void animate(float ageInTicks) {
        float flap = (float)Math.sin(ageInTicks * 0.55F);
        leftWing.zRot = 0.22F + flap * 0.38F;
        rightWing.zRot = -0.22F - flap * 0.38F;
        leftWingTip.zRot = 0.18F + flap * 0.22F;
        rightWingTip.zRot = -0.18F - flap * 0.22F;

        float tail = (float)Math.sin(ageInTicks * 0.22F) * 0.24F;
        lizardTail1.yRot = tail;
        lizardTail2.yRot = -tail * 1.35F;
        lizardTail3.yRot = tail * 1.7F;
    }

    @Override
    public void setupAnim(AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // The Kikan overlay has its own animation state. Resetting the model here
        // keeps the layer deterministic before KikanSpecialLayer applies its state.
        root().getAllParts().forEach(ModelPart::resetPose);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
