package br.mevis.kencraft.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.CubeListBuilder;
import net.minecraft.client.model.geom.LayerDefinition;
import net.minecraft.client.model.geom.MeshDefinition;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.player.AbstractClientPlayer;

public final class KikanModel extends EntityModel<AbstractClientPlayer> {
    private final ModelPart root;
    private final ModelPart crocodile1;
    private final ModelPart crocodile2;
    private final ModelPart crocodile3;
    private final ModelPart tentacle1;
    private final ModelPart tentacle2;
    private final ModelPart tentacle3;
    private final ModelPart tentacle4;
    private final ModelPart scorpion1;
    private final ModelPart scorpion2;
    private final ModelPart scorpion3;
    private final ModelPart scorpion4;
    private final ModelPart scorpionStinger;
    private String type = "";

    public KikanModel(ModelPart root) {
        super(root, RenderType::entityCutoutNoCull);
        this.root = root;
        this.crocodile1 = root.getChild("crocodile1");
        this.crocodile2 = crocodile1.getChild("crocodile2");
        this.crocodile3 = crocodile2.getChild("crocodile3");
        this.tentacle1 = root.getChild("tentacle1");
        this.tentacle2 = tentacle1.getChild("tentacle2");
        this.tentacle3 = tentacle2.getChild("tentacle3");
        this.tentacle4 = tentacle3.getChild("tentacle4");
        this.scorpion1 = root.getChild("scorpion1");
        this.scorpion2 = scorpion1.getChild("scorpion2");
        this.scorpion3 = scorpion2.getChild("scorpion3");
        this.scorpion4 = scorpion3.getChild("scorpion4");
        this.scorpionStinger = scorpion4.getChild("scorpionStinger");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition c1 = root.addOrReplaceChild("crocodile1",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.2F, -2.2F, 0F, 4.4F, 4.4F, 5F),
                PartPose.offset(0F, 0F, 1.5F));
        PartDefinition c2 = c1.addOrReplaceChild("crocodile2",
                CubeListBuilder.create().texOffs(0, 8).addBox(-2F, -2F, 0F, 4F, 4F, 5F),
                PartPose.offset(0F, 0F, 5F));
        c2.addOrReplaceChild("crocodile3",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 5F),
                PartPose.offset(0F, 0F, 5F));

        PartDefinition t1 = root.addOrReplaceChild("tentacle1",
                CubeListBuilder.create().texOffs(16, 0).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 4F),
                PartPose.offset(0F, 0F, 1.5F));
        PartDefinition t2 = t1.addOrReplaceChild("tentacle2",
                CubeListBuilder.create().texOffs(16, 6).addBox(-1.35F, -1.35F, 0F, 2.7F, 2.7F, 4F),
                PartPose.offset(0F, 0F, 4F));
        PartDefinition t3 = t2.addOrReplaceChild("tentacle3",
                CubeListBuilder.create().texOffs(16, 12).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 4F),
                PartPose.offset(0F, 0F, 4F));
        t3.addOrReplaceChild("tentacle4",
                CubeListBuilder.create().texOffs(16, 18).addBox(-1F, -1F, 0F, 2F, 2F, 4F),
                PartPose.offset(0F, 0F, 4F));

        PartDefinition s1 = root.addOrReplaceChild("scorpion1",
                CubeListBuilder.create().texOffs(0, 24).addBox(-2F, -2F, 0F, 4F, 4F, 4F),
                PartPose.offset(0F, 0F, 1.5F));
        PartDefinition s2 = s1.addOrReplaceChild("scorpion2",
                CubeListBuilder.create().texOffs(8, 24).addBox(-1.7F, -1.7F, 0F, 3.4F, 3.4F, 4F),
                PartPose.offset(0F, 0F, 4F));
        PartDefinition s3 = s2.addOrReplaceChild("scorpion3",
                CubeListBuilder.create().texOffs(16, 24).addBox(-1.4F, -1.4F, 0F, 2.8F, 2.8F, 4F),
                PartPose.offset(0F, 0F, 4F));
        PartDefinition s4 = s3.addOrReplaceChild("scorpion4",
                CubeListBuilder.create().texOffs(24, 24).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 4F),
                PartPose.offset(0F, 0F, 4F));
        s4.addOrReplaceChild("scorpionStinger",
                CubeListBuilder.create().texOffs(28, 0).addBox(-1F, -1F, 0F, 2F, 2F, 3F),
                PartPose.offset(0F, -1.5F, 4F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    public void setType(String type) {
        this.type = type == null ? "" : type;
        crocodile1.visible = type.equals("CROCODILE_TAIL");
        tentacle1.visible = type.equals("TENTACLE");
        scorpion1.visible = type.equals("SCORPION_TAIL");
    }

    public void animate(float progress, boolean heavy) {
        float swing = (float) Math.sin(progress * Math.PI);
        float power = heavy ? 1.0F : 0.7F;
        crocodile1.yRot = swing * 0.35F * power;
        crocodile1.xRot = swing * 0.18F * power;
        crocodile2.yRot = swing * -0.45F * power;
        crocodile3.yRot = swing * 0.55F * power;

        tentacle1.yRot = swing * 0.45F * power;
        tentacle2.yRot = swing * -0.65F * power;
        tentacle3.yRot = swing * 0.75F * power;
        tentacle4.yRot = swing * -0.85F * power;

        scorpion1.xRot = swing * 0.2F * power;
        scorpion2.xRot = swing * -0.3F * power;
        scorpion3.xRot = swing * 0.45F * power;
        scorpion4.xRot = swing * -0.6F * power;
        scorpionStinger.xRot = swing * 0.8F * power;
    }

    @Override
    public void setupAnim(AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // The Kikan layer has its own lightweight animation controller.
    }
}
