package br.mevis.kencraft.client;

import br.mevis.kencraft.entity.AodaiEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public final class AodaiSnakeModel extends EntityModel<AodaiEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("kencraft", "aodai_snake"), "main");
    private final ModelPart body;
    private final ModelPart head;
    public AodaiSnakeModel(ModelPart root) { super(root); body = root.getChild("body"); head = root.getChild("head"); }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0,0).addBox(-1,-1,-4,2,2,8), PartPose.offset(0,0,0));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0,10).addBox(-1.5F,-1.5F,-3,3,3,3), PartPose.offset(0,0,-4));
        return LayerDefinition.create(mesh, 32, 32);
    }
    @Override public void setupAnim(AodaiEntity e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        body.yRot = (float)Math.sin(age * 0.35F) * 0.35F;
        head.yRot = body.yRot;
        head.xRot = (float)Math.sin(age * 0.28F) * 0.12F;
    }
}
