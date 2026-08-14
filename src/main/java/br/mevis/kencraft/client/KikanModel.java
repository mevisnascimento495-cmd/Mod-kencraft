package br.mevis.kencraft.client;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class KikanModel extends HierarchicalModel<AbstractClientPlayer> {
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

    public KikanModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
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
        crocodile1.visible = "CROCODILE_TAIL".equals(type);
        tentacle1.visible = "TENTACLE".equals(type);
        scorpion1.visible = "SCORPION_TAIL".equals(type);
    }

    public void animate(float progress, boolean heavy, String attackKey) {
        resetPose();
        float contact = KikanAnimationState.impactEnvelope();
        float p = Math.max(0.0F, Math.min(1.0F, progress));
        float power = heavy ? 1.0F : 0.82F;
        String key = attackKey == null ? "z" : attackKey;

        if (crocodile1.visible) {
            if ("z".equals(key)) {
                // Short whip: the chain snaps forward and recoils immediately at contact.
                float whip = (float) Math.sin(p * Math.PI) * power;
                float snap = contact * contact;
                crocodile1.yRot = 0.48F * whip + 0.30F * snap;
                crocodile2.yRot = -0.86F * whip - 0.42F * snap;
                crocodile3.yRot = 1.08F * whip + 0.58F * snap;
                crocodile1.xRot = -0.24F * whip;
                crocodile2.xRot = 0.16F * whip;
                crocodile3.xRot = -0.18F * snap;
            } else {
                // Ground smash: tail drops toward the ground, then kicks back up with impact.
                float slam = (float) Math.sin(p * Math.PI);
                crocodile1.xRot = 0.95F * slam * power;
                crocodile2.xRot = -1.18F * slam * power;
                crocodile3.xRot = 1.24F * slam * power;
                crocodile1.yRot = 0.18F * contact;
                crocodile2.yRot = -0.28F * contact;
                crocodile3.yRot = 0.36F * contact;
            }
            return;
        }

        if (tentacle1.visible) {
            if ("z".equals(key)) {
                // Penetrating thrust: every segment follows the previous one so the tip reaches the target.
                float thrust = (float) Math.sin(p * Math.PI);
                tentacle1.yRot = 0.22F * thrust;
                tentacle2.yRot = -0.38F * thrust;
                tentacle3.yRot = 0.56F * thrust;
                tentacle4.yRot = -0.72F * thrust;
                tentacle3.xRot = -0.18F * contact;
                tentacle4.xRot = -0.24F * contact;
            } else {
                // Grab: the four segments curl around the target and hold at maximum contact.
                float close = (float) Math.sin(p * Math.PI * 0.5F);
                float hold = p > 0.58F ? (p - 0.58F) / 0.42F : 0.0F;
                tentacle1.yRot = 0.32F * close;
                tentacle2.yRot = -0.68F * close;
                tentacle3.yRot = 1.02F * close;
                tentacle4.yRot = -1.32F * close;
                tentacle1.xRot = -0.12F * close;
                tentacle2.xRot = 0.16F * close;
                tentacle3.xRot = -0.20F * close;
                tentacle4.xRot = 0.24F * close;
                if (hold > 0.0F) {
                    tentacle2.yRot += 0.18F * hold;
                    tentacle3.yRot -= 0.28F * hold;
                }
            }
            return;
        }

        if (scorpion1.visible) {
            if ("z".equals(key)) {
                // Horizontal scything tail passes through the target once.
                float slash = (float) Math.sin(p * Math.PI);
                scorpion1.yRot = 0.52F * slash * power;
                scorpion2.yRot = -0.92F * slash * power;
                scorpion3.yRot = 1.18F * slash * power;
                scorpion4.yRot = -1.34F * slash * power;
                scorpionStinger.yRot = 1.52F * slash * power;
                scorpionStinger.xRot = -0.12F * contact;
            } else {
                // Multiple stabs: a visible repeating thrust with a strong contact pulse.
                float jab = (float) Math.sin(p * Math.PI * 7.0F) * (0.42F + 0.58F * contact);
                scorpion1.xRot = -0.28F * jab;
                scorpion2.xRot = 0.48F * jab;
                scorpion3.xRot = -0.68F * jab;
                scorpion4.xRot = 0.88F * jab;
                scorpionStinger.xRot = -1.16F * jab - 0.28F * contact;
                scorpionStinger.yRot = 0.18F * jab;
            }
        }
    }

    private void resetPose() {
        crocodile1.xRot = crocodile1.yRot = crocodile1.zRot = 0.0F;
        crocodile2.xRot = crocodile2.yRot = crocodile2.zRot = 0.0F;
        crocodile3.xRot = crocodile3.yRot = crocodile3.zRot = 0.0F;
        tentacle1.xRot = tentacle1.yRot = tentacle1.zRot = 0.0F;
        tentacle2.xRot = tentacle2.yRot = tentacle2.zRot = 0.0F;
        tentacle3.xRot = tentacle3.yRot = tentacle3.zRot = 0.0F;
        tentacle4.xRot = tentacle4.yRot = tentacle4.zRot = 0.0F;
        scorpion1.xRot = scorpion1.yRot = scorpion1.zRot = 0.0F;
        scorpion2.xRot = scorpion2.yRot = scorpion2.zRot = 0.0F;
        scorpion3.xRot = scorpion3.yRot = scorpion3.zRot = 0.0F;
        scorpion4.xRot = scorpion4.yRot = scorpion4.zRot = 0.0F;
        scorpionStinger.xRot = scorpionStinger.yRot = scorpionStinger.zRot = 0.0F;
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Combat animation is intentionally driven by KikanAnimationState.
    }
}
