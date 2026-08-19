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
    private final ModelPart crocodile1, crocodile2, crocodile3;
    private final ModelPart tentacle1, tentacle2, tentacle3, tentacle4;
    private final ModelPart tentacle5, tentacle6, tentacle7, tentacle8;
    private final ModelPart tentacle9, tentacle10, tentacle11, tentacle12;
    private final ModelPart tentacle13, tentacle14, tentacle15, tentacle16;
    private final ModelPart scorpion1, scorpion2, scorpion3, scorpion4, scorpionStinger;
    private final ModelPart scorpionArmLeftUpper, scorpionArmLeftLower;
    private final ModelPart scorpionArmRightUpper, scorpionArmRightLower;
    private boolean kikakogouActive;

    public KikanModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        crocodile1 = root.getChild("crocodile1");
        crocodile2 = crocodile1.getChild("crocodile2");
        crocodile3 = crocodile2.getChild("crocodile3");
        tentacle1 = root.getChild("tentacle1");
        tentacle2 = tentacle1.getChild("tentacle2");
        tentacle3 = tentacle2.getChild("tentacle3");
        tentacle4 = tentacle3.getChild("tentacle4");
        tentacle5 = root.getChild("tentacle5");
        tentacle6 = tentacle5.getChild("tentacle6");
        tentacle7 = tentacle6.getChild("tentacle7");
        tentacle8 = tentacle7.getChild("tentacle8");
        tentacle9 = root.getChild("tentacle9");
        tentacle10 = tentacle9.getChild("tentacle10");
        tentacle11 = tentacle10.getChild("tentacle11");
        tentacle12 = tentacle11.getChild("tentacle12");
        tentacle13 = root.getChild("tentacle13");
        tentacle14 = tentacle13.getChild("tentacle14");
        tentacle15 = tentacle14.getChild("tentacle15");
        tentacle16 = tentacle15.getChild("tentacle16");
        scorpion1 = root.getChild("scorpion1");
        scorpion2 = scorpion1.getChild("scorpion2");
        scorpion3 = scorpion2.getChild("scorpion3");
        scorpion4 = scorpion3.getChild("scorpion4");
        scorpionStinger = scorpion4.getChild("scorpionStinger");
        scorpionArmLeftUpper = root.getChild("scorpionArmLeftUpper");
        scorpionArmLeftLower = scorpionArmLeftUpper.getChild("scorpionArmLeftLower");
        scorpionArmRightUpper = root.getChild("scorpionArmRightUpper");
        scorpionArmRightLower = scorpionArmRightUpper.getChild("scorpionArmRightLower");
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

        addTentacleChain(root, "tentacle1", "tentacle2", "tentacle3", "tentacle4", 0, 0);
        addTentacleChain(root, "tentacle5", "tentacle6", "tentacle7", "tentacle8", 0, 8);
        addTentacleChain(root, "tentacle9", "tentacle10", "tentacle11", "tentacle12", 8, 0);
        addTentacleChain(root, "tentacle13", "tentacle14", "tentacle15", "tentacle16", 8, 8);

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

        PartDefinition leftUpper = root.addOrReplaceChild("scorpionArmLeftUpper",
                CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -1.5F, -0.5F, 3F, 4.5F, 3F),
                PartPose.offset(-2.7F, -1.5F, 0F));
        leftUpper.addOrReplaceChild("scorpionArmLeftLower",
                CubeListBuilder.create().texOffs(12, 28).addBox(-1.35F, -1.35F, 0F, 2.7F, 4F, 2.7F),
                PartPose.offset(0F, 4F, 0.2F));
        PartDefinition rightUpper = root.addOrReplaceChild("scorpionArmRightUpper",
                CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -1.5F, -0.5F, 3F, 4.5F, 3F),
                PartPose.offset(2.7F, -1.5F, 0F));
        rightUpper.addOrReplaceChild("scorpionArmRightLower",
                CubeListBuilder.create().texOffs(12, 28).addBox(-1.35F, -1.35F, 0F, 2.7F, 4F, 2.7F),
                PartPose.offset(0F, 4F, 0.2F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    private static void addTentacleChain(PartDefinition root, String a, String b, String c, String d, int tx, int ty) {
        PartDefinition p1 = root.addOrReplaceChild(a,
                CubeListBuilder.create().texOffs(tx, ty).addBox(-1.35F, -1.35F, 0F, 2.7F, 2.7F, 3F),
                PartPose.offset(0F, 0F, 1.0F));
        PartDefinition p2 = p1.addOrReplaceChild(b,
                CubeListBuilder.create().texOffs(tx, ty + 6).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 3F),
                PartPose.offset(0F, 0F, 3F));
        PartDefinition p3 = p2.addOrReplaceChild(c,
                CubeListBuilder.create().texOffs(tx, ty + 12).addBox(-1.05F, -1.05F, 0F, 2.1F, 2.1F, 3F),
                PartPose.offset(0F, 0F, 3F));
        p3.addOrReplaceChild(d,
                CubeListBuilder.create().texOffs(tx, ty + 18).addBox(-0.9F, -0.9F, 0F, 1.8F, 1.8F, 3F),
                PartPose.offset(0F, 0F, 3F));
    }

    public void setType(String type) {
        boolean crocodile = "CROCODILE_TAIL".equalsIgnoreCase(type);
        boolean tentacle = "TENTACLE".equalsIgnoreCase(type);
        boolean scorpion = "SCORPION_TAIL".equalsIgnoreCase(type) || "SCORPION".equalsIgnoreCase(type);

        crocodile1.visible = crocodile;
        tentacle1.visible = tentacle;
        scorpion1.visible = scorpion;

        boolean extraTentacles = kikakogouActive && tentacle;
        tentacle5.visible = tentacle6.visible = tentacle7.visible = tentacle8.visible = extraTentacles;
        tentacle9.visible = tentacle10.visible = tentacle11.visible = tentacle12.visible = extraTentacles;
        tentacle13.visible = tentacle14.visible = tentacle15.visible = tentacle16.visible = extraTentacles;

        boolean extraScorpionArms = kikakogouActive && scorpion;
        scorpionArmLeftUpper.visible = scorpionArmLeftLower.visible = extraScorpionArms;
        scorpionArmRightUpper.visible = scorpionArmRightLower.visible = extraScorpionArms;

        if (tentacle) {
            if (kikakogouActive) {
                positionTentacleRoot(tentacle1, -2.25F, 0F, 0.9F);
                positionTentacleRoot(tentacle5, -0.75F, 0F, 0.9F);
                positionTentacleRoot(tentacle9, 0.75F, 0F, 0.9F);
                positionTentacleRoot(tentacle13, 2.25F, 0F, 0.9F);
            } else {
                positionTentacleRoot(tentacle1, 0F, 0F, 1.5F);
            }
        }
    }

    private static void positionTentacleRoot(ModelPart part, float x, float y, float z) {
        part.x = x;
        part.y = y;
        part.z = z;
    }

    public void setKikakogouActive(boolean active) {
        kikakogouActive = active;
    }

    public void animate(float progress, boolean heavy, String attackKey) {
        resetPose();
        float contact = KikanAnimationState.impactEnvelope();
        float p = Math.max(0F, Math.min(1F, progress));
        float power = heavy ? 1F : 0.82F;
        String key = attackKey == null ? "z" : attackKey.toLowerCase();

        if (crocodile1.visible) {
            float whip = (float)Math.sin(p * Math.PI) * power;
            crocodile1.yRot = 0.48F * whip + 0.30F * contact;
            crocodile2.yRot = -0.86F * whip - 0.42F * contact;
            crocodile3.yRot = 1.08F * whip + 0.58F * contact;
            crocodile1.xRot = -0.24F * whip;
            crocodile2.xRot = 0.16F * whip;
            crocodile3.xRot = -0.18F * contact;
            return;
        }

        if (tentacle1.visible) {
            animateTentacleChain(tentacle1, tentacle2, tentacle3, tentacle4, p, contact, key, 1.0F, -0.28F);
            if (kikakogouActive) {
                animateTentacleChain(tentacle5, tentacle6, tentacle7, tentacle8, p, contact, key, 0.96F, -0.10F);
                animateTentacleChain(tentacle9, tentacle10, tentacle11, tentacle12, p, contact, key, 1.04F, 0.10F);
                animateTentacleChain(tentacle13, tentacle14, tentacle15, tentacle16, p, contact, key, 0.92F, 0.28F);
            }
            return;
        }

        if (scorpion1.visible) {
            if ("z".equals(key)) {
                float slash = (float)Math.sin(p * Math.PI);
                scorpion1.yRot = 0.52F * slash * power;
                scorpion2.yRot = -0.92F * slash * power;
                scorpion3.yRot = 1.18F * slash * power;
                scorpion4.yRot = -1.34F * slash * power;
                scorpionStinger.yRot = 1.52F * slash * power;
            } else {
                float jab = (float)Math.sin(p * Math.PI * 7F) * (0.42F + 0.58F * contact);
                scorpion1.xRot = -0.28F * jab;
                scorpion2.xRot = 0.48F * jab;
                scorpion3.xRot = -0.68F * jab;
                scorpion4.xRot = 0.88F * jab;
                scorpionStinger.xRot = -1.16F * jab - 0.28F * contact;
            }
            if (kikakogouActive) {
                float arm = (float)Math.sin(p * Math.PI) * power;
                scorpionArmLeftUpper.xRot = -0.45F * arm;
                scorpionArmLeftLower.xRot = 0.75F * arm;
                scorpionArmRightUpper.xRot = -0.45F * arm;
                scorpionArmRightLower.xRot = 0.75F * arm;
                if ("c".equals(key)) {
                    scorpionArmLeftUpper.yRot = 0.55F * arm;
                    scorpionArmRightUpper.yRot = -0.55F * arm;
                }
            }
        }
    }

    private static void animateTentacleChain(ModelPart a, ModelPart b, ModelPart c, ModelPart d,
                                              float p, float contact, String key, float multiplier, float splay) {
        if ("z".equals(key)) {
            float thrust = (float)Math.sin(p * Math.PI) * multiplier;
            a.yRot = splay + 0.18F * thrust;
            b.yRot = -0.30F * thrust;
            c.yRot = 0.46F * thrust;
            d.yRot = -0.60F * thrust;
            d.xRot = -0.20F * contact;
        } else {
            float close = (float)Math.sin(p * Math.PI * 0.5F) * multiplier;
            a.yRot = splay + 0.24F * close;
            b.yRot = -0.54F * close;
            c.yRot = 0.78F * close;
            d.yRot = -1.00F * close;
            a.xRot = -0.10F * close;
            b.xRot = 0.14F * close;
            c.xRot = -0.18F * close;
            d.xRot = 0.22F * close;
            if (p > 0.58F) {
                float hold = (p - 0.58F) / 0.42F;
                c.yRot -= 0.20F * hold;
                d.yRot += 0.24F * hold;
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
        reset(scorpionArmLeftUpper); reset(scorpionArmLeftLower);
        reset(scorpionArmRightUpper); reset(scorpionArmRightLower);
    }

    private static void reset(ModelPart part) {
        part.xRot = 0F;
        part.yRot = 0F;
        part.zRot = 0F;
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
    public void setupAnim(AbstractClientPlayer entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
