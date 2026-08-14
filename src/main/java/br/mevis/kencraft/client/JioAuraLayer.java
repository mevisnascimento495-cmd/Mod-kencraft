package br.mevis.kencraft.client;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Blue translucent skin layer used by Seishin dan ability 3. */
public final class JioAuraLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final PlayerModel<AbstractClientPlayer> auraModel;

    public JioAuraLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                        EntityModelSet modelSet) {
        super(parent);
        this.auraModel = new PlayerModel<>(modelSet.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER), false);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        if (data.race() != Race.HUMAN) return;
        if (!"Seishin dan".equalsIgnoreCase(PlayerData.normalizeTechnique(data.jioTechnique()))) return;
        if (data.jioAbilitySlot() != 2 || !JioAnimationState.active()) return;

        getParentModel().copyPropertiesTo(auraModel);
        auraModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        poseStack.pushPose();
        float pulse = 0.78F + 0.18F * (float) Math.sin(JioAnimationState.progress() * Math.PI * 8.0F);
        int blue = ((int) (150.0F * pulse) << 24) | (110 << 16) | (190 << 8) | 255;
        auraModel.renderToBuffer(poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucent(player.getSkin().texture())),
                packedLight, OverlayTexture.NO_OVERLAY, blue);
        poseStack.popPose();
    }
}
