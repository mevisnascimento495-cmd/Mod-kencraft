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
    private final ModelPart tentacle5;
    private final ModelPart tentacle6;
    private final ModelPart tentacle7;
    private final ModelPart tentacle8;
    private final ModelPart tentacle9;
    private final ModelPart tentacle10;
    private final ModelPart tentacle11;
    private final ModelPart tentacle12;
    private final ModelPart tentacle13;
    private final ModelPart tentacle14;
    private final ModelPart tentacle15;
    private final ModelPart tentacle16;
    private final ModelPart scorpion1;
    private final ModelPart scorpion2;
    private final ModelPart scorpion3;
    private final ModelPart scorpion4;
    private final ModelPart scorpionStinger;
    private final ModelPart scorpionExtraLeftUpper;
    private final ModelPart scorpionExtraLeftLower;
    private final ModelPart scorpionExtraRightUpper;
    private final ModelPart scorpionExtraRightLower;
    private boolean kikakogouActive;

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
        this.tentacle5 = root.getChild("tentacle5");
        this.tentacle6 = tentacle5.getChild("tentacle6");
        this.tentacle7 = tentacle6.getChild("tentacle7");
        this.tentacle8 = tentacle7.getChild("tentacle8");
        this.tentacle9 = root.getChild("tentacle9");
        this.tentacle10 = tentacle9.getChild("tentacle10");
        this.tentacle11 = tentacle10.getChild("tentacle11");
        this.tentacle12 = tentacle11.getChild("tentacle12");
        this.tentacle13 = root.getChild("tentacle13");
        this.tentacle14 = tentacle13.getChild("tentacle14");
        this.tentacle15 = tentacle14.getChild("tentacle15");
        this.tentacle16 = tentacle15.getChild("tentacle16");
        this.scorpion1 = root.getChild("scorpion1");
        this.scorpion2 = scorpion1.getChild("scorpion2");
        this.scorpion3 = scorpion2.getChild("scorpion3");
        this.scorpion4 = scorpion3.getChild("scorpion4");
        this.scorpionStinger = scorpion4.getChild("scorpionStinger");
        this.scorpionExtraLeftUpper = root.getChild("scorpionExtraLeftUpper");
        this.scorpionExtraLeftLower = scorpionExtraLeftUpper.getChild("scorpionExtraLeftLower");
        this.scorpionExtraRightUpper = root.getChild("scorpionExtraRightUpper");
        this.scorpionExtraRightLower = scorpionExtraRightUpper.getChild("scorpionExtraRightLower");
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

        addTentacleChain(root, "tentacle5", "tentacle6", "tentacle7", "tentacle8", -3.0F, 8, 0);
        addTentacleChain(root, "tentacle9", "tentacle10", "tentacle11", "tentacle12", -1.0F, 8, 8);
        addTentacleChain(root, "tentacle13", "tentacle14", "tentacle15", "tentacle16", 1.0F, 16, 8);

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

        PartDefinition leftUpper = root.addOrReplaceChild("scorpionExtraLeftUpper",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.8F, -0.8F, -0.2F, 1.6F, 1.6F, 3.4F),
                PartPose.offset(-2.8F, -1.0F, 0.8F));
        leftUpper.addOrReplaceChild("scorpionExtraLeftLower",
                CubeListBuilder.create().texOffs(4, 0).addBox(-0.7F, -0.7F, 0F, 1.4F, 1.4F, 3.0F),
                PartPose.offset(0F, 0F, 3.2F));
        PartDefinition rightUpper = root.addOrReplaceChild("scorpionExtraRightUpper",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.8F, -0.8F, -0.2F, 1.6F, 1.6F, 3.4F),
                PartPose.offset(2.8F, -1.0F, 0.8F));
        rightUpper.addOrReplaceChild("scorpionExtraRightLower",
                CubeListBuilder.create().texOffs(4, 0).addBox(-0.7F, -0.7F, 0F, 1.4F, 1.4F, 3.0F),
                PartPose.offset(0F, 0F, 3.2F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    private static void addTentacleChain(PartDefinition root, String a, String b, String c, String d,
                                         float x, int tx, int ty) {
        PartDefinition p1 = root.addOrReplaceChild(a,
                CubeListBuilder.create().texOffs(tx, ty).addBox(-1.5F, -1.5F, 0F, 3F, 3F, 4F),
                PartPose.offset(x, 0F, 1.7F));
        PartDefinition p2 = p1.addOrReplaceChild(b,
                CubeListBuilder.create().texOffs(tx, ty + 6).addBox(-1.35F, -1.35F, 0F, 2.7F, 2.7F, 4F),
                PartPose.offset(0F, 0F, 4F));
        PartDefinition p3 = p2.addOrReplaceChild(c,
                CubeListBuilder.create().texOffs(tx, ty + 12).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 4F),
                PartPose.offset(0F, 0F, 4F));
        p3.addOrReplaceChild(d,
                CubeListBuilder.create().texOffs(tx, ty + 18).addBox(-1F, -1F, 0F, 2F, 2F, 4F),
                PartPose.offset(0F, 0F, 4F));
    }

    public void setType(String type) {
        crocodile1.visible = "CROCODILE_TAIL".equals(type);
        tentacle1.visible = "TENTACLE".equals(type);
        scorpion1.visible = "SCORPION_TAIL".equals(type);
        tentacle5.visible = tentacle6.visible = tentacle7.visible = tentacle8.visible = kikakogouActive && "TENTACLE".equals(type);
        tentacle9.visible = tentacle10.visible = tentacle11.visible = tentacle12.visible = kikakogouActive && "TENTACLE".equals(type);
        tentacle13.visible = tentacle14.visible = tentacle15.visible = tentacle16.visible = kikakogouActive && "TENTACLE".equals(type);
        scorpionExtraLeftUpper.visible = scorpionExtraLeftLower.visible = scorpionExtraRightUpper.visible = scorpionExtraRightLower.visible = kikakogouActive && "SCORPION_TAIL".equals(type);
    }

    public void setKikakogouActive(boolean active) {
        kikakogouActive = active;
    }

    public void animate(float progress, boolean heavy, String attackKey) {
        resetPose();
        float contact = KikanAnimationState.impactEnvelope();
        float p = Math.max(0.0F, Math.min(1.0F, progress));
        float power = heavy ? 1.0F : 0.82F;
        String key = attackKey == null ? "z" : attackKey;

        if (crocodile1.visible) {
            if ("z".equals(key)) {
                float whip = (float) Math.sin(p * Math.PI) * power;
                float snap = contact * contact;
                crocodile1.yRot = 0.48F * whip + 0.30F * snap;
                crocodile2.yRot = -0.86F * whip - 0.42F * snap;
                crocodile3.yRot = 1.08F * whip + 0.58F * snap;
                crocodile1.xRot = -0.24F * whip;
                crocodile2.xRot = 0.16F * whip;
                crocodile3.xRot = -0.18F * snap;
            } else {
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
            animateTentacleChain(tentacle1, tentacle2, tentacle3, tentacle4, p, contact, key, 1.0F);
            if (kikakogouActive) {
                animateTentacleChain(tentacle5, tentacle6, tentacle7, tentacle8, p, contact, key, 0.96F);
                animateTentacleChain(tentacle9, tentacle10, tentacle11, tentacle12, p, contact, key, 1.04F);
                animateTentacleChain(tentacle13, tentacle14, tentacle15, tentacle16, p, contact, key, 0.92F);
            }
            return;
        }

        if (scorpion1.visible) {
            if ("z".equals(key)) {
                float slash = (float) Math.sin(p * Math.PI);
                scorpion1.yRot = 0.52F * slash * power;
                scorpion2.yRot = -0.92F * slash * power;
                scorpion3.yRot = 1.18F * slash * power;
                scorpion4.yRot = -1.34F * slash * power;
                scorpionStinger.yRot = 1.52F * slash * power;
                scorpionStinger.xRot = -0.12F * contact;
            } else {
                float jab = (float) Math.sin(p * Math.PI * 7.0F) * (0.42F + 0.58F * contact);
                scorpion1.xRot = -0.28F * jab;
                scorpion2.xRot = 0.48F * jab;
                scorpion3.xRot = -0.68F * jab;
                scorpion4.xRot = 0.88F * jab;
                scorpionStinger.xRot = -1.16F * jab - 0.28F * contact;
                scorpionStinger.yRot = 0.18F * jab;
            }
            if (kikakogouActive) {
                float arm = (float) Math.sin(p * Math.PI) * power;
                scorpionExtraLeftUpper.xRot = -0.55F * arm;
                scorpionExtraLeftLower.xRot = 0.95F * arm;
                scorpionExtraRightUpper.xRot = -0.55F * arm;
                scorpionExtraRightLower.xRot = 0.95F * arm;
                if ("c".equals(key)) {
                    scorpionExtraLeftUpper.yRot = 0.45F * arm;
                    scorpionExtraRightUpper.yRot = -0.45F * arm;
                }
            }
        }
    }

    private static void animateTentacleChain(ModelPart a, ModelPart b, ModelPart c, ModelPart d,
                                              float p, float contact, String key, float multiplier) {
        if ("z".equals(key)) {
            float thrust = (float) Math.sin(p * Math.PI) * multiplier;
            a.yRot = 0.22F * thrust;
            b.yRot = -0.38F * thrust;
            c.yRot = 0.56F * thrust;
            d.yRot = -0.72F * thrust;
            c.xRot = -0.18F * contact;
            d.xRot = -0.24F * contact;
        } else {
            float close = (float) Math.sin(p * Math.PI * 0.5F) * multiplier;
            float hold = p > 0.58F ? (p - 0.58F) / 0.42F : 0.0F;
            a.yRot = 0.32F * close;
            b.yRot = -0.68F * close;
            c.yRot = 1.02F * close;
            d.yRot = -1.32F * close;
            a.xRot = -0.12F * close;
            b.xRot = 0.16F * close;
            c.xRot = -0.20F * close;
            d.xRot = 0.24F * close;
            if (hold > 0.0F) {
                b.yRot += 0.18F * hold;
                c.yRot -= 0.28F * hold;
            }
        }
    }

    private void resetPose() {
        reset(crocodile1); reset(crocodile2); reset(crocodile3);
        reset(tentacle1); reset(tentacle2); reset(tentacle3); reset(tentacle4);
        reset(tentacle5); reset(tentacle6); reset(tentacle7); reset(tentacle8);
        reset(tentacle9); reset(tentacle10); reset(tentacle11); reset(tentacle12);
        reset(tentacle13); reset(tentacle14); reset(tentacle15); reset(tentacle16);
        reset(scorpion1); reset(scorpion2); reset(scorpion3); reset(scorpion4); reset(scorpionStinger);
        reset(scorpionExtraLeftUpper); reset(scorpionExtraLeftLower); reset(scorpionExtraRightUpper); reset(scorpionExtraRightLower);
    }

    private static void reset(ModelPart part) {
        part.xRot = part.yRot = part.zRot = 0.0F;
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
        // Combat animation is driven by KikanAnimationState.
    }
}
