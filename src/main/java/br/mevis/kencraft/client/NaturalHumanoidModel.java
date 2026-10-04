package br.mevis.kencraft.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Subtle secondary motion for KenCraft humanoids.
 * Keeps vanilla compatibility while making walking/turning feel less rigid.
 */
public class NaturalHumanoidModel<T extends LivingEntity> extends HumanoidModel<T> {
    public NaturalHumanoidModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float cycle = limbSwing * 0.6662F;
        float sway = Mth.cos(cycle) * 0.035F * motion;
        float counterSway = Mth.sin(cycle) * 0.025F * motion;

        body.yRot += sway;
        body.xRot += 0.035F * motion;

        rightArm.zRot += counterSway;
        leftArm.zRot -= counterSway;

        if (entity.isSprinting()) {
            body.xRot += 0.045F;
            rightArm.xRot -= 0.08F;
            leftArm.xRot -= 0.08F;
        }

        float idle = Mth.sin(ageInTicks * 0.08F) * 0.012F;
        head.yRot += idle;
    }
}
