package br.mevis.kencraft.client;

import br.mevis.kencraft.data.ModAttachments;
import br.mevis.kencraft.data.PlayerData;
import br.mevis.kencraft.data.Race;
import br.mevis.kencraft.data.SpiritualState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Player aura layer used by Jio animations and the first spiritual release state. */
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
        String technique = PlayerData.normalizeTechnique(data.jioTechnique());
        SpiritualState spiritual = player.getData(ModAttachments.SPIRITUAL_STATE);
        boolean sujo = spiritual.isSujo() && (data.race() == Race.HUMAN || data.race() == Race.HYBRID || data.race() == Race.JASHIN) && !"NONE".equals(technique);
        boolean animationAura = data.race() == Race.HUMAN && "Seishin dan".equalsIgnoreCase(technique) && data.jioAbilitySlot() == 2 && JioAnimationState.active();
        if (!sujo && !animationAura) return;

        getParentModel().copyPropertiesTo(auraModel);
        auraModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        poseStack.pushPose();
        if (sujo) {
            float pulse = 0.88F + 0.10F * (float) Math.sin((player.tickCount + partialTick) * 0.22F);
            int rgb = techniqueColor(technique);
            int alpha = (int) (115.0F * pulse);
            int tint = (alpha << 24) | rgb;
            poseStack.scale(1.025F, 1.025F, 1.025F);
            auraModel.renderToBuffer(poseStack,
                    bufferSource.getBuffer(RenderType.entityTranslucent(player.getSkin().texture())),
                    packedLight, OverlayTexture.NO_OVERLAY, tint);
        }
        if (animationAura) {
            float pulse = 0.78F + 0.18F * (float) Math.sin(JioAnimationState.progress() * Math.PI * 8.0F);
            int blue = ((int) (150.0F * pulse) << 24) | (110 << 16) | (190 << 8) | 255;
            auraModel.renderToBuffer(poseStack,
                    bufferSource.getBuffer(RenderType.entityTranslucent(player.getSkin().texture())),
                    packedLight, OverlayTexture.NO_OVERLAY, blue);
        }
        poseStack.popPose();
    }

    private static int techniqueColor(String technique) {
        return switch (technique) {
            case "The Paradise" -> (185 << 16) | (220 << 8) | 255;
            case "The King of Lies" -> (220 << 16) | (105 << 8) | 185;
            case "God Thunder" -> (255 << 16) | (235 << 8) | 90;
            case "Hakai satsu Totetsu: Seimei kui" -> (220 << 16) | (80 << 8) | 80;
            case "Kata kyoka" -> (100 << 16) | (220 << 8) | 145;
            default -> (115 << 16) | (150 << 8) | 255;
        };
    }
}
