package br.mevis.kencraft.client;

import br.mevis.kencraft.KenCraft;
import br.mevis.kencraft.entity.*;
import br.mevis.kencraft.event.SpiritualTrainingSystem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid=KenCraft.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class KenCraftEntityRenderers {
 public static final ModelLayerLocation KIKAN_LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"kikan"),"main");
 private static final ResourceLocation RINKA_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/rinka.png");
 private static final ResourceLocation RISHIN_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/rishin_generated.png");
 private static final ResourceLocation AODAI_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/aodai.png");
 private static final ResourceLocation SHOP_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/arf.png");
 private static final ResourceLocation INTERIOR_SPIRIT_PARADISE_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/interior_spirit_paradise.png");
 private static final ResourceLocation INTERIOR_SPIRIT_KING_TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/interior_spirit_king_of_lies.png");
 private KenCraftEntityRenderers(){}
 @SubscribeEvent public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e){
  e.registerEntityRenderer(KenCraftEntities.RINKA.get(),RinkaRenderer::new);
  e.registerEntityRenderer(KenCraftEntities.RANK_C_RINKA.get(),RankCRinkaRenderer::new);
  e.registerEntityRenderer(KenCraftEntities.RINKA_HUNGRY.get(),RinkaHungryRenderer::new);
  e.registerEntityRenderer(KenCraftEntities.RISHIN.get(),RishinRenderer::new);
  e.registerEntityRenderer(KenCraftEntities.AODAI.get(),AodaiRenderer::new);
  e.registerEntityRenderer(KenCraftEntities.ARF_INVESTIGATOR.get(),ArfRenderer::new);
  e.registerEntityRenderer(KenCraftEntities.ARF_GENERAL.get(),GeneralRenderer::new);
  e.registerEntityRenderer(KenCraftEntities.INTERIOR_SPIRIT.get(),InteriorSpiritRenderer::new);
  e.registerEntityRenderer(KenCraftEntities.ARTIFACT_SHOP.get(),ArtifactShopRenderer::new);
 }
 @SubscribeEvent public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e){
  e.registerLayerDefinition(KIKAN_LAYER,KikanModel::createBodyLayer);
  e.registerLayerDefinition(AodaiSnakeModel.LAYER,AodaiSnakeModel::createBodyLayer);
 }
 @SubscribeEvent public static void addPlayerLayers(EntityRenderersEvent.AddLayers e){
  for(PlayerSkin.Model skin:e.getSkins())if(e.getSkin(skin) instanceof PlayerRenderer pr){pr.addLayer(new KikanLayer(pr,e.getEntityModels()));pr.addLayer(new JioAuraLayer(pr,e.getEntityModels()));}
 }
 private static HumanoidModel<RinkaEntity> playerModel(EntityRendererProvider.Context c){return new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER));}
 private static HumanoidModel<InteriorSpiritEntity> spiritModel(EntityRendererProvider.Context c){return new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER));}
 private static final class RinkaRenderer extends HumanoidMobRenderer<RinkaEntity,HumanoidModel<RinkaEntity>>{RinkaRenderer(EntityRendererProvider.Context c){super(c,playerModel(c),.5F);}public ResourceLocation getTextureLocation(RinkaEntity e){return RINKA_TEXTURE;}}
 private static final class RankCRinkaRenderer extends HumanoidMobRenderer<RankCRinkaEntity,HumanoidModel<RankCRinkaEntity>>{RankCRinkaRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),.5F);}public ResourceLocation getTextureLocation(RankCRinkaEntity e){return RINKA_TEXTURE;}}
 private static final class RinkaHungryRenderer extends HumanoidMobRenderer<RinkaHungryEntity,HumanoidModel<RinkaHungryEntity>>{
  RinkaHungryRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),.5F);this.model.hat.visible=false;addLayer(new RinkaHungryKikanLayer(this,c.getModelSet()));}
  public ResourceLocation getTextureLocation(RinkaHungryEntity e){return ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/rinka_hungry.png");}
 }
 private static final class RinkaHungryKikanLayer extends net.minecraft.client.renderer.entity.layers.RenderLayer<RinkaHungryEntity,HumanoidModel<RinkaHungryEntity>>{
  private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(KenCraft.MOD_ID,"textures/entity/kikan.png");
  private final KikanModel model;
  RinkaHungryKikanLayer(net.minecraft.client.renderer.entity.RenderLayerParent<RinkaHungryEntity,HumanoidModel<RinkaHungryEntity>> parent,net.minecraft.client.model.geom.EntityModelSet set){super(parent);model=new KikanModel(set.bakeLayer(KIKAN_LAYER));}
  @Override public void render(com.mojang.blaze3d.vertex.PoseStack poseStack,net.minecraft.client.renderer.MultiBufferSource buffer,int light,RinkaHungryEntity entity,float limbSwing,float limbSwingAmount,float partialTick,float ageInTicks,float netHeadYaw,float headPitch){
   if(!entity.isKikanActive())return;
   model.setKikakogouActive(false);model.setType(entity.getKikanType());model.animate(entity.getKikanAnimationProgress(partialTick),true,entity.getKikanAttackKey());
   poseStack.pushPose();poseStack.translate(0,.72D,.24D);model.renderToBuffer(poseStack,buffer.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(TEXTURE)),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0xFFFFFFFF);poseStack.popPose();
  }
 }
 private static final class RishinRenderer extends HumanoidMobRenderer<RishinEntity,HumanoidModel<RishinEntity>>{RishinRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),.5F);this.model.hat.visible=false;}public ResourceLocation getTextureLocation(RishinEntity e){return RISHIN_TEXTURE;}}
 private static final class AodaiRenderer extends HumanoidMobRenderer<AodaiEntity,HumanoidModel<AodaiEntity>>{AodaiRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),.5F);this.model.hat.visible=false;addLayer(new AodaiSnakeLayer(this,c));}public ResourceLocation getTextureLocation(AodaiEntity e){return AODAI_TEXTURE;}}
 private static final class ArfRenderer extends HumanoidMobRenderer<ArfInvestigatorEntity,HumanoidModel<ArfInvestigatorEntity>>{ArfRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),.5F);}public ResourceLocation getTextureLocation(ArfInvestigatorEntity e){return SHOP_TEXTURE;}}
 private static final class GeneralRenderer extends HumanoidMobRenderer<ArfGeneralEntity,HumanoidModel<ArfGeneralEntity>>{GeneralRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),.5F);}public ResourceLocation getTextureLocation(ArfGeneralEntity e){return SHOP_TEXTURE;}}
 private static final class ArtifactShopRenderer extends HumanoidMobRenderer<ArtifactShopEntity,HumanoidModel<ArtifactShopEntity>>{ArtifactShopRenderer(EntityRendererProvider.Context c){super(c,new HumanoidModel<>(c.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)),.5F);this.model.hat.visible=false;}public ResourceLocation getTextureLocation(ArtifactShopEntity e){return SHOP_TEXTURE;}}
 private static final class InteriorSpiritRenderer extends HumanoidMobRenderer<InteriorSpiritEntity,HumanoidModel<InteriorSpiritEntity>>{InteriorSpiritRenderer(EntityRendererProvider.Context c){super(c,spiritModel(c),.5F);}public ResourceLocation getTextureLocation(InteriorSpiritEntity e){return e.level().dimension()==SpiritualTrainingSystem.KING_TRAINING?INTERIOR_SPIRIT_KING_TEXTURE:INTERIOR_SPIRIT_PARADISE_TEXTURE;}}
}